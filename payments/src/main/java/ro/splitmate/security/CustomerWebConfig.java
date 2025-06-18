package ro.splitmate.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ro.splitmate.types.CustomerIdentifier;

import java.security.Principal;
import java.util.List;
import java.util.stream.Stream;

@Configuration
public class CustomerWebConfig implements WebMvcConfigurer {
    @Autowired
    JwtDecoder jwtDecoder;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentCustomerArgumentResolver());
    }


    private record CurrentCustomerArgumentResolver() implements HandlerMethodArgumentResolver {

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
            if (user instanceof JwtAuthenticationToken jwtAuth) {
                Long customerId =
                        Stream.of(jwtAuth.getToken()
                        .getClaim("scope")
                        .toString().split(","))
                                .filter(s -> s.startsWith("CUSTOMER_"))
                                .map(s -> s.substring("CUSTOMER_".length()))
                                .map(Long::parseLong)
                                .findFirst()
                                .get();

                return new CustomerIdentifier(customerId);
            }
            return null;
        }
    }
}