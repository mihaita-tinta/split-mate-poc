package ro.splitmate.customers.internal;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.customers.api.CustomerOffboarded;
import ro.splitmate.customers.api.CustomerOnboarded;

import java.util.Optional;

@Component
class CustomerManagement {

    private final CustomerRepository customers;
    private final ApplicationEventPublisher publisher;

    public CustomerManagement(CustomerRepository customers, ApplicationEventPublisher publisher) {
        this.customers = customers;
        this.publisher = publisher;
    }

    public Customer onboard(String username, String password) {
        return (Customer) customers.findByUsername(username)
                .map(existing -> {
                    throw new IllegalStateException("Username already exists");
                })
                .orElseGet(() -> {
                    Customer domain = customers.create(username, password);
                    publisher.publishEvent(new CustomerOnboarded(domain.id()));
                    return domain;
                });
    }

    public Optional<Customer> findByUsername(String username) {
        return customers.findByUsername(username);
    }

    public Customer offboard(Customer customer) {
        customers.delete(customer);
        publisher.publishEvent(new CustomerOffboarded(customer.id()));
        return customer;
    }
}
