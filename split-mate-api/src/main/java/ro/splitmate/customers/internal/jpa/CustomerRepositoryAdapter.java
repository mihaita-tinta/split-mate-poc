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
        Customer c = new Customer(new CustomerIdentifier(jpa.getId()),
                jpa.getUsername(),
                jpa.getPassword(),
                List.of(jpa.getRoles().split(",")),
                jpa.getCreationDate().toLocalDateTime());
        return c;
    }

    private static JpaCustomer fromDomain(Customer c) {
        JpaCustomer jpa = new JpaCustomer();
        jpa.setUsername(c.username());
        jpa.setPassword(c.password());
        jpa.setRoles(String.join(",", c.roles()));
        jpa.setCreationDate(Timestamp.from(c.creationDate().toInstant(ZoneOffset.UTC)));
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
