package ro.splitmate.customers.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;


@RestController
class CustomerController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerManagement userRepository;

    public CustomerController(CustomerManagement userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/whoami")
    public ResponseEntity<CustomerDto> getAuthentication(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .map(CustomerController::toCustomerDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private static CustomerDto toCustomerDto(Customer customer) {
        return new CustomerDto(
                customer.id().id().toString(),
                customer.username(),
                customer.roles(),
                true,
                customer.creationDate());
    }

    record CustomerDto(String id, String username, List<String> roles,
                       boolean active, LocalDateTime creationDate) {
    }

}
