package ro.splitmate.payments.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ro.splitmate.payments.api.AmountClaimed;
import ro.splitmate.payments.api.InternalReferenceIdentifier;
import ro.splitmate.types.CustomerIdentifier;
import ro.splitmate.types.TargetAmount;

import java.io.IOException;

@Component
public class AmountClaimedEventListener {
    private static final Logger log = LoggerFactory.getLogger(AmountClaimedEventListener.class);
    private final PaymentRepository paymentRepository;
    private final ObjectMapper mapper;

    public AmountClaimedEventListener(PaymentRepository paymentRepository, ObjectMapper mapper) {
        this.paymentRepository = paymentRepository;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "expenses.AmountClaimed", groupId = "payments-123")
    public void onAmountClaimed(ConsumerRecord<String, byte[]> event) throws IOException {
        var amountClaimed = mapper.readValue(event.value(), AmountClaimed.class);
        log.info("onAmountClaimed -  {} ", amountClaimed);
        Payment payment = paymentRepository.create(
                new InternalReferenceIdentifier(amountClaimed.id()),
                new CustomerIdentifier(amountClaimed.senderId()),
                new CustomerIdentifier(amountClaimed.receiverId()),
                new TargetAmount(amountClaimed.amount()));
        log.info("onAmountClaimed -  created payment {} ", payment);

    }

}
