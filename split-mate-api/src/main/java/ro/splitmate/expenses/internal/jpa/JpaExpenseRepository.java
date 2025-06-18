package ro.splitmate.expenses.internal.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface JpaExpenseRepository extends CrudRepository<JpaExpense, Long> {
    @Query("SELECT e FROM JpaExpense e LEFT JOIN FETCH e.shares WHERE e.userId = :userId")
    List<JpaExpense> findByUserId(Long userId);
    @Query("SELECT e FROM JpaExpense e LEFT JOIN FETCH e.shares WHERE e.id = :id and  e.userId = :userId")
    Optional<JpaExpense> findByUserIdAndId(Long userId, Long id);
}
