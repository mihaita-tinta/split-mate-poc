package ro.splitmate.expenses.internal;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.expenses.api.ExpenseIdentifier;
import ro.splitmate.types.TargetAmount;
import ro.splitmate.types.Title;

import java.math.BigDecimal;
import java.util.List;


@RestController
class ExpenseController {
    private static final Logger log = LoggerFactory.getLogger(ExpenseController.class);

    private final ExpenseManagement expenses;

    ExpenseController(ExpenseManagement expenses) {
        this.expenses = expenses;
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

    @PostMapping("/expenses/{expenseId}/qr")
    public ExpenseQRCode shareToOthers(
            @PathVariable ExpenseIdentifier expenseId,
            CustomerIdentifier customerId) {
        var e = expenses.shareToOthers(customerId, expenseId);
        return new ExpenseQRCode(e.url(), e.qrCode());
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
    public ResponseEntity<ListExpenseShareResponse> listShares(
            @PathVariable ExpenseIdentifier expenseId,
            CustomerIdentifier customerId,
            @RequestParam(value = "shareCode", required = false) String shareCode) {
        // If shareCode is present, allow access if it matches the expense's code
        if (shareCode != null && !shareCode.isBlank()) {
            return expenses.expenseByShareCode(expenseId, shareCode)
                    .map(e -> new ListExpenseShareResponse(e.title().title(),
                            e.targetAmount().targetAmount(),
                            e.shares()
                                    .stream()
                                    .map(s -> new ShareDto(
                                            s.id(),
                                            s.sender(),
                                            s.status(),
                                            s.shareAmount()))
                                    .toList()))
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
        // Default: only allow owner
        return expenses.expense(customerId, expenseId)
                .map(e -> new ListExpenseShareResponse(e.title().title(),
                        e.targetAmount().targetAmount(),
                        e.shares()
                                .stream()
                                .map(s -> new ShareDto(
                                        s.id(),
                                        s.sender(),
                                        s.status(),
                                        s.shareAmount()))
                                .toList()))
                .map(ResponseEntity :: ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/expenses/{expenseId}/shares")
    public ShareDto claimShare(
                            @PathVariable ExpenseIdentifier expenseId,
                            @RequestBody ClaimShareRequest req,
                             CustomerIdentifier customerId) {
        var share = expenses.claimShare(expenseId, req, customerId);
        return new ShareDto(
                share.id(),
                share.sender(),
                share.status(),
                share.shareAmount());
    }

    record ListExpensesResponse(List<ExpenseDto> expenses) {
    }

    record ExpenseDto(
            @JsonUnwrapped ExpenseIdentifier id,
            @JsonUnwrapped Title title,
            @JsonUnwrapped TargetAmount targetAmount) {
    }

    record ExpenseQRCode(String url, String qrCode  ) {
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
        private @JsonUnwrapped String shareCode;

        public TargetAmount getTargetAmount() {
            return targetAmount;
        }

        public String getShareCode() {
            return shareCode;
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
    record ListExpenseShareResponse(
            String title,
            BigDecimal targetAmount,
            List<ShareDto> shares) {
    }

}
