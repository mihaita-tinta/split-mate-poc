package ro.splitmate.expenses.internal.payments;

import org.springframework.modulith.events.Externalized;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.types.TargetAmount;

@Externalized("payments.PaymentOpened::#{internalReferenceIdentifier()}")
public record PaymentOpened (
        InternalReferenceIdentifier internalReferenceIdentifier,
        CustomerIdentifier senderId,
        CustomerIdentifier receiverId,
        TargetAmount amount) {
}

