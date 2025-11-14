package com.upi.utility.repository;

import com.upi.utility.entity.PaymentCategory;
import com.upi.utility.entity.SavedBiller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedBillerRepository extends JpaRepository<SavedBiller, Long> {

    List<SavedBiller> findByUserId(Long userId);

    List<SavedBiller> findByUserIdAndCategory(Long userId, PaymentCategory category);

    boolean existsByUserIdAndCategoryAndProviderIdAndAccountIdentifier(
            Long userId, PaymentCategory category, Long providerId, String accountIdentifier);
}
