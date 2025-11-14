package com.upi.utility.repository;

import com.upi.utility.entity.PaymentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentCategoryRepository extends JpaRepository<PaymentCategory, Long> {

    Optional<PaymentCategory> findByName(String name);

    List<PaymentCategory> findByIsActiveTrue();
}
