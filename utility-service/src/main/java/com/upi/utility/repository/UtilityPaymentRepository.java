package com.upi.utility.repository;

import com.upi.utility.entity.PaymentCategory;
import com.upi.utility.entity.UtilityPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UtilityPaymentRepository extends JpaRepository<UtilityPayment, Long> {

    Optional<UtilityPayment> findByTransactionRef(String transactionRef);

    List<UtilityPayment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<UtilityPayment> findByUserIdAndCategoryOrderByCreatedAtDesc(Long userId, PaymentCategory category);

    List<UtilityPayment> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);

    List<UtilityPayment> findByUpiIdOrderByCreatedAtDesc(String upiId);
}
