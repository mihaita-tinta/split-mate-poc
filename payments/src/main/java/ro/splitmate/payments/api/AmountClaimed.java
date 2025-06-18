package ro.splitmate.payments.api;

import ro.splitmate.types.CustomerIdentifier;
import ro.splitmate.types.TargetAmount;

import java.math.BigDecimal;

public record AmountClaimed(
        Long id,
        Long senderId,
        Long receiverId,
        BigDecimal amount) {
}

