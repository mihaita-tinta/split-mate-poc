package ro.splitmate.mockbank;

import java.math.BigDecimal;

public class Requests {

    public record PaymentCreate(
            BigDecimal amount,
            Currency currency,
            String paymentId,
            String returnUrl,
            String description,
            String email,
            String providerId,
            String source_user_id
    ) {
        /**
         * Send:
         * <pre> {
         "amount": 11.0,
         "currency": "EUR",
         "paymentId": "119318-123-testz-0023",
         "returnUrl": "https://myshop.com/returnurl",
         "description":"sandbox testing 123213212",
         "email":"test@customer.noda",
         "providerId":"sparkasse_de",
         "source_user_id": "12312312"
         }
         </pre>
         */
        public static PaymentCreate of(BigDecimal amount,
                                String description,
                                String paymentReferenceId,
                                String email,
                                String source_user_id) {
            return new PaymentCreate(amount,
                    Currency.RON,
                    paymentReferenceId,
                    "http://localhost:8080/payments.html",
                    description,
                    email,
                    "sparkasse_de",
                    source_user_id
                    );
        }


    }

    enum Currency {
        RON
    }
}
