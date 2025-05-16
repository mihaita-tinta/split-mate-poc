package ro.splitmate.payouts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PayoutsEventListener {
    private static final Logger log = LoggerFactory.getLogger(PayoutsEventListener.class);

    @KafkaListener(topics = "expenses.AllMoneyArePaid", groupId = "payouts-123")
    public void onAllMoneyArePaid(ConsumerRecord<String, byte[]> event) throws IOException {
        var e = new ObjectMapper().readValue(event.value(), AllMoneyArePaid.class);
        log.info("onAllMoneyArePaid -  {} will be paid to {}", e.amount(), e.id());
    }

}
