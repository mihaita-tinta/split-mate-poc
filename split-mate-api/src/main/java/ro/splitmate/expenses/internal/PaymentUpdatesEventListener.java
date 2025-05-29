package ro.splitmate.expenses.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ro.splitmate.expenses.api.ExpenseSettled;
import ro.splitmate.payments.api.PaymentConfirmed;

@Component
public class PaymentUpdatesEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentUpdatesEventListener.class);
    private final ExpenseRepository expenseRepository;
    private final ApplicationEventPublisher publisher;

    public PaymentUpdatesEventListener(ExpenseRepository expenseRepository, ApplicationEventPublisher publisher) {
        this.expenseRepository = expenseRepository;
        this.publisher = publisher;
    }


    @ApplicationModuleListener
    public void onPaymentConfirmed(PaymentConfirmed update) {
        var expense = expenseRepository.findByShareId(
                        new ShareIdentifier(update.internalReferenceIdentifier().id()))
                        .map(e -> {
                            e.onPaymentConfirmed(update);
                            return expenseRepository.update(e);
                        });

        expense.ifPresent(e -> {
            if (e.settle()) {
                publisher.publishEvent(new ExpenseSettled(e.userId(), e.id(), e.title(), e.targetAmount()));
            }
        });

        log.info("onPaymentConfirmed - expense: {}", expense);
    }

}
