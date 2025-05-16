package ro.splitmate.customers.internal.jpa;

import org.springframework.stereotype.Service;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.customers.internal.Customer;
import ro.splitmate.customers.internal.CustomerRepository;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static ro.splitmate.customers.internal.jpa.JpaCustomer.createUser;

@Service
class CustomerRepositoryAdapter implements CustomerRepository {
    private final JpaCustomerRepository repository;

    CustomerRepositoryAdapter(JpaCustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        return repository.findByUsername(username)
                .map(CustomerRepositoryAdapter::toDomain);
    }

    private static Customer toDomain(JpaCustomer jpa) {
        Customer c = new Customer(jpa.getUsername(),
                jpa.getPassword(), new CustomerIdentifier(jpa.getId()),
                jpa.getCreationDate().toLocalDateTime());
        c.setRoles(List.of(jpa.getRoles().split(",")));
        return c;
    }

    private static JpaCustomer fromDomain(Customer c) {
        JpaCustomer jpa = new JpaCustomer();
        jpa.setUsername(c.getUsername());
        jpa.setPassword(c.getPassword());
        jpa.setRoles(String.join(",", c.getRoles()));
        jpa.setCreationDate(Timestamp.from(c.getCreationDate().toInstant(ZoneOffset.UTC)));
        return jpa;
    }

    @Override
    public Customer create(String username, String password) {
        JpaCustomer newUser = createUser(username, password);
        JpaCustomer save = repository.save(newUser);
        Customer domain = toDomain(save);
        return domain;
    }

    @Override
    public void delete(Customer customer) {
        repository.delete(fromDomain(customer));
    }
}
