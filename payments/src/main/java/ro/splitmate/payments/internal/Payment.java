package ro.splitmate.payments.internal;

import ro.splitmate.types.CustomerIdentifier;
import ro.splitmate.payments.api.InternalReferenceIdentifier;
import ro.splitmate.payments.internal.noda.PaymentStatus;
import ro.splitmate.payments.internal.noda.Responses;
import ro.splitmate.types.CreationTime;
import ro.splitmate.types.TargetAmount;

public class Payment {
    private PaymentIdentifier id;
    private InternalReferenceIdentifier internalReferenceId;
    private CustomerIdentifier senderId;
    private CustomerIdentifier receiverId;
    private TargetAmount targetAmount;
    private CreationTime creationDate;
    private Status status;
    private ExternalPaymentIdentifier externalPaymentId;
    private String externalPaymentUrl;
    private PaymentStatus externalPaymentStatus;

    public Payment(
            PaymentIdentifier id,
            InternalReferenceIdentifier internalReferenceId,
            CustomerIdentifier senderId,
            CustomerIdentifier receiverId,
            TargetAmount targetAmount,
            CreationTime creationDate,
            Status status,
            ExternalPaymentIdentifier externalPaymentId,
            String externalPaymentUrl,
            PaymentStatus externalPaymentStatus) {
        this.id = id;
        this.internalReferenceId = internalReferenceId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.targetAmount = targetAmount;
        this.creationDate = creationDate;
        this.status = status;
        this.externalPaymentId = externalPaymentId;
        this.externalPaymentUrl = externalPaymentUrl;
        this.externalPaymentStatus = externalPaymentStatus;
    }

    public PaymentIdentifier getId() {
        return id;
    }

    public InternalReferenceIdentifier getInternalReferenceId() {
        return internalReferenceId;
    }

    public CustomerIdentifier getSenderId() {
        return senderId;
    }

    public CustomerIdentifier getReceiverId() {
        return receiverId;
    }

    public TargetAmount getTargetAmount() {
        return targetAmount;
    }

    public CreationTime getCreationDate() {
        return creationDate;
    }

    public ExternalPaymentIdentifier getExternalPaymentId() {
        return externalPaymentId;
    }

    public String getExternalPaymentUrl() {
        return externalPaymentUrl;
    }

    public PaymentStatus getExternalPaymentStatus() {
        return externalPaymentStatus;
    }

    public Status getStatus() {
        return status;
    }

    public void onProviderUpdate(Responses.PaymentResponse providerResponse) {

        this.externalPaymentId = new ExternalPaymentIdentifier(providerResponse.id());
        this.externalPaymentStatus = providerResponse.status();
        if (providerResponse.url() != null) {
            this.externalPaymentUrl = providerResponse.url();
        }
        switch (providerResponse.status()) {
            case New, Processing -> {
                this.status = Status.INITIATED;
            }
            case Failed -> {
                this.status = Status.REJECTED;
            }
            case Done -> {
                this.status = Status.PAID;
            }
        }
    }

    public enum Status {
        WAITING,
        INITIATED,
        REJECTED,
        PAID
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", targetAmount=" + targetAmount +
                ", status=" + status +
                '}';
    }
}
