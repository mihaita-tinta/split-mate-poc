package ro.splitmate.customers.internal;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ro.splitmate.customers.api.CustomerIdentifier;

import java.security.Principal;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private CustomerManagement userRepository;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentCustomerArgumentResolver(userRepository));
    }


    private record CurrentCustomerArgumentResolver(
            CustomerManagement userRepository) implements HandlerMethodArgumentResolver {

        public boolean supportsParameter(MethodParameter parameter) {
            MethodParameter nestedParameter = parameter.nestedIfOptional();
            Class<?> paramType = nestedParameter.getNestedParameterType();
            return CustomerIdentifier.class.isAssignableFrom(paramType);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) throws Exception {
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if (request == null) {
                throw new IllegalStateException("Current request is not of type HttpServletRequest: " + String.valueOf(webRequest));
            } else {
                Principal principal = request.getUserPrincipal();
                return getCustomerId(principal);
            }
        }


        CustomerIdentifier getCustomerId(Principal user) {
            if (user == null) {
                return null;
            }
            return userRepository.findByUsername(user.getName())
                    .map(Customer::id)
                    .orElse(null);
        }
    }
}