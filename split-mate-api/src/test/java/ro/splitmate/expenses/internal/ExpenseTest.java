package ro.splitmate.expenses.internal;

import org.junit.jupiter.api.Test;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.expenses.api.ExpenseIdentifier;
import ro.splitmate.types.CreationTime;
import ro.splitmate.types.TargetAmount;
import ro.splitmate.types.Title;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpenseTest {

    @Test
    void testMergeSharesByAcceptedToPay() {
        BigDecimal acceptedToPay = BigDecimal.valueOf(50.00);
        ExpenseIdentifier expenseId = new ExpenseIdentifier(1L);
        CustomerIdentifier johnId = new CustomerIdentifier(2L);
        CustomerIdentifier alexId = new CustomerIdentifier(3L);

        Share johnShare1 = new Share(new ShareIdentifier(1L),
                expenseId, johnId, alexId, Share.Status.ACCEPTED_TO_PAY,
                new TargetAmount(BigDecimal.valueOf(25.00)),
                new CreationTime(LocalDateTime.now())
        );
        Share alexShare = new Share(new ShareIdentifier(2L),
                expenseId, alexId, alexId, Share.Status.ACCEPTED_TO_PAY,
                new TargetAmount(acceptedToPay),
                new CreationTime(LocalDateTime.now())
        );

        Expense expense = new Expense(
                expenseId,
                alexId,
                new Title("testing"),
                new TargetAmount(BigDecimal.valueOf(100.00)),
                new CreationTime(LocalDateTime.now().minusHours(1)),
                List.of(johnShare1, alexShare));

        Share updatedShare = expense.claimAmountByUser(
                new TargetAmount(BigDecimal.valueOf(25.00)),
                johnId);

        assertEquals(2, expense.shares().size());
        assertEquals(acceptedToPay, expense.shares().stream()
                .filter(s -> s.sender().equals(alexId))
                .findAny().get().shareAmount().targetAmount());
        assertEquals(acceptedToPay, updatedShare.shareAmount().targetAmount());
    }

    @Test
    void testMergeSharesExcludesOtherStatuses() {
        ExpenseIdentifier expenseId = new ExpenseIdentifier(1L);
        CustomerIdentifier johnId = new CustomerIdentifier(2L);
        CustomerIdentifier alexId = new CustomerIdentifier(3L);

        Share johnShare1 = new Share(new ShareIdentifier(1L),
                expenseId, johnId, alexId, Share.Status.PAYMENT_CONFIRMED,
                new TargetAmount(BigDecimal.valueOf(25.00)),
                new CreationTime(LocalDateTime.now())
        );
        Share alexShare = new Share(new ShareIdentifier(2L),
                expenseId, alexId, alexId, Share.Status.ACCEPTED_TO_PAY,
                new TargetAmount(BigDecimal.valueOf(50.00)),
                new CreationTime(LocalDateTime.now())
        );

        Expense expense = new Expense(
                expenseId,
                alexId,
                new Title("testing"),
                new TargetAmount(BigDecimal.valueOf(100.00)),
                new CreationTime(LocalDateTime.now().minusHours(1)),
                List.of(johnShare1, alexShare));

        Share updatedShare = expense.claimAmountByUser(
                new TargetAmount(BigDecimal.valueOf(25.00)),
                johnId);

        assertEquals(3, expense.shares().size());
        assertEquals(BigDecimal.valueOf(50.00), expense.shares().stream()
                .filter(s -> s.sender().equals(alexId))
                .findAny().get().shareAmount().targetAmount());
        assertEquals(BigDecimal.valueOf(25.00), expense.shares().stream()
                .filter(s -> s.sender().equals(johnId) && s.status() == Share.Status.ACCEPTED_TO_PAY)
                .findAny().get().shareAmount().targetAmount());
        assertEquals(BigDecimal.valueOf(25.00), expense.shares().stream()
                .filter(s -> s.sender().equals(johnId) && s.status() == Share.Status.PAYMENT_CONFIRMED)
                .findAny().get().shareAmount().targetAmount());
    }

}