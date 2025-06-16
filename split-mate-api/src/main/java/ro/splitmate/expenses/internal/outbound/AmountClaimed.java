package ro.splitmate.expenses.internal.outbound;

import org.springframework.modulith.events.Externalized;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.expenses.internal.payments.InternalReferenceIdentifier;
import ro.splitmate.types.TargetAmount;

import java.math.BigDecimal;

@Externalized("expenses.AmountClaimed::#{internalReferenceIdentifier()}")
public record AmountClaimed(
        Long id,
        Long senderId,
        Long receiverId,
        BigDecimal amount) {
}

