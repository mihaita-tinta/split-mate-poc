package ro.splitmate.expenses.internal.jpa;

import org.springframework.stereotype.Service;
import ro.splitmate.customers.api.CustomerIdentifier;
import ro.splitmate.expenses.internal.ShareIdentifier;
import ro.splitmate.expenses.internal.Share;
import ro.splitmate.expenses.internal.ShareRepository;

import java.util.List;
import java.util.Optional;

@Service
class ShareRepositoryAdapter implements ShareRepository {
    private final JpaShareRepository repository;

    ShareRepositoryAdapter(JpaShareRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Share> findByUserId(CustomerIdentifier customerId) {
        return repository.findBySenderId(customerId.id())
                .stream()
                .map(JpaShare::toDomain)
                .toList();
    }

    @Override
    public Optional<Share> findById(ShareIdentifier id) {
        return repository.findById(id.id())
                .map(JpaShare::toDomain);
    }

//    @Override
//    public Share create(Expense expense, TargetAmount targetAmount, CustomerIdentifier senderId) {
//        JpaShare newShare = JpaShare.create(targetAmount, senderId, expense.userId(), expense.id());
//        return JpaShare.toDomain(repository.save(newShare));
//    }

    @Override
    public Share save(Share share) {
        return JpaShare.toDomain(repository.save(JpaShare.fromDomain(share)));
    }

}
