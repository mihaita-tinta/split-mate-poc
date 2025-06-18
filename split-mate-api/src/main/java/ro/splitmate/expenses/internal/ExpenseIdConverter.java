package ro.splitmate.expenses.internal;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ro.splitmate.expenses.api.ExpenseIdentifier;

@Component
class ExpenseIdConverter implements Converter<String, ExpenseIdentifier> {
    @Override
    public ExpenseIdentifier convert(String source) {
        return new ExpenseIdentifier(Long.parseLong(source));
    }
}
