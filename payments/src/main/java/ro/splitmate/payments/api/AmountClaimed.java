package ro.splitmate.payments.api;

import ro.splitmate.types.CustomerIdentifier;
import ro.splitmate.types.TargetAmount;

public record AmountClaimed(
        InternalReferenceIdentifier internalReferenceIdentifier,
        CustomerIdentifier senderId,
        CustomerIdentifier receiverId,
        TargetAmount amount) {
}

