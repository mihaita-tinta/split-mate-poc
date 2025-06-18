package ro.splitmate.expenses.internal;

import org.springframework.security.access.AccessDeniedException;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.expenses.api.ExpenseIdentifier;
import ro.splitmate.expenses.internal.payments.PaymentConfirmed;
import ro.splitmate.types.CreationTime;
import ro.splitmate.types.ShareCode;
import ro.splitmate.types.TargetAmount;
import ro.splitmate.types.Title;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record Expense(
        ExpenseIdentifier id,
        CustomerIdentifier userId,
        Title title,
        Optional<ShareCode> shareCode,
        TargetAmount targetAmount,
        CreationTime creationDate,
        List<Share> shares
) {

    public Expense claim(TargetAmount amount, CustomerIdentifier senderId) {
        BigDecimal remainingToBePaid = getRemainingToBePaid().add(getAmountClaimedButNotPayedByUser(senderId));
        if (remainingToBePaid.compareTo(amount.targetAmount()) < 0) {
            BigDecimal exceededAmount = remainingToBePaid.subtract(amount.targetAmount());
            throw new TargetAmountExceeded(
                    "Remaining value to be paid is " + remainingToBePaid +
                            ", exceeded by " + exceededAmount + " from total " + this.targetAmount().targetAmount(),
                    targetAmount.targetAmount(),
                    amount.targetAmount(),
                    remainingToBePaid);
        }

        Share newShare = getShareNotPayedByUser(senderId)
                .map(s -> s.withUpdatedAmount(amount))
                .orElseGet(() -> {
                    Share.Status status = senderId.equals(userId) ? Share.Status.OWNER_BEHALF : Share.Status.ACCEPTED_TO_PAY;
                    return new Share(null,
                            this.id, senderId, this.userId, status,
                            amount,
                            new CreationTime(LocalDateTime.now())
                    );
                });
        List<Share> updated = new ArrayList<>(
                shares.stream()
                        .filter(s -> !(
                                (s.status() == Share.Status.ACCEPTED_TO_PAY
                                        || s.status() == Share.Status.OWNER_BEHALF) &&
                                        s.sender().equals(senderId)))
                        .toList()
        );
        updated.add(newShare);
        return new Expense(id, userId, title,
                shareCode,
                targetAmount, creationDate, updated);
    }

    public Expense shareToOthers(CustomerIdentifier currentUserId) {
        if (!userId.equals(currentUserId)) {
            throw new AccessDeniedException("Only the owner can share the expense");
        }
        return new Expense(id, userId, title,
                Optional.of(
                        new ShareCode(UUID.randomUUID().toString())
                ),
                targetAmount, creationDate, shares);
       }

    public Optional<Share> getShareNotPayedByUser(CustomerIdentifier senderId) {
        return shares.stream()
                .filter(s ->
                        s.sender().equals(senderId) &&
                                (s.status() == Share.Status.ACCEPTED_TO_PAY ||
                                        s.status() == Share.Status.OWNER_BEHALF
                                ))
                .findFirst();
    }

    public Expense onPaymentConfirmed(PaymentConfirmed payment) {
        Optional<Share> maybeShare = shares.stream().filter(s -> s.id().equals(
                        new ShareIdentifier(payment.internalReferenceIdentifier().id())))
                .findAny();
        if (maybeShare.isPresent()) {
            var newShare = maybeShare.get().onPaymentConfirmed();
            List<Share> withoutNewShare = shares
                    .stream().filter(s -> !s.id().equals(newShare.id()))
                    .toList();
            List<Share> includingNewShare = new ArrayList<>(withoutNewShare);
            includingNewShare.add(newShare);
            return new Expense(id, userId, title,
                    shareCode,
                    targetAmount, creationDate, includingNewShare);
        }
        return this;
    }

    public boolean settle() {
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

    BigDecimal getAmountClaimedButNotPayedByUser(CustomerIdentifier senderId) {
        return shares.stream()
                .filter(s -> !s.isPayed() && s.sender().equals(senderId))
                .map(s -> s.shareAmount().targetAmount())
                .reduce(new BigDecimal(0), BigDecimal::add);
    }

    BigDecimal getRemainingToBePaid() {
        return targetAmount.targetAmount().subtract(shares.stream()
                .map(s -> s.shareAmount().targetAmount())
                .reduce(new BigDecimal(0), BigDecimal::add));
    }


    public BigDecimal getOwnedMoneyToReceiver() {
        return shares.stream()
                .filter(s -> {
                    boolean isExpenseOwner = s.sender().equals(this.userId);
                    return s.isPayed() && !isExpenseOwner;
                })
                .map(s -> s.shareAmount().targetAmount())
                .reduce(new BigDecimal(0), BigDecimal::add);
    }
}