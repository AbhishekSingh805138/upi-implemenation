package com.upi.utility.gateway;

import com.upi.utility.dto.*;

import java.util.List;

public interface ServiceProviderGateway {

    // Mobile Recharge
    RechargeResponse processMobileRecharge(MobileRechargeRequest request);
    
    List<RechargePlanResponse> getMobileRechargePlans(String operatorCode);

    // DTH Recharge
    RechargeResponse processDTHRecharge(DTHRechargeRequest request);
    
    List<RechargePlanResponse> getDTHRechargePlans(String operatorCode, String subscriberId);

    // Bill Payments
    BillDetails fetchElectricityBill(String providerCode, String consumerNumber);
    
    PaymentResponse payElectricityBill(ElectricityBillPaymentRequest request);
    
    PaymentResponse payCreditCardBill(CreditCardPaymentRequest request);
    
    PaymentResponse payInsurancePremium(InsurancePremiumRequest request);

    // Provider Management
    boolean validateProvider(String providerCode);
    
    ProviderStatus getProviderStatus(String providerCode);
}
