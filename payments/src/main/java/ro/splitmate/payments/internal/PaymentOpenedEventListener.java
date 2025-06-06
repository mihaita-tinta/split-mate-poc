package ro.splitmate.payments.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ro.splitmate.payments.api.PaymentOpened;
import ro.splitmate.payouts.ExpenseSettled;

import java.io.IOException;

@Component
public class PaymentOpenedEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentOpenedEventListener.class);
    private final PaymentRepository paymentRepository;
    private final ObjectMapper mapper;

    public PaymentOpenedEventListener(PaymentRepository paymentRepository, ObjectMapper mapper) {
        this.paymentRepository = paymentRepository;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "payments.PaymentOpened", groupId = "payments-123")
    public void onPaymentOpened(ConsumerRecord<String, byte[]> event) throws IOException {
        var paymentOpened = mapper.readValue(event.value(), PaymentOpened.class);
        log.info("onPaymentOpened -  {} ", paymentOpened);
        Payment payment = paymentRepository.create(
                paymentOpened.internalReferenceIdentifier(),
                paymentOpened.senderId(),
                paymentOpened.receiverId(),
                paymentOpened.amount());
        log.info("onPaymentOpened -  created {} ", payment);

    }

}
