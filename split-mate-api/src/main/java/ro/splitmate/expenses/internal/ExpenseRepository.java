package ro.splitmate.expenses.internal;

import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.expenses.api.ExpenseIdentifier;
import ro.splitmate.types.TargetAmount;
import ro.splitmate.types.Title;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository {

    List<Expense> findByUserId(CustomerIdentifier customerId);
    Optional<Expense> findByUserIdAndId(CustomerIdentifier customerId, ExpenseIdentifier expenseIdentifier);

    Optional<Expense> findExpense(ExpenseIdentifier expenseIdentifier);

    Expense update(Expense expense);

    Expense create(Title title, TargetAmount targetAmount, CustomerIdentifier customerId);

}
