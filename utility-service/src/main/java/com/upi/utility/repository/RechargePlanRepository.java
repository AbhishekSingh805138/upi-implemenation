package com.upi.utility.repository;

import com.upi.utility.entity.RechargePlan;
import com.upi.utility.entity.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RechargePlanRepository extends JpaRepository<RechargePlan, Long> {

    List<RechargePlan> findByProviderAndIsActiveTrue(ServiceProvider provider);

    Optional<RechargePlan> findByProviderAndPlanCode(ServiceProvider provider, String planCode);

    List<RechargePlan> findByProvider(ServiceProvider provider);
}
