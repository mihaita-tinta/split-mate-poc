package ro.splitmate.expenses.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ro.splitmate.expenses.internal.outbound.AmountClaimed;

import java.util.concurrent.CompletableFuture;

@Service
public class MyPublisher {
    private static final Logger log = LoggerFactory.getLogger(MyPublisher.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ObjectMapper mapper;

    public MyPublisher(KafkaTemplate<String, byte[]> kafkaTemplate, ObjectMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
    }

    @Async
    public CompletableFuture<Void> send(AmountClaimed message) {
        byte[] data = null;
        try {
            data = mapper.writeValueAsBytes(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return kafkaTemplate.send("expenses.AmountClaimed", message.toString(),
                            data)
                    .handle((res, ex) -> {
                        if (ex != null) {
                            log.error("send - failed to send message: {}", message, ex);
                        } else {
                            log.info("send - published message: {}", message);
                        }
                        return null;
                    });
    }
}
