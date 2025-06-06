package ro.splitmate.customers.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
class CustomerDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(CustomerDetailsService.class);
    private final CustomerManagement customers;

    public CustomerDetailsService(CustomerManagement customers) {
        this.customers = customers;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername - username: {}", username);
        return customers.findByUsername(username)
                .map(customer -> User.builder()
                        .username(customer.username())
                        .password(customer.password())
                        .authorities("ROLE_USER", "CUSTOMER_" + customer.id().id())
                        .build())
                .orElseGet(() -> {
                    log.info("loadUserByUsername - not found username: {}", username);
                    return null;
                });
    }
}
