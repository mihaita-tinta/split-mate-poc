package ro.splitmate.expenses.internal.jpa;

import org.springframework.stereotype.Service;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.expenses.api.ExpenseIdentifier;
import ro.splitmate.types.TargetAmount;
import ro.splitmate.types.Title;
import ro.splitmate.expenses.internal.Expense;
import ro.splitmate.expenses.internal.ExpenseRepository;

import java.util.List;
import java.util.Optional;

import static ro.splitmate.expenses.internal.jpa.JpaExpense.fromDomain;
import static ro.splitmate.expenses.internal.jpa.JpaExpense.toDomain;

@Service
class ExpenseRepositoryAdapter implements ExpenseRepository {
    private final JpaExpenseRepository expenseRepository;
    private final JpaShareRepository shareRepository;

    ExpenseRepositoryAdapter(JpaExpenseRepository expenseRepository, JpaShareRepository shareRepository) {
        this.expenseRepository = expenseRepository;
        this.shareRepository = shareRepository;
    }

    @Override
    public List<Expense> findByUserId(CustomerIdentifier customerId) {
        return expenseRepository.findByUserId(customerId.id())
                .stream()
                .map(JpaExpense::toDomain)
                .toList();
    }
    @Override
    public Optional<Expense> findByUserIdAndId(CustomerIdentifier customerId, ExpenseIdentifier expenseIdentifier) {
        return expenseRepository.findByUserIdAndId(customerId.id(), expenseIdentifier.id())
                .map(JpaExpense::toDomain)
                ;
    }

    @Override
    public Optional<Expense> findExpense(ExpenseIdentifier expenseIdentifier) {
        return expenseRepository.findById(expenseIdentifier.id())
                .map(JpaExpense::toDomain);
    }
    @Override
    public Expense update(Expense expense) {
        JpaExpense entity = fromDomain(expense);
        shareRepository.saveAll(entity.getShares());
        return JpaExpense.toDomain(expenseRepository.save(entity));
    }

    @Override
    public Expense create(Title title, TargetAmount targetAmount, CustomerIdentifier customerId) {
        JpaExpense newExpense = JpaExpense.create(title, targetAmount, customerId);
        return toDomain(expenseRepository.save(newExpense));
    }

}
