package ro.splitmate.expenses.internal.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.types.CreationTime;
import ro.splitmate.expenses.api.ExpenseIdentifier;
import ro.splitmate.expenses.internal.ShareIdentifier;
import ro.splitmate.types.TargetAmount;
import ro.splitmate.expenses.internal.Share;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;


@Entity
@Table(name = "shares")
public class JpaShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long expenseId;
    @NotNull
    private Long senderId;
    private Long receiverId;
    @Enumerated
    private Share.Status status;
    private BigDecimal targetAmount;
    private Timestamp creationDate;

    public Timestamp getCreationDate() {
        return creationDate;
    }


    static Share toDomain(JpaShare jpa) {
        return new Share(
                new ShareIdentifier(jpa.id),
                new ExpenseIdentifier(jpa.expenseId),
                new CustomerIdentifier(jpa.senderId),
                new CustomerIdentifier(jpa.receiverId),
                jpa.status,
                new TargetAmount(jpa.targetAmount),
                new CreationTime(jpa.getCreationDate().toLocalDateTime()));
    }

    static JpaShare fromDomain(Share e) {
        JpaShare jpa = new JpaShare();
        jpa.id = e.id() != null ? e.id().id() : null;
        jpa.expenseId = e.expenseId().id();
        jpa.senderId = e.sender().id();
        jpa.receiverId = e.receiver().id();
        jpa.status = e.status();
        jpa.targetAmount = e.shareAmount().targetAmount();
        jpa.creationDate = Timestamp.from(e.creationDate().creationTime().toInstant(ZoneOffset.UTC));
        return jpa;
    }
    static JpaShare create(TargetAmount targetAmount,
                           CustomerIdentifier sender,
                           CustomerIdentifier receiver,
                           ExpenseIdentifier expenseId) {
        JpaShare newUser = new JpaShare();
        newUser.creationDate = Timestamp.from(Instant.now());
        newUser.expenseId = expenseId.id();
        newUser.senderId = sender.id();
        newUser.receiverId = receiver.id();
        newUser.status = Share.Status.ACCEPTED_TO_PAY;
        newUser.targetAmount = targetAmount.targetAmount();
        return newUser;
    }
}
