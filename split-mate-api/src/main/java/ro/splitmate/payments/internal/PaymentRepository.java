package ro.splitmate.payments.internal;

import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.payments.api.InternalReferenceIdentifier;
import ro.splitmate.types.TargetAmount;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    List<Payment> findByInternalReferenceIdAndSenderId(InternalReferenceIdentifier internalReferenceId, CustomerIdentifier customerId);
    Optional<Payment> findByPaymentIdAndSenderId(PaymentIdentifier paymentIdentifier, CustomerIdentifier customerId);
    Optional<Payment> findByExternalPaymentIdAndSenderId(ExternalPaymentIdentifier paymentIdentifier, CustomerIdentifier customerId);
    Payment create(InternalReferenceIdentifier internalReferenceId, CustomerIdentifier senderId, CustomerIdentifier receiverId, TargetAmount amount);
    Payment save(Payment payment);
}
