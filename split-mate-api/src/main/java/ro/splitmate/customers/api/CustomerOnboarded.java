package ro.splitmate.customers.api;

import org.springframework.modulith.events.Externalized;

@Externalized("customers.CustomerOnboarded")
public record CustomerOnboarded(CustomerIdentifier id) {
}

