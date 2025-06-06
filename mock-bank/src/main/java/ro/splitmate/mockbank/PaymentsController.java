package ro.splitmate.mockbank;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class PaymentsController {

    private final Map<UUID, Payment> payments = new ConcurrentHashMap<>();

    @PutMapping("/api/payments")
    public PaymentResponse createPayment(@RequestBody Requests.PaymentCreate paymentCreate) throws InterruptedException {
        simulateRealWork();
        // Log the received payment request (for debugging purposes)
        System.out.println("Received payment request: " + paymentCreate);
        UUID internalReference = UUID.randomUUID();
        Payment payment = new Payment(internalReference, paymentCreate, PaymentStatus.New);
        payments.put(internalReference, payment);
        // Return a new PaymentStatus indicating the payment is being processed
        return new PaymentResponse(
                internalReference.toString(),
                "http://localhost:8081/?id=" + internalReference,
                "dummy-qr-code-for-" + internalReference,
                "dummy-token-for-" + internalReference,
                payment.request().description(),
                payment.request().amount(),
                payment.status
        ); // or any other status based on your logic
    }

    @GetMapping("/api/payments/{paymentId}")
    public PaymentResponse get(@PathVariable UUID paymentId) throws InterruptedException {
        simulateRealWork();
        Payment payment = payments.get(paymentId);
        return new PaymentResponse(
                paymentId.toString(),
                "http://localhost:8081/?id=" + paymentId,
                "dummy-qr-code-for-" + paymentId,
                "dummy-token-for-" + paymentId,
                payment.request().description(),
                payment.request().amount(),
                payment.status
        );
    }

    @PatchMapping("/api/payments/{paymentId}")
    public PaymentResponse updatePaymentStatus(@PathVariable UUID paymentId, @RequestBody UpdateStatusRequest req) throws InterruptedException {
        simulateRealWork();
        Payment payment = payments.get(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found");
        }
        PaymentStatus newStatus = req.status();
        Payment updated = new Payment(paymentId, payment.request(), newStatus);
        payments.put(paymentId, updated);
        return new PaymentResponse(
                paymentId.toString(),
                "http://localhost:8081/?id=" + paymentId,
                "dummy-qr-code-for-" + paymentId,
                "dummy-token-for-" + paymentId,
                payment.request().description(),
                payment.request().amount(),
                newStatus
        );
    }

    private static void simulateRealWork() throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextInt(500, 2001));
    }

    @GetMapping("/api/providers/top")
    public List<Bank> getTopBanks(@RequestParam String country, @RequestParam int count) throws InterruptedException {
        // Add random delay between 0.5 and 2 seconds
        simulateRealWork();
        // Return a static list of 3 banks for the mock
        return List.of(
            new Bank("catbank", "CatBank", "https://catbank.com/logo.png"),
            new Bank("dogbank", "DogBank", "https://dogbank.com/logo.png"),
            new Bank("foxbank", "FoxBank", "https://foxbank.com/logo.png")
        );
    }

    record Payment(UUID id, Requests.PaymentCreate request, PaymentStatus status) {
        // You can add more fields and methods as needed
    }

    public record PaymentResponse(String id,
                                  String url,
                                  String qrCode,
                                  String token,
                                  String description,
                                  BigDecimal amount,
                                  PaymentStatus status) {
    }

    public record Bank(String id, String name, String logoUrl) {}

    public record UpdateStatusRequest(PaymentStatus status) {}
}
