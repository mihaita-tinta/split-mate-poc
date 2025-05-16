package ro.splitmate.payments.internal;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ro.splitmate.payments.api.PaymentIdentifier;

@Component
class PaymentIdConverter implements Converter<String, PaymentIdentifier> {
    @Override
    public PaymentIdentifier convert(String source) {
        return new PaymentIdentifier(Long.parseLong(source));
    }
}
