package ro.splitmate.expenses.internal;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.expenses.api.ExpenseIdentifier;
import ro.splitmate.types.TargetAmount;
import ro.splitmate.types.Title;

import java.util.List;


@RestController
class ExpenseController {
    private static final Logger log = LoggerFactory.getLogger(ExpenseController.class);

    private final ExpenseManagement expenses;

    ExpenseController(ExpenseManagement expenses) {
        this.expenses = expenses;
    }


    @GetMapping("/expenses")
    public ListExpensesResponse list(CustomerIdentifier customerId) {
        List<ExpenseDto> all = expenses.list(customerId)
                .stream()
                .map(e -> new ExpenseDto(e.id(), e.title(), e.targetAmount()))
                .toList();
        return new ListExpensesResponse(all);
    }

    @GetMapping("/expenses/{expenseId}/shares")
    public ListExpenseShareResponse listShares(
            @PathVariable ExpenseIdentifier expenseId,
            CustomerIdentifier customerId) {
        List<ShareDto> all = expenses.shares(customerId, expenseId)
                .stream()
                .map(e -> new ShareDto(
                        e.id(),
                        e.sender(),
                        e.status(),
                        e.shareAmount()))
                .toList();
        return new ListExpenseShareResponse(all);
    }

    @PostMapping("/expenses")
    public ExpenseDto create(@RequestBody CreateExpenseRequest req,
                             CustomerIdentifier customerId) {
        var e = expenses.create(req, customerId);
        return new ExpenseDto(
                e.id(),
                e.title(),
                e.targetAmount());
    }

    @PutMapping("/expenses/{expenseId}/shares")
    public ShareDto claimShare(
                            @PathVariable ExpenseIdentifier expenseId,
                            @RequestBody ClaimShareRequest req,
                             CustomerIdentifier customerId) {
        var e = expenses.claimShare(expenseId, req, customerId);
        return new ShareDto(
                e.id(),
                e.sender(),
                e.status(),
                e.shareAmount());
    }

    record ListExpensesResponse(List<ExpenseDto> expenses) {
    }

    record ExpenseDto(
            @JsonUnwrapped ExpenseIdentifier id,
            @JsonUnwrapped Title title,
            @JsonUnwrapped TargetAmount targetAmount) {
    }

    public static class CreateExpenseRequest {
        @JsonUnwrapped // TODO not yet supported for records deserialization
        Title title;
        @JsonUnwrapped
        TargetAmount targetAmount;

        public Title getTitle() {
            return title;
        }

        public void setTitle(Title title) {
            this.title = title;
        }

        public TargetAmount getTargetAmount() {
            return targetAmount;
        }

        public void setTargetAmount(TargetAmount targetAmount) {
            this.targetAmount = targetAmount;
        }
    }

    public static class ClaimShareRequest {
        private @JsonUnwrapped TargetAmount targetAmount;

        public TargetAmount getTargetAmount() {
            return targetAmount;
        }

        public void setTargetAmount(TargetAmount targetAmount) {
            this.targetAmount = targetAmount;
        }
    }
    record ShareDto(
            @JsonUnwrapped ShareIdentifier id,
            @JsonUnwrapped(prefix = "sender") CustomerIdentifier sender,
            @JsonUnwrapped Share.Status status,
            @JsonUnwrapped TargetAmount targetAmount) {
    }
    record ListExpenseShareResponse(List<ShareDto> shares) {
    }

}
