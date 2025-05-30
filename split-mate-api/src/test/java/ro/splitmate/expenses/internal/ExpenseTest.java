package ro.splitmate.expenses.internal;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.expenses.api.ExpenseIdentifier;
import ro.splitmate.types.CreationTime;
import ro.splitmate.types.TargetAmount;
import ro.splitmate.types.Title;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpenseTest {
    ExpenseIdentifier expenseId = new ExpenseIdentifier(1L);
    CustomerIdentifier johnId = new CustomerIdentifier(2L);
    CustomerIdentifier alexId = new CustomerIdentifier(3L);
    CustomerIdentifier mikeId = new CustomerIdentifier(4L);

    @Test
    void testMergeSharesByAcceptedToPay() {
        BigDecimal acceptedToPay = BigDecimal.valueOf(50.00);

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

        var updatedExpense = expense.claim(
                new TargetAmount(BigDecimal.valueOf(25.00)),
                johnId);

        assertEquals(2, updatedExpense.shares().size());
        assertEquals(acceptedToPay, updatedExpense.getShareNotPayedByUser(alexId)
                .get().shareAmount().targetAmount());
    }
    @Test
    void testClaimMoreThenTotal() {

        Expense expense = new Expense(
                expenseId,
                alexId,
                new Title("testing"),
                new TargetAmount(BigDecimal.valueOf(100.00)),
                new CreationTime(LocalDateTime.now().minusHours(1)),
                List.of());

        var updatedExpense = expense.claim(
                new TargetAmount(BigDecimal.valueOf(35.00)),
                johnId);
        updatedExpense = updatedExpense.claim(
                new TargetAmount(BigDecimal.valueOf(25.00)),
                alexId);

        assertEquals(2, updatedExpense.shares().size());
        assertEquals(BigDecimal.valueOf(40.00), updatedExpense.getRemainingToBePaid());
        assertEquals(BigDecimal.valueOf(25.00), updatedExpense.getAmountClaimedButNotPayedByUser(alexId));
        assertEquals(BigDecimal.valueOf(35.00), updatedExpense.getAmountClaimedButNotPayedByUser(johnId));

        updatedExpense = updatedExpense.claim(
                new TargetAmount(BigDecimal.valueOf(65.00)),
                alexId);
        assertEquals(2, updatedExpense.shares().size());
        assertEquals(BigDecimal.valueOf(65.00), updatedExpense.getAmountClaimedButNotPayedByUser(alexId));
        assertEquals(BigDecimal.valueOf(35.00), updatedExpense.getAmountClaimedButNotPayedByUser(johnId));

        final var e = updatedExpense;
        TargetAmountExceeded ex = assertThrows(TargetAmountExceeded.class, () -> e.claim(
                new TargetAmount(BigDecimal.valueOf(65.01)),
                alexId));
        assertThat(ex.getMessage()).isEqualTo("Remaining value to be paid is 65.0, exceeded by -0.01 from total 100.0");

        ex = assertThrows(TargetAmountExceeded.class, () -> e.claim(
                new TargetAmount(BigDecimal.valueOf(35.01)),
                johnId));
        assertThat(ex.getMessage()).isEqualTo("Remaining value to be paid is 35.0, exceeded by -0.01 from total 100.0");

        ex = assertThrows(TargetAmountExceeded.class, () -> e.claim(
                new TargetAmount(BigDecimal.valueOf(10)),
                mikeId));
        assertThat(ex.getMessage()).isEqualTo("Remaining value to be paid is 0.0, exceeded by -10.0 from total 100.0");
    }
    @Test
    void testMergeSharesByOwnerBehalf() {

        Share johnShare1 = new Share(new ShareIdentifier(1L),
                expenseId, johnId, alexId, Share.Status.ACCEPTED_TO_PAY,
                new TargetAmount(BigDecimal.valueOf(25.00)),
                new CreationTime(LocalDateTime.now())
        );
        Share alexShare = new Share(new ShareIdentifier(2L),
                expenseId, alexId, alexId, Share.Status.OWNER_BEHALF,
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

        var updatedExpense = expense.claim(
                new TargetAmount(BigDecimal.valueOf(25.00)),
                alexId);

        assertEquals(2, updatedExpense.shares().size());
        assertEquals(BigDecimal.valueOf(25.00), updatedExpense.getShareNotPayedByUser(johnId)
                .get().shareAmount().targetAmount());
        assertEquals(BigDecimal.valueOf(25.00), updatedExpense.getShareNotPayedByUser(alexId)
                .get().shareAmount().targetAmount());
    }

    @Test
    void testMergeSharesExcludesOtherStatuses() {
        ExpenseIdentifier expenseId = new ExpenseIdentifier(1L);

        Share johnShare1 = new Share(new ShareIdentifier(1L),
                expenseId, johnId, alexId, Share.Status.PAYMENT_CONFIRMED,
                new TargetAmount(BigDecimal.valueOf(25.00)),
                new CreationTime(LocalDateTime.now())
        );
        Share alexShare = new Share(new ShareIdentifier(2L),
                expenseId, alexId, alexId, Share.Status.OWNER_BEHALF,
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

        expense = expense.claim(
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
    @Test
    void testOwnedMoney() {
        ExpenseIdentifier expenseId = new ExpenseIdentifier(1L);

        Share johnShare1 = new Share(new ShareIdentifier(1L),
                expenseId, johnId, alexId, Share.Status.PAYMENT_CONFIRMED,
                new TargetAmount(BigDecimal.valueOf(50.01)),
                new CreationTime(LocalDateTime.now())
        );
        Share alexShare = new Share(new ShareIdentifier(2L),
                expenseId, alexId, alexId, Share.Status.OWNER_BEHALF,
                new TargetAmount(BigDecimal.valueOf(49.99)),
                new CreationTime(LocalDateTime.now())
        );

        Expense expense = new Expense(
                expenseId,
                alexId,
                new Title("testing"),
                new TargetAmount(BigDecimal.valueOf(100.00)),
                new CreationTime(LocalDateTime.now().minusHours(1)),
                List.of(johnShare1, alexShare));

        assertEquals(BigDecimal.valueOf(50.01), expense.getOwnedMoneyToReceiver());
    }
    @Test
    void testSettle() {
        ExpenseIdentifier expenseId = new ExpenseIdentifier(1L);

        Share johnShare1 = new Share(new ShareIdentifier(1L),
                expenseId, johnId, alexId, Share.Status.PAYMENT_CONFIRMED,
                new TargetAmount(BigDecimal.valueOf(50.00)),
                new CreationTime(LocalDateTime.now())
        );
        Share alexShare = new Share(new ShareIdentifier(2L),
                expenseId, alexId, alexId, Share.Status.OWNER_BEHALF,
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

        assertTrue(expense.settle());
    }

    @Test
    void testNotSettled() {
        ExpenseIdentifier expenseId = new ExpenseIdentifier(1L);

        Share alexShare = new Share(new ShareIdentifier(2L),
                expenseId, alexId, alexId, Share.Status.OWNER_BEHALF,
                new TargetAmount(BigDecimal.valueOf(50.00)),
                new CreationTime(LocalDateTime.now())
        );

        Expense expense = new Expense(
                expenseId,
                alexId,
                new Title("testing"),
                new TargetAmount(BigDecimal.valueOf(100.00)),
                new CreationTime(LocalDateTime.now().minusHours(1)),
                List.of(alexShare));

        assertFalse(expense.settle());
    }

}