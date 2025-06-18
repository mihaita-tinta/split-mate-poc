package ro.splitmate.payments.internal.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface JpaPaymentRepository extends CrudRepository<JpaPayment, Long> {
    List<JpaPayment> findByInternalReferenceIdAndSenderId(Long internalReferenceId, Long senderId);
    Optional<JpaPayment> findByIdAndSenderId(Long paymentId, Long senderId);
    Optional<JpaPayment> findByExternalPaymentIdAndSenderId(String externalId, Long senderId);
}
