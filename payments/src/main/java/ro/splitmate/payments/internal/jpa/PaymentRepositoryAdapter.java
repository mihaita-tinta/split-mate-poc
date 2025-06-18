package ro.splitmate.payments.internal.jpa;

import org.springframework.stereotype.Service;
import ro.splitmate.types.CustomerIdentifier;
import ro.splitmate.payments.api.InternalReferenceIdentifier;
import ro.splitmate.payments.internal.ExternalPaymentIdentifier;
import ro.splitmate.payments.internal.Payment;
import ro.splitmate.payments.internal.PaymentIdentifier;
import ro.splitmate.payments.internal.PaymentRepository;
import ro.splitmate.types.TargetAmount;

import java.util.List;
import java.util.Optional;

@Service
class PaymentRepositoryAdapter implements PaymentRepository {
    private final JpaPaymentRepository repository;

    PaymentRepositoryAdapter(JpaPaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Payment> findByInternalReferenceIdAndSenderId(InternalReferenceIdentifier internalReferenceId, CustomerIdentifier customerId) {
        return repository.findByInternalReferenceIdAndSenderId(internalReferenceId.id(), customerId.id())
                .stream()
                .map(JpaPayment::toDomain)
                .toList();
    }

    @Override
    public Optional<Payment> findByPaymentIdAndSenderId(PaymentIdentifier paymentIdentifier, CustomerIdentifier customerId) {
        return repository.findByIdAndSenderId(paymentIdentifier.id(),
                        customerId.id())
                .map(JpaPayment::toDomain);
    }

    @Override
    public Optional<Payment> findByExternalPaymentIdAndSenderId(ExternalPaymentIdentifier paymentIdentifier, CustomerIdentifier customerId) {
        return repository.findByExternalPaymentIdAndSenderId(paymentIdentifier.id(),
                        customerId.id())
                .map(JpaPayment::toDomain);
    }

    @Override
    public Payment create(InternalReferenceIdentifier internalReferenceId, CustomerIdentifier senderId, CustomerIdentifier receiverId, TargetAmount amount) {
        Optional<JpaPayment> existing = repository
                .findByInternalReferenceIdAndSenderId(internalReferenceId.id(), senderId.id())
                .stream()
                .filter(JpaPayment::canBeUpdatedWithNewAmount)
                .findFirst();


        return JpaPayment.toDomain(
                repository.save(existing
                        .map(payment -> {
                            payment.withAmount(amount.targetAmount());
                            return payment;
                        }).orElseGet(() ->
                                JpaPayment.create(internalReferenceId,
                                        senderId,
                                        receiverId,
                                        amount)
                        )
                ));
    }

    @Override
    public Payment save(Payment payment) {
        return JpaPayment.toDomain(repository.save(JpaPayment.fromDomain(payment)));
    }

}
