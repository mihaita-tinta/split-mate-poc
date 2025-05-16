package ro.splitmate.payouts;

import org.springframework.modulith.events.Externalized;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.expenses.api.ExpenseIdentifier;
import ro.splitmate.types.TargetAmount;
import ro.splitmate.types.Title;

@Externalized("payouts.MoneySentToUser::#{id()}")
public record MoneySentToUser(CustomerIdentifier id, ExpenseIdentifier expenseId, Title title, TargetAmount amount) {
}

