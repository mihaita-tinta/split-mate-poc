package ro.splitmate.expenses.internal;

import ro.splitmate.customers.api.CustomerIdentifier;

import java.util.List;
import java.util.Optional;

public interface ShareRepository {

    List<Share> findByUserId(CustomerIdentifier customerId);

    Optional<Share> findById(ShareIdentifier expenseIdentifier);

//    Share create(Expense expense, TargetAmount targetAmount, CustomerIdentifier senderId);

    Share save(Share share);

}
