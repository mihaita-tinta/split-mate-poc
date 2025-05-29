package ro.splitmate.expenses.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ro.splitmate.expenses.api.AllMoneyArePaid;
import ro.splitmate.payments.api.PaymentStatusUpdated;

@Component
public class PaymentUpdatesEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentUpdatesEventListener.class);
    private final ShareRepository repository;
    private final ExpenseRepository expenseRepository;
    private final ApplicationEventPublisher publisher;

    public PaymentUpdatesEventListener(ShareRepository repository, ExpenseRepository expenseRepository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.expenseRepository = expenseRepository;
        this.publisher = publisher;
    }


    @ApplicationModuleListener
    public void onPaymentNewStatus(PaymentStatusUpdated update) {
        var expense = repository.findById(
                        new ShareIdentifier(update.internalReferenceId().id()))
                .map(s -> {
                    Share updatedShare = s.onPaymentUpdate(update.status());
                    return repository.save(updatedShare);
                })
                .flatMap(s -> expenseRepository.findExpense(s.expenseId())
                        .map(e -> {
                            e.withShareUpdated(s);
                            return expenseRepository.update(e);
                        }));

        expense.ifPresent(e -> {
            if (e.allSharesArePaid()) {
                publisher.publishEvent(new AllMoneyArePaid(e.userId(), e.id(), e.title(), e.targetAmount()));
            }
        });

        log.info("shareUpdateOnPaymentNewStatus - payment updated: {}", expense);
    }

}
