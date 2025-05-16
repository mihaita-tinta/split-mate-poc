package ro.splitmate.payouts;

import org.springframework.modulith.events.Externalized;

@Externalized("payouts.MoneySentToUser::#{id()}")
public record MoneySentToUser(AllMoneyArePaid.CustomerIdentifier id,
                              AllMoneyArePaid.ExpenseIdentifier expenseId,
                              AllMoneyArePaid.Title title,
                              AllMoneyArePaid.TargetAmount amount) {
}

