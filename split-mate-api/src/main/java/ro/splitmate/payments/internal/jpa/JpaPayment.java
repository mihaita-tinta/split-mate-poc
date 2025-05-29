package ro.splitmate.payments.internal.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.payments.api.InternalReferenceIdentifier;
import ro.splitmate.payments.internal.noda.PaymentStatus;
import ro.splitmate.payments.internal.Payment;
import ro.splitmate.types.CreationTime;
import ro.splitmate.payments.internal.ExternalPaymentIdentifier;
import ro.splitmate.payments.internal.PaymentIdentifier;
import ro.splitmate.types.TargetAmount;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;


@Entity
@Table(name = "payments")
public class JpaPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String externalPaymentId;
    @Column(length = 4096)
    private String externalPaymentUrl;
    @Enumerated
    private PaymentStatus externalPaymentStatus;
    @NotNull
    private Long internalReferenceId;
    private Long senderId;
    private Long receiverId;
    private BigDecimal targetAmount;
    private Timestamp creationDate;
    @Enumerated
    private Payment.Status status;

    public Timestamp getCreationDate() {
        return creationDate;
    }


    public boolean canBeUpdatedWithNewAmount() {
        return status == Payment.Status.WAITING;
    }
    public void withAmount(BigDecimal amount) {
        this.targetAmount = amount;
    }
    static Payment toDomain(JpaPayment jpa) {
        return new Payment(
                new PaymentIdentifier(jpa.id),
                new InternalReferenceIdentifier(jpa.internalReferenceId),
                new CustomerIdentifier(jpa.senderId),
                new CustomerIdentifier(jpa.receiverId),
                new TargetAmount(jpa.targetAmount),
                new CreationTime(jpa.getCreationDate().toLocalDateTime()),
                jpa.status,
                new ExternalPaymentIdentifier(jpa.externalPaymentId),
                jpa.externalPaymentUrl,
                jpa.externalPaymentStatus
        );
    }

    static JpaPayment fromDomain(Payment e) {
        JpaPayment jpa = new JpaPayment();
        jpa.targetAmount = e.getTargetAmount().targetAmount();
        jpa.internalReferenceId = e.getInternalReferenceId().id();
        jpa.id = e.getId().id();
        jpa.senderId = e.getSenderId().id();
        jpa.receiverId = e.getReceiverId().id();
        jpa.creationDate = Timestamp.from(e.getCreationDate().creationTime().toInstant(ZoneOffset.UTC));
        jpa.status = e.getStatus();
        jpa.externalPaymentId = e.getExternalPaymentId().id();
        jpa.externalPaymentUrl = e.getExternalPaymentUrl();
        jpa.externalPaymentStatus = e.getExternalPaymentStatus();
        return jpa;
    }

    static JpaPayment create(InternalReferenceIdentifier internalReferenceId,
                             CustomerIdentifier senderId,
                             CustomerIdentifier receiverId,
                             TargetAmount targetAmount) {
        JpaPayment payment = new JpaPayment();
        payment.creationDate = Timestamp.from(Instant.now());
        payment.internalReferenceId = internalReferenceId.id();
        payment.senderId = senderId.id();
        payment.receiverId = receiverId.id();
        payment.targetAmount = targetAmount.targetAmount();
        payment.status = Payment.Status.WAITING;
        return payment;
    }
}
