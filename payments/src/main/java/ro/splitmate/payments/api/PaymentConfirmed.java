package ro.splitmate.payments.api;

import org.springframework.modulith.events.Externalized;
import ro.splitmate.types.CustomerIdentifier;
import ro.splitmate.types.TargetAmount;

@Externalized("payments.PaymentConfirmed")
public record PaymentConfirmed(
        InternalReferenceIdentifier internalReferenceIdentifier,
        CustomerIdentifier senderId,
        CustomerIdentifier receiverId,
        TargetAmount amount) {
}

