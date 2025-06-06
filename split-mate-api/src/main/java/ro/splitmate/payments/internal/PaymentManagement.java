package ro.splitmate.payments.internal;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.payments.api.InternalReferenceIdentifier;
import ro.splitmate.payments.api.PaymentConfirmed;
import ro.splitmate.payments.internal.noda.PaymentApiClient;
import ro.splitmate.payments.internal.noda.Responses;

import java.util.List;
import java.util.Optional;

@Service
class PaymentManagement {
    private final PaymentRepository repository;
    private final ApplicationEventPublisher publisher;
    private final PaymentApiClient apiClient;

    PaymentManagement(PaymentRepository repository, ApplicationEventPublisher publisher, PaymentApiClient apiClient) {
        this.repository = repository;
        this.publisher = publisher;
        this.apiClient = apiClient;
    }

    public List<Payment> findByInternalReferenceIdAndSenderId(InternalReferenceIdentifier internalReferenceId, CustomerIdentifier customerId) {
        return repository.findByInternalReferenceIdAndSenderId(internalReferenceId, customerId);
    }

    @Transactional
    public Optional<Payment> initiate(PaymentIdentifier id, CustomerIdentifier customerId,
                                      PaymentController.PaymentInitiateRequest initiateRequest) {
        return repository.findByPaymentIdAndSenderId(id, customerId)
                // TODO initiate one time only?
                .map(p -> {
                    Responses.PaymentResponse response = apiClient.createPayment(initiateRequest.getEmail(), p);
                    p.onProviderUpdate(response);
                    return repository.save(p);// TODO publish PaymentInitiated
                });
    }

    @Transactional
    public Optional<Payment> onReturn(ExternalPaymentIdentifier id, CustomerIdentifier customerId) {
        return repository.findByExternalPaymentIdAndSenderId(id, customerId)
                // TODO initiate one time only?
                .map(payment -> {
                    Responses.PaymentResponse response = apiClient.getPayment(payment);
                    payment.onProviderUpdate(response);
                    if (payment.getStatus() == Payment.Status.PAID) {
                        publisher.publishEvent(new PaymentConfirmed(payment.getInternalReferenceId(),
                                payment.getSenderId(), payment.getReceiverId(), payment.getTargetAmount()));
                    }
                    return repository.save(payment);
                });
    }

}
