package com.upi.utility.config;

import com.upi.utility.entity.PaymentCategory;
import com.upi.utility.entity.RechargePlan;
import com.upi.utility.entity.ServiceProvider;
import com.upi.utility.repository.PaymentCategoryRepository;
import com.upi.utility.repository.RechargePlanRepository;
import com.upi.utility.repository.ServiceProviderRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data initialization component
 * Initializes default payment categories, service providers, and recharge plans
 * Runs once on application startup
 */
@Component
@Slf4j
public class DataInitializer {

    private final PaymentCategoryRepository paymentCategoryRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final RechargePlanRepository rechargePlanRepository;

    public DataInitializer(
            PaymentCategoryRepository paymentCategoryRepository,
            ServiceProviderRepository serviceProviderRepository,
            RechargePlanRepository rechargePlanRepository) {
        this.paymentCategoryRepository = paymentCategoryRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.rechargePlanRepository = rechargePlanRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeData() {
        log.info("=== Starting Data Initialization ===");
        
        try {
            // Initialize payment categories
            List<PaymentCategory> categories = initializePaymentCategories();
            
            // Initialize service providers for each category
            initializeServiceProviders(categories);
            
            // Initialize sample recharge plans
            initializeRechargePlans();
            
            log.info("=== Data Initialization Completed Successfully ===");
        } catch (Exception e) {
            log.error("Error during data initialization", e);
        }
    }

    /**
     * Initialize default payment categories
     */
    private List<PaymentCategory> initializePaymentCategories() {
        log.info("Initializing payment categories...");
        
        List<PaymentCategory> categories = new ArrayList<>();
        
        // Mobile Recharge
        categories.add(createCategoryIfNotExists(
                "MOBILE_RECHARGE", 
                "Mobile Recharge"));
        
        // DTH Recharge
        categories.add(createCategoryIfNotExists(
                "DTH_RECHARGE", 
                "DTH Recharge"));
        
        // Electricity Bill
        categories.add(createCategoryIfNotExists(
                "ELECTRICITY_BILL", 
                "Electricity Bill"));
        
        // Credit Card Bill
        categories.add(createCategoryIfNotExists(
                "CREDIT_CARD_BILL", 
                "Credit Card Bill"));
        
        // Insurance Premium
        categories.add(createCategoryIfNotExists(
                "INSURANCE_PREMIUM", 
                "Insurance Premium"));
        
        log.info("Initialized {} payment categories", categories.size());
        return categories;
    }

    /**
     * Create payment category if it doesn't exist
     */
    private PaymentCategory createCategoryIfNotExists(String name, String displayName) {
        return paymentCategoryRepository.findByName(name)
                .orElseGet(() -> {
                    PaymentCategory category = new PaymentCategory();
                    category.setName(name);
                    category.setDisplayName(displayName);
                    category.setIsActive(true);
                    PaymentCategory saved = paymentCategoryRepository.save(category);
                    log.info("Created payment category: {}", displayName);
                    return saved;
                });
    }

    /**
     * Initialize service providers for each category
     */
    private void initializeServiceProviders(List<PaymentCategory> categories) {
        log.info("Initializing service providers...");
        
        for (PaymentCategory category : categories) {
            switch (category.getName()) {
                case "MOBILE_RECHARGE":
                    initializeMobileOperators(category);
                    break;
                case "DTH_RECHARGE":
                    initializeDTHOperators(category);
                    break;
                case "ELECTRICITY_BILL":
                    initializeElectricityProviders(category);
                    break;
                case "CREDIT_CARD_BILL":
                    initializeCreditCardIssuers(category);
                    break;
                case "INSURANCE_PREMIUM":
                    initializeInsuranceProviders(category);
                    break;
            }
        }
        
        log.info("Service providers initialization completed");
    }

    /**
     * Initialize mobile operators
     */
    private void initializeMobileOperators(PaymentCategory category) {
        createProviderIfNotExists(category, "JIO", "Jio", "https://api.jio.com");
        createProviderIfNotExists(category, "AIRTEL", "Airtel", "https://api.airtel.com");
        createProviderIfNotExists(category, "VODAFONE", "Vodafone Idea", "https://api.vodafone.com");
        createProviderIfNotExists(category, "BSNL", "BSNL", "https://api.bsnl.com");
    }

    /**
     * Initialize DTH operators
     */
    private void initializeDTHOperators(PaymentCategory category) {
        createProviderIfNotExists(category, "DISH_TV", "Dish TV", "https://api.dishtv.com");
        createProviderIfNotExists(category, "D2H", "D2H", "https://api.d2h.com");
        createProviderIfNotExists(category, "TATA_SKY", "Tata Sky", "https://api.tatasky.com");
        createProviderIfNotExists(category, "AIRTEL_DTH", "Airtel Digital TV", "https://api.airteldth.com");
        createProviderIfNotExists(category, "SUN_DIRECT", "Sun Direct", "https://api.sundirect.com");
    }

    /**
     * Initialize electricity providers
     */
    private void initializeElectricityProviders(PaymentCategory category) {
        createProviderIfNotExists(category, "BESCOM", "BESCOM", "https://api.bescom.com");
        createProviderIfNotExists(category, "MSEDCL", "MSEDCL", "https://api.msedcl.com");
        createProviderIfNotExists(category, "TSNPDCL", "TSNPDCL", "https://api.tsnpdcl.com");
        createProviderIfNotExists(category, "APSPDCL", "APSPDCL", "https://api.apspdcl.com");
    }

    /**
     * Initialize credit card issuers
     */
    private void initializeCreditCardIssuers(PaymentCategory category) {
        createProviderIfNotExists(category, "HDFC", "HDFC Bank", "https://api.hdfcbank.com");
        createProviderIfNotExists(category, "ICICI", "ICICI Bank", "https://api.icicibank.com");
        createProviderIfNotExists(category, "SBI", "State Bank of India", "https://api.sbi.com");
        createProviderIfNotExists(category, "AXIS", "Axis Bank", "https://api.axisbank.com");
    }

    /**
     * Initialize insurance providers
     */
    private void initializeInsuranceProviders(PaymentCategory category) {
        createProviderIfNotExists(category, "LIC", "LIC of India", "https://api.licindia.com");
        createProviderIfNotExists(category, "HDFC_LIFE", "HDFC Life", "https://api.hdfclife.com");
        createProviderIfNotExists(category, "ICICI_PRU", "ICICI Prudential", "https://api.iciciprulife.com");
        createProviderIfNotExists(category, "SBI_LIFE", "SBI Life", "https://api.sbilife.com");
    }

    /**
     * Create service provider if it doesn't exist
     */
    private void createProviderIfNotExists(PaymentCategory category, String code, String name, String apiEndpoint) {
        serviceProviderRepository.findByProviderCode(code)
                .orElseGet(() -> {
                    ServiceProvider provider = new ServiceProvider();
                    provider.setCategory(category);
                    provider.setProviderCode(code);
                    provider.setProviderName(name);
                    provider.setApiEndpoint(apiEndpoint);
                    provider.setApiKeyEncrypted("MOCK_API_KEY"); // Mock API key for testing
                    provider.setIsActive(true);
                    ServiceProvider saved = serviceProviderRepository.save(provider);
                    log.info("Created service provider: {} ({})", name, code);
                    return saved;
                });
    }

    /**
     * Initialize sample recharge plans for testing
     */
    private void initializeRechargePlans() {
        log.info("Initializing sample recharge plans...");
        
        // Get mobile operators
        ServiceProvider jio = serviceProviderRepository.findByProviderCode("JIO").orElse(null);
        ServiceProvider airtel = serviceProviderRepository.findByProviderCode("AIRTEL").orElse(null);
        
        if (jio != null) {
            createPlanIfNotExists(jio, "JIO-99", "₹99 Plan", new BigDecimal("99.00"), 28, 
                    "Unlimited calls + 1GB/day data");
            createPlanIfNotExists(jio, "JIO-199", "₹199 Plan", new BigDecimal("199.00"), 28, 
                    "Unlimited calls + 2GB/day data");
            createPlanIfNotExists(jio, "JIO-299", "₹299 Plan", new BigDecimal("299.00"), 28, 
                    "Unlimited calls + 3GB/day data");
        }
        
        if (airtel != null) {
            createPlanIfNotExists(airtel, "AIRTEL-99", "₹99 Plan", new BigDecimal("99.00"), 28, 
                    "Unlimited calls + 1GB/day data");
            createPlanIfNotExists(airtel, "AIRTEL-199", "₹199 Plan", new BigDecimal("199.00"), 28, 
                    "Unlimited calls + 2GB/day data");
            createPlanIfNotExists(airtel, "AIRTEL-399", "₹399 Plan", new BigDecimal("399.00"), 56, 
                    "Unlimited calls + 2.5GB/day data");
        }
        
        // Get DTH operators
        ServiceProvider tataSky = serviceProviderRepository.findByProviderCode("TATA_SKY").orElse(null);
        
        if (tataSky != null) {
            createPlanIfNotExists(tataSky, "TATA-299", "Basic Pack", new BigDecimal("299.00"), 30, 
                    "150+ channels including HD");
            createPlanIfNotExists(tataSky, "TATA-499", "Premium Pack", new BigDecimal("499.00"), 30, 
                    "250+ channels including HD and sports");
        }
        
        log.info("Sample recharge plans initialization completed");
    }

    /**
     * Create recharge plan if it doesn't exist
     */
    private void createPlanIfNotExists(ServiceProvider provider, String planCode, String planName, 
                                      BigDecimal amount, Integer validityDays, String description) {
        rechargePlanRepository.findByProviderAndPlanCode(provider, planCode)
                .orElseGet(() -> {
                    RechargePlan plan = new RechargePlan();
                    plan.setProvider(provider);
                    plan.setPlanCode(planCode);
                    plan.setPlanName(planName);
                    plan.setAmount(amount);
                    plan.setValidityDays(validityDays);
                    plan.setDescription(description);
                    plan.setIsActive(true);
                    RechargePlan saved = rechargePlanRepository.save(plan);
                    log.debug("Created recharge plan: {} for {}", planName, provider.getProviderName());
                    return saved;
                });
    }
}
