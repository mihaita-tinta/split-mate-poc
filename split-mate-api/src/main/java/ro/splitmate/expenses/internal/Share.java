package ro.splitmate.expenses.internal;

import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.payments.api.PaymentStatus;
import ro.splitmate.types.CreationTime;
import ro.splitmate.expenses.api.ExpenseIdentifier;
import ro.splitmate.types.TargetAmount;

public record Share(
        ShareIdentifier id,
        ExpenseIdentifier expenseId,
        CustomerIdentifier sender,
        CustomerIdentifier receiver,
        Status status,
        TargetAmount shareAmount,
        CreationTime creationDate) {

    public enum Status {
        ACCEPTED_TO_PAY,
        PAYMENT_STARTED,
        PAYMENT_CONFIRMED,
        PAYMENT_FAILED,
        MONEY_RECEIVED,
        OWNER_BEHALF,
    }

    public Share onPaymentUpdate(PaymentStatus paymentStatus) {

        var status = switch (paymentStatus) {
            case New -> Status.PAYMENT_STARTED;
            case Failed -> Status.PAYMENT_FAILED;
            case Processing -> Status.PAYMENT_STARTED;
            case Done -> Status.PAYMENT_CONFIRMED;
        };
        return new Share(id, expenseId, sender, receiver, status, shareAmount, creationDate);
    }

    public Share withUpdatedAmount(TargetAmount newAmount) {
        return new Share(id, expenseId, sender, receiver, status,
                new TargetAmount(shareAmount.targetAmount()
                        .add(newAmount.targetAmount())), creationDate);
    }
}
