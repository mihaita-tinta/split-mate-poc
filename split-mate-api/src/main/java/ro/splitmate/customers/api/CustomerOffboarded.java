package ro.splitmate.customers.api;

import org.springframework.modulith.events.Externalized;

@Externalized("customers.CustomerOffboarded")
public record CustomerOffboarded(CustomerIdentifier id) {
}
