package ro.splitmate.expenses.internal;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.expenses.api.ExpenseCreated;
import ro.splitmate.expenses.api.ExpenseIdentifier;
import ro.splitmate.payments.api.InternalReferenceIdentifier;
import ro.splitmate.payments.api.PaymentOpened;

import java.util.List;
import java.util.Optional;

@Service
class ExpenseManagement {
    private final ExpenseRepository repository;
    private final ApplicationEventPublisher publisher;

    ExpenseManagement(ExpenseRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public List<Expense> list(CustomerIdentifier customerId) {
        return repository.findByUserId(customerId);
    }

    public Expense create(ExpenseController.CreateExpenseRequest req, CustomerIdentifier customerId) {
        Expense expense = repository.create(req.getTitle(), req.getTargetAmount(), customerId);
        publisher.publishEvent(new ExpenseCreated(customerId, expense.id(), expense.title(), expense.targetAmount()));
        return expense;
    }

    public List<Share> shares(CustomerIdentifier currentUserId,
                              ExpenseIdentifier expenseIdentifier) {
        return repository.findByUserIdAndId(currentUserId, expenseIdentifier)
                .map(Expense::shares)
                .orElse(List.of());
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
                    return e.id() != null;
                })
                .map(e -> {
                    var updated = e.claim(req.getTargetAmount(), currentUserId);
                    Expense saved = repository.update(updated);
                    var claimed = saved.getShareNotPayedByUser(currentUserId)
                            .map(s -> {
                                if (s.shouldAllowNewPayments()) {
                                    publisher.publishEvent(new PaymentOpened(
                                            new InternalReferenceIdentifier(s.id().id()),
                                            s.sender(),
                                            s.receiver(),
                                            s.shareAmount()));
                                }
                                return s;
                            })
                            .orElseThrow();

                    return claimed;
                })
                .orElseThrow();
    }
}
