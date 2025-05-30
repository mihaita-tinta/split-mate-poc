package ro.splitmate.expenses.internal.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface JpaShareRepository extends CrudRepository<JpaShare, Long> {

    Optional<JpaShare> findByExpenseIdAndSenderId(Long expenseId, Long senderId);
}
