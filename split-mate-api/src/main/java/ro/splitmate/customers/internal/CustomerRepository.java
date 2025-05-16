package ro.splitmate.customers.internal;

import java.util.Optional;

public interface CustomerRepository {
    Optional<Customer> findByUsername(String username);

    Customer create(String username, String password);

    void delete(Customer customer);

}
