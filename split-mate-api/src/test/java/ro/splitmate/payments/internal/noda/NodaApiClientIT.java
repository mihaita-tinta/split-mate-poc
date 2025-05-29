package ro.splitmate.payments.internal.noda;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.payments.api.InternalReferenceIdentifier;
import ro.splitmate.payments.internal.Payment;
import ro.splitmate.types.CreationTime;
import ro.splitmate.payments.internal.ExternalPaymentIdentifier;
import ro.splitmate.payments.internal.PaymentIdentifier;
import ro.splitmate.types.TargetAmount;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Calling real noda sandbox. You need to add your own apikey: -Dnoda.server.api.key=xxxxxx-xxx-xx-xx-xxxx to work
 */
@Disabled
@SpringBootTest
class NodaApiClientIT {
    private static final Logger log = LoggerFactory.getLogger(NodaApiClientIT.class);

    @Autowired
    NodaApiClient noda;

    @Test
    void testNodaCalls() {
        List<Responses.Bank> banks = noda.getBanks();
        banks.forEach(bank -> log.info("Bank: {}", bank));

        String email = "junit@testing.dummy";
        var payment = new Payment(
                new PaymentIdentifier(1L),
                new InternalReferenceIdentifier(100L),
                new CustomerIdentifier(1000L),
                new CustomerIdentifier(1001L),
                new TargetAmount(new BigDecimal(19.99)),
                new CreationTime(LocalDateTime.now()),
                Payment.Status.WAITING,
                new ExternalPaymentIdentifier("46f5a1c3-d9bd-49c6-9b5c-03b80d889f6f"),
                null,
                null

        );
        var nodaPayment = noda.createPayment(email, payment);
        log.info("nodaPayment - created: {}", nodaPayment);
        payment.onProviderUpdate(nodaPayment);
        var get = noda.getPayment(payment);
        payment.onProviderUpdate(get);
        log.info("nodaPayment - get: {}", get);
    }

}