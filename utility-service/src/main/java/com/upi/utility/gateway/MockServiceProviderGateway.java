package com.upi.utility.gateway;

import com.upi.utility.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
@ConditionalOnProperty(name = "provider-gateway.mock-mode", havingValue = "true", matchIfMissing = true)
@Slf4j
public class MockServiceProviderGateway implements ServiceProviderGateway {

    private static final long SIMULATED_DELAY_MS = 500;

    @Override
    public RechargeResponse processMobileRecharge(MobileRechargeRequest request) {
        log.info("Mock: Processing mobile recharge for {}", request.getMobileNumber());
        simulateDelay();
        
        return RechargeResponse.builder()
                .success(true)
                .transactionRef("MOCK-MOB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .message("Mobile recharge successful")
                .status("SUCCESS")
                .build();
    }

    @Override
    public List<RechargePlanResponse> getMobileRechargePlans(String operatorCode) {
        log.info("Mock: Fetching mobile recharge plans for operator {}", operatorCode);
        simulateDelay();
        
        return Arrays.asList(
                RechargePlanResponse.builder()
                        .planCode("PLAN-99")
                        .planName("₹99 Plan")
                        .amount(new BigDecimal("99.00"))
                        .validityDays(28)
                        .description("Unlimited calls + 1GB/day data")
                        .build(),
                RechargePlanResponse.builder()
                        .planCode("PLAN-199")
                        .planName("₹199 Plan")
                        .amount(new BigDecimal("199.00"))
                        .validityDays(28)
                        .description("Unlimited calls + 2GB/day data")
                        .build(),
                RechargePlanResponse.builder()
                        .planCode("PLAN-299")
                        .planName("₹299 Plan")
                        .amount(new BigDecimal("299.00"))
                        .validityDays(28)
                        .description("Unlimited calls + 3GB/day data")
                        .build()
        );
    }

    @Override
    public RechargeResponse processDTHRecharge(DTHRechargeRequest request) {
        log.info("Mock: Processing DTH recharge for subscriber {}", request.getSubscriberId());
        simulateDelay();
        
        return RechargeResponse.builder()
                .success(true)
                .transactionRef("MOCK-DTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .message("DTH recharge successful")
                .status("SUCCESS")
                .build();
    }

    @Override
    public List<RechargePlanResponse> getDTHRechargePlans(String operatorCode, String subscriberId) {
        log.info("Mock: Fetching DTH plans for operator {} and subscriber {}", operatorCode, subscriberId);
        simulateDelay();
        
        return Arrays.asList(
                RechargePlanResponse.builder()
                        .planCode("DTH-299")
                        .planName("Basic Pack")
                        .amount(new BigDecimal("299.00"))
                        .validityDays(30)
                        .description("150+ channels including HD")
                        .build(),
                RechargePlanResponse.builder()
                        .planCode("DTH-499")
                        .planName("Premium Pack")
                        .amount(new BigDecimal("499.00"))
                        .validityDays(30)
                        .description("250+ channels including HD and sports")
                        .build()
        );
    }

    @Override
    public BillDetails fetchElectricityBill(String providerCode, String consumerNumber) {
        log.info("Mock: Fetching electricity bill for consumer {}", consumerNumber);
        simulateDelay();
        
        Map<String, Object> additionalDetails = new HashMap<>();
        additionalDetails.put("units", "350 kWh");
        additionalDetails.put("ratePerUnit", "₹6.50");
        additionalDetails.put("fixedCharges", "₹100.00");
        
        return BillDetails.builder()
                .consumerNumber(consumerNumber)
                .consumerName("Mock Consumer")
                .amountDue(new BigDecimal("2375.00"))
                .dueDate(LocalDate.now().plusDays(7))
                .billingPeriod("November 2025")
                .additionalDetails(additionalDetails)
                .build();
    }

    @Override
    public PaymentResponse payElectricityBill(ElectricityBillPaymentRequest request) {
        log.info("Mock: Processing electricity bill payment for consumer {}", request.getConsumerNumber());
        simulateDelay();
        
        return PaymentResponse.builder()
                .success(true)
                .transactionRef("MOCK-ELEC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .message("Electricity bill payment successful")
                .status("SUCCESS")
                .build();
    }

    @Override
    public PaymentResponse payCreditCardBill(CreditCardPaymentRequest request) {
        log.info("Mock: Processing credit card bill payment for card ending {}", request.getCardLast4Digits());
        simulateDelay();
        
        return PaymentResponse.builder()
                .success(true)
                .transactionRef("MOCK-CC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .message("Credit card bill payment successful")
                .status("SUCCESS")
                .build();
    }

    @Override
    public PaymentResponse payInsurancePremium(InsurancePremiumRequest request) {
        log.info("Mock: Processing insurance premium payment for policy {}", request.getPolicyNumber());
        simulateDelay();
        
        return PaymentResponse.builder()
                .success(true)
                .transactionRef("MOCK-INS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .message("Insurance premium payment successful")
                .status("SUCCESS")
                .build();
    }

    @Override
    public boolean validateProvider(String providerCode) {
        log.info("Mock: Validating provider {}", providerCode);
        return true;
    }

    @Override
    public ProviderStatus getProviderStatus(String providerCode) {
        log.info("Mock: Getting status for provider {}", providerCode);
        
        return ProviderStatus.builder()
                .providerCode(providerCode)
                .available(true)
                .message("Provider is available")
                .build();
    }

    private void simulateDelay() {
        try {
            Thread.sleep(SIMULATED_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Simulated delay interrupted", e);
        }
    }
}
