package ro.splitmate.payouts;

import org.springframework.modulith.events.Externalized;

@Externalized("payouts.MoneySentToUser::#{id()}")
public record MoneySentToUser(ExpenseSettled.CustomerIdentifier id,
                              ExpenseSettled.ExpenseIdentifier expenseId,
                              ExpenseSettled.Title title,
                              ExpenseSettled.TargetAmount amount) {
}

