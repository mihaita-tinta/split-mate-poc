package ro.splitmate.expenses.api;

import org.springframework.modulith.events.Externalized;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.types.TargetAmount;
import ro.splitmate.types.Title;

@Externalized("expenses.AllMoneyArePaid::#{expenseId()}")
public record AllMoneyArePaid(CustomerIdentifier id, ExpenseIdentifier expenseId, Title title, TargetAmount amount) {
}

