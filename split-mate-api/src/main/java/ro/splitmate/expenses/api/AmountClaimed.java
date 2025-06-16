package ro.splitmate.expenses.api;

import org.springframework.modulith.events.Externalized;

import java.math.BigDecimal;

@Externalized("expenses.AmountClaimed::#{internalReferenceIdentifier()}")
public record AmountClaimed(
        Long id,
        Long senderId,
        Long receiverId,
        BigDecimal amount) {
}

