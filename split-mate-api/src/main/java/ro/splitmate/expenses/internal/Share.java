package ro.splitmate.expenses.internal;

import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.payments.internal.noda.PaymentStatus;
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
        OWNER_BEHALF;


    }

    public boolean isPayed() {
        return this.status == Share.Status.PAYMENT_CONFIRMED;
    }
    public boolean shouldAllowNewPayments() {
        return this.status == Status.ACCEPTED_TO_PAY;
    }
    public Share onPaymentConfirmed() {
        return new Share(id, expenseId, sender, receiver, Status.PAYMENT_CONFIRMED, shareAmount, creationDate);
    }

    public Share withUpdatedAmount(TargetAmount newAmount) {
        return new Share(id, expenseId, sender, receiver, status,
                new TargetAmount(newAmount.targetAmount()), creationDate);
    }
}
