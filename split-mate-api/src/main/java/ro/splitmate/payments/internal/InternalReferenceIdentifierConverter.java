package ro.splitmate.payments.internal;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ro.splitmate.payments.api.InternalReferenceIdentifier;

@Component
class InternalReferenceIdentifierConverter implements Converter<String, InternalReferenceIdentifier> {
    @Override
    public InternalReferenceIdentifier convert(String source) {
        return new InternalReferenceIdentifier(Long.parseLong(source));
    }
}
