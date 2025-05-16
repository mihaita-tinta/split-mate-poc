package ro.splitmate.payments.api;

public record PaymentStatusUpdated(InternalReferenceIdentifier internalReferenceId, PaymentStatus status) {
}
