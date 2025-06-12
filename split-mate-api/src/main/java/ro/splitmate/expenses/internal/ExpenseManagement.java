package ro.splitmate.expenses.internal;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.expenses.api.ExpenseCreated;
import ro.splitmate.expenses.api.ExpenseIdentifier;
import ro.splitmate.expenses.internal.payments.InternalReferenceIdentifier;
import ro.splitmate.expenses.internal.payments.PaymentOpened;

import java.util.List;
import java.util.Optional;

@Service
class ExpenseManagement {
    private final ExpenseRepository repository;
    private final ApplicationEventPublisher publisher;
    private final MyPublisher kafkaPublisher;

    ExpenseManagement(ExpenseRepository repository, ApplicationEventPublisher publisher, MyPublisher kafkaPublisher) {
        this.repository = repository;
        this.publisher = publisher;
        this.kafkaPublisher = kafkaPublisher;
    }

    public List<Expense> list(CustomerIdentifier customerId) {
        return repository.findByUserId(customerId);
    }

    public Expense create(ExpenseController.CreateExpenseRequest req, CustomerIdentifier customerId) {
        Expense expense = repository.create(req.getTitle(), req.getTargetAmount(), customerId);
        publisher.publishEvent(new ExpenseCreated(customerId, expense.id(), expense.title(), expense.targetAmount()));
        return expense;
    }

    public Optional<Expense> expense(CustomerIdentifier currentUserId,
                              ExpenseIdentifier expenseIdentifier) {
        return repository.findByUserIdAndId(currentUserId, expenseIdentifier);
    }

    /**
     * Find an expense by its id and a share code (for public/participant access).
     */
    public Optional<Expense> expenseByShareCode(ExpenseIdentifier expenseId, String shareCode) {
        // Find the expense by id
        Optional<Expense> expenseOpt = repository.findExpense(expenseId);
        // Check if the share code matches (assuming Expense has a getShareCode() or similar)
        return expenseOpt
                .filter(e -> e.shareCode().isPresent())
                .filter(e -> shareCode.equals(e.shareCode().get().value()));
    }

    public QrCode shareToOthers(CustomerIdentifier currentUserId,
                              ExpenseIdentifier expenseIdentifier) {
        var expense = repository.findByUserIdAndId(currentUserId, expenseIdentifier)
                .orElseThrow();
        expense = expense.shareToOthers(currentUserId);
        expense = repository.update(expense);
        String urlForParticipants = "http://localhost:8080/participant.html?expenseId="
                + expenseIdentifier.id() + "&shareCode=" + expense.shareCode().get().value();
        return new QrCode(urlForParticipants,
                "https://api.qrserver.com/v1/create-qr-code/?data=" + urlForParticipants);
    }

    @Transactional
    public Share claimShare(ExpenseIdentifier expenseId,
                            ExpenseController.ClaimShareRequest req,
                            CustomerIdentifier currentUserId) {
        Optional<Expense> expense = repository.findExpense(expenseId);
        return expense
                .filter(e -> {
                    if (currentUserId.equals(e.userId())) {
                        return true;
                    }
                    // TODO authorize claim using some code?
                    return e.shareCode().isPresent()
                    && e.shareCode().get().value().equals(req.getShareCode());
                })
                .map(e -> {
                    var updated = e.claim(req.getTargetAmount(), currentUserId);
                    Expense saved = repository.update(updated);
                    return saved.getShareNotPayedByUser(currentUserId)
                            .map(s -> {
                                if (s.shouldAllowNewPayments()) {
                                    kafkaPublisher.send(new PaymentOpened(
                                            new InternalReferenceIdentifier(s.id().id()),
                                            s.sender(),
                                            s.receiver(),
                                            s.shareAmount()));

//                                    publisher.publishEvent(new PaymentOpened(
//                                            new InternalReferenceIdentifier(s.id().id()),
//                                            s.sender(),
//                                            s.receiver(),
//                                            s.shareAmount()));
                                }
                                return s;
                            })
                            .orElseThrow();
                })
                .orElseThrow();
    }
}
