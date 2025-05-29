package ro.splitmate.payments.api;

import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.types.TargetAmount;

public record PaymentConfirmed(
        InternalReferenceIdentifier internalReferenceIdentifier,
        CustomerIdentifier senderId,
        CustomerIdentifier receiverId,
        TargetAmount amount) {
}

