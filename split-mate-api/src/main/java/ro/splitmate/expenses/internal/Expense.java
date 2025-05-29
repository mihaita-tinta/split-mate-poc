package ro.splitmate.expenses.internal;

import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.expenses.api.ExpenseIdentifier;
import ro.splitmate.payments.api.PaymentConfirmed;
import ro.splitmate.types.CreationTime;
import ro.splitmate.types.TargetAmount;
import ro.splitmate.types.Title;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Expense {
    private ExpenseIdentifier id;
    private CustomerIdentifier userId;
    private Title title;
    private TargetAmount targetAmount;
    private CreationTime creationDate;
    private List<Share> shares;

    public Expense(ExpenseIdentifier id, CustomerIdentifier userId,
                   Title title,
                   TargetAmount targetAmount,
                   CreationTime creationDate,
                   List<Share> shares) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.targetAmount = targetAmount;
        this.creationDate = creationDate;
        this.shares = shares;
    }

    public ExpenseIdentifier id() {
        return id;
    }

    public CustomerIdentifier userId() {
        return userId;
    }

    public Title title() {
        return title;
    }

    public TargetAmount targetAmount() {
        return targetAmount;
    }

    public CreationTime creationDate() {
        return creationDate;
    }

    public List<Share> shares() {
        return shares;
    }

    public Share claim(TargetAmount amount, CustomerIdentifier senderId) {

        BigDecimal remainingToBePaid = getAmountNotClaimed();
        if (remainingToBePaid
                .compareTo(amount.targetAmount()) < 0) {
            BigDecimal exceededAmount = remainingToBePaid.subtract(amount.targetAmount());
            throw new TargetAmountExceeded(
                    "Remaining value to be paid is " + remainingToBePaid +
                            ", exceeded by " + exceededAmount + " from total " + amount.targetAmount(),
                    targetAmount.targetAmount(),
                    amount.targetAmount(),
                    remainingToBePaid);
        }

        Share newShare = getPaymentAcceptedByUser(senderId)
                .map(s -> s.withUpdatedAmount(amount))
                .orElseGet(() -> {
                    Share.Status status = senderId.equals(userId) ? Share.Status.ACCEPTED_TO_PAY : Share.Status.OWNER_BEHALF;
                    return new Share(null,
                            this.id, senderId, this.userId, status,
                            amount,
                            new CreationTime(LocalDateTime.now())
                    );
                });
        List<Share> updated = new ArrayList<>(
                shares.stream()
                        .filter(s -> !(s.status() == Share.Status.ACCEPTED_TO_PAY &&
                                s.sender().equals(senderId)))
                        .toList()
        );
        updated.add(newShare);
        this.shares = updated;
        return newShare;
    }

    Optional<Share> getPaymentAcceptedByUser(CustomerIdentifier senderId) {
        return shares.stream()
                .filter(s -> s.status() == Share.Status.ACCEPTED_TO_PAY &&
                        s.sender().equals(senderId))
                .findFirst();
    }

    void onPaymentConfirmed(PaymentConfirmed payment) {
        shares.stream().filter(s -> s.id().equals(
                        new ShareIdentifier(payment.internalReferenceIdentifier().id())))
                .findAny()
                .ifPresent(share -> {
                    var newShare = share.onPaymentConfirmed();
                    List<Share> withoutNewShare = shares
                            .stream().filter(s -> !s.id().equals(newShare.id()))
                            .toList();
                    List<Share> includingNewShare = new ArrayList<>(withoutNewShare);
                    includingNewShare.add(newShare);
                    this.shares = includingNewShare;
                });
    }

    boolean settle() {
        return isSettled();
    }

    private boolean isSettled() {
        return shares.stream()
                .filter(s ->
                        s.status() == Share.Status.PAYMENT_CONFIRMED
                                || s.status() == Share.Status.OWNER_BEHALF)
                .map(s -> s.shareAmount().targetAmount())
                .reduce(new BigDecimal(0), BigDecimal::add)
                .compareTo(targetAmount.targetAmount()) == 0;
    }

    BigDecimal getAmountNotClaimed() {
        return targetAmount.targetAmount().subtract(getAmountToBePaid());
    }

    BigDecimal getAmountToBePaid() {
        return getSharesTotal(shares);
    }

    static BigDecimal getSharesTotal(List<Share> shares) {
        return shares.stream()
                .map(s -> s.shareAmount().targetAmount())
                .reduce(new BigDecimal(0), BigDecimal::add);
    }

    BigDecimal getOwnedMoneyToReceiver() {
        return shares.stream()
                .filter(s -> {
                    boolean isExpenseOwner = s.sender().equals(this.userId);
                    return s.isPayed() && !isExpenseOwner;
                })
                .map(s -> s.shareAmount().targetAmount())
                .reduce(new BigDecimal(0), BigDecimal::add);
    }
}
