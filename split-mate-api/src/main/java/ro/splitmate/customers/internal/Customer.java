package ro.splitmate.customers.internal;

import ro.splitmate.customers.api.CustomerIdentifier;

import java.time.LocalDateTime;
import java.util.List;

public record Customer(CustomerIdentifier id, String username,
                       String password, List<String> roles, LocalDateTime creationDate) {

}