package com.upi.utility.repository;

import com.upi.utility.entity.PaymentCategory;
import com.upi.utility.entity.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {

    Optional<ServiceProvider> findByProviderCode(String providerCode);

    List<ServiceProvider> findByCategory(PaymentCategory category);

    List<ServiceProvider> findByCategoryAndIsActiveTrue(PaymentCategory category);

    List<ServiceProvider> findByIsActiveTrue();
}
