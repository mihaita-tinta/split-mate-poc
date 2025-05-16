package ro.splitmate.customers.internal.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface JpaCustomerRepository extends CrudRepository<JpaCustomer, Long> {

    Optional<JpaCustomer> findByUsername(String username);
    void deleteByUsername(String username);
}
