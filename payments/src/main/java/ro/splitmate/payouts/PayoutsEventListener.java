package ro.splitmate.payouts;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PayoutsEventListener {
    private static final Logger log = LoggerFactory.getLogger(PayoutsEventListener.class);

    private final ObjectMapper mapper;

    public PayoutsEventListener(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @KafkaListener(topics = "expenses.ExpenseSettled", groupId = "payouts-123")
    public void onExpenseSettled(ConsumerRecord<String, byte[]> event) throws IOException {
        var e = mapper.readValue(event.value(), ExpenseSettled.class);
        log.info("onExpenseSettled -  {} will be paid to {}", e.amount(), e.id());
    }

}
