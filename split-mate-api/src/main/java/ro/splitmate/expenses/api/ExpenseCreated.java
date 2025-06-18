package ro.splitmate.expenses.api;

import org.springframework.modulith.events.Externalized;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.types.TargetAmount;
import ro.splitmate.types.Title;

@Externalized("expenses.ExpenseCreated::#{expenseId()}")
public record ExpenseCreated(CustomerIdentifier id, ExpenseIdentifier expenseId, Title title, TargetAmount amount) {
}

