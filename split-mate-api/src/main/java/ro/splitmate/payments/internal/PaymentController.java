package ro.splitmate.payments.internal;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.types.TargetAmount;
import ro.splitmate.payments.api.ExternalPaymentIdentifier;
import ro.splitmate.payments.api.InternalReferenceIdentifier;
import ro.splitmate.payments.api.PaymentIdentifier;

import java.util.List;


@RestController
class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentManagement payments;

    PaymentController(PaymentManagement payments) {
        this.payments = payments;
    }

    @GetMapping("/payments")
    public ListSharePayments list(@RequestParam InternalReferenceIdentifier internalReferenceId,
                                  CustomerIdentifier customerId) {
        List<PaymentDto> all = payments.findByInternalReferenceIdAndSenderId(internalReferenceId, customerId)
                .stream()
                .map(e -> new PaymentDto(e.getId(), e.getTargetAmount(), e.getStatus(), e.getExternalPaymentUrl()))
                .toList();
        return new ListSharePayments(all);
    }

    @PostMapping("/payments/{paymentId}")
    public PaymentDto initiate(@PathVariable PaymentIdentifier paymentId,
                               @Valid @RequestBody PaymentInitiateRequest initiateRequest,
                               CustomerIdentifier customerId) {
        return payments.initiate(paymentId, customerId, initiateRequest)
                .map(e -> new PaymentDto(
                        e.getId(),
                        e.getTargetAmount(),
                        e.getStatus(),
                        e.getExternalPaymentUrl()))
                .orElseThrow();
    }

    @GetMapping("/payments/returnurl")
    public ListSharePayments onReturnUrl(
            @RequestParam("id") ExternalPaymentIdentifier id,
            @RequestParam String signature,// TODO may validate this?
            CustomerIdentifier customerId
    ) {
        List<PaymentDto> all = payments.onReturn(id, customerId)
                .stream()
                .map(e -> new PaymentDto(e.getId(), e.getTargetAmount(), e.getStatus(), e.getExternalPaymentUrl()))
                .toList();
        return new ListSharePayments(all);
    }

    record ListSharePayments(List<PaymentDto> payments) {
    }

    record PaymentDto(
            @JsonUnwrapped PaymentIdentifier id,
            @JsonUnwrapped TargetAmount amount,
            @JsonUnwrapped Payment.Status status,
            String url) {
    }

    public static class PaymentInitiateRequest {
        @NotNull
        @Email
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

}
