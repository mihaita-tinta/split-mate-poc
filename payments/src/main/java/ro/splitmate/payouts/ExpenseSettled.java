package ro.splitmate.payouts;

import org.springframework.modulith.events.Externalized;

import java.math.BigDecimal;

@Externalized("expenses.ExpenseSettled::#{id()}")
public record ExpenseSettled(CustomerIdentifier id, ExpenseIdentifier expenseId, Title title, TargetAmount amount) {

    public record CustomerIdentifier(Long id){}
    public record ExpenseIdentifier(Long id){}
    public record Title(String title){}
    public record TargetAmount(BigDecimal targetAmount){}

}