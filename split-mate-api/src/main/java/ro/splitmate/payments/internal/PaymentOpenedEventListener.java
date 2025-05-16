package ro.splitmate.payments.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ro.splitmate.payments.api.PaymentOpened;

@Component
public class PaymentOpenedEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentOpenedEventListener.class);
    private final PaymentRepository paymentRepository;

    public PaymentOpenedEventListener(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    //    @Async
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    @TransactionalEventListener(fallbackExecution = true)
    @ApplicationModuleListener
    public void onShareClaimed(PaymentOpened paymentOpened) {
        Payment payment = paymentRepository.create(
                paymentOpened.internalReferenceIdentifier(),
                paymentOpened.senderId(),
                paymentOpened.receiverId(),
                paymentOpened.amount());
        log.info("onShareClaimed - payment created: {}", payment);
    }

}
