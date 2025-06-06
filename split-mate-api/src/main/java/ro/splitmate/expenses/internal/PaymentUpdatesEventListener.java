package ro.splitmate.expenses.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.splitmate.expenses.api.ExpenseSettled;
import ro.splitmate.expenses.internal.payments.PaymentConfirmed;

import java.io.IOException;

@Component
public class PaymentUpdatesEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentUpdatesEventListener.class);
    private final ExpenseRepository expenseRepository;
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper mapper;

    public PaymentUpdatesEventListener(ExpenseRepository expenseRepository, ApplicationEventPublisher publisher, ObjectMapper mapper) {
        this.expenseRepository = expenseRepository;
        this.publisher = publisher;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "payments.PaymentConfirmed", groupId = "expenses-123")
    @Transactional
    public void onPaymentConfirmed(ConsumerRecord<String, byte[]> event) throws IOException {
        var update = mapper.readValue(event.value(), PaymentConfirmed.class);
        log.info("onPaymentConfirmed - payment: {}", update);
        var expense = expenseRepository.findByShareId(
                        new ShareIdentifier(update.internalReferenceIdentifier().id()))
                .map(e -> expenseRepository.update(e.onPaymentConfirmed(update)));

        expense.ifPresent(e -> {
            if (e.settle()) {
                publisher.publishEvent(new ExpenseSettled(e.userId(), e.id(), e.title(), e.targetAmount()));
            }
        });

        log.info("onPaymentConfirmed - payment: {} finished", expense);
    }

    @ApplicationModuleListener
    public void onPaymentConfirmed(PaymentConfirmed update) {
        var expense = expenseRepository.findByShareId(
                        new ShareIdentifier(update.internalReferenceIdentifier().id()))
                        .map(e -> expenseRepository.update(e.onPaymentConfirmed(update)));

        expense.ifPresent(e -> {
            if (e.settle()) {
                publisher.publishEvent(new ExpenseSettled(e.userId(), e.id(), e.title(), e.targetAmount()));
            }
        });

        log.info("onPaymentConfirmed - expense: {}", expense);
    }

}
