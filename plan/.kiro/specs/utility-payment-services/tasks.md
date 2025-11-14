# Implementation Plan

- [x] 1. Set up Utility Service project structure and core configuration



  - Create utility-service Spring Boot project with Maven
  - Configure application.yml with port 8084, Eureka client, and H2 database
  - Add dependencies: Spring Web, Spring Data JPA, Eureka Client, WebClient, Lombok
  - Create main application class with @EnableEurekaClient annotation
  - Configure RestTemplate and WebClient beans with @LoadBalanced for service discovery
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1_

- [x] 2. Implement database schema and core entities


  - [x] 2.1 Create PaymentCategory entity and repository


    - Define PaymentCategory entity with fields: id, name, displayName, iconUrl, isActive
    - Create PaymentCategoryRepository interface extending JpaRepository
    - _Requirements: 1.1, 1.2_
  
  - [x] 2.2 Create ServiceProvider entity and repository


    - Define ServiceProvider entity with fields: id, categoryId, providerName, providerCode, apiEndpoint, apiKeyEncrypted, isActive
    - Create ServiceProviderRepository with custom queries for finding by code and category
    - _Requirements: 11.2, 11.3, 11.4_
  
  - [x] 2.3 Create UtilityPayment entity and repository


    - Define UtilityPayment entity with fields: id, userId, upiId, categoryId, providerId, accountIdentifier, amount, paymentStatus, transactionRef, providerTransactionRef, paymentDetails
    - Create UtilityPaymentRepository with queries for user history and filtering
    - Add indexes for userId and transactionRef
    - _Requirements: 7.1, 7.2, 7.3, 8.1_
  
  - [x] 2.4 Create SavedBiller entity and repository


    - Define SavedBiller entity with fields: id, userId, categoryId, providerId, accountIdentifier, nickname, accountHolderName
    - Create SavedBillerRepository with queries for user billers by category
    - Add unique constraint on user-category-provider-account combination
    - _Requirements: 9.1, 9.2, 9.3_
  
  - [x] 2.5 Create RechargePlan entity and repository


    - Define RechargePlan entity with fields: id, providerId, planCode, planName, amount, validityDays, description, isActive
    - Create RechargePlanRepository with queries for active plans by provider
    - _Requirements: 2.2, 5.2_

- [x] 3. Create DTOs and request/response models


  - [x] 3.1 Create payment request DTOs


    - MobileRechargeRequest: upiId, mobileNumber, operatorCode, amount, planCode
    - DTHRechargeRequest: upiId, subscriberId, operatorCode, amount, planCode
    - ElectricityBillPaymentRequest: upiId, providerCode, consumerNumber, amount, billingCycle
    - CreditCardPaymentRequest: upiId, issuerCode, cardLast4Digits, amount
    - InsurancePremiumRequest: upiId, providerCode, policyNumber, amount
    - _Requirements: 2.1, 3.1, 4.1, 5.1, 6.1_
  
  - [x] 3.2 Create response DTOs


    - UtilityPaymentResponse: transactionRef, providerTransactionRef, status, amount, message, timestamp, receiptDetails
    - BillDetails: consumerNumber, consumerName, amountDue, dueDate, billingPeriod, additionalDetails
    - BalanceResponse: balance, upiId (for Account Service integration)
    - PaymentCategoryResponse: id, name, displayName, iconUrl, isActive
    - _Requirements: 3.3, 8.2, 8.3, 1.2_
  
  - [x] 3.3 Create error response DTOs


    - UtilityErrorResponse: error, message, status, transactionRef, timestamp, details
    - _Requirements: 10.1, 10.2, 12.3_

- [x] 4. Implement service provider gateway interface and adapters


  - [x] 4.1 Create ServiceProviderGateway interface


    - Define methods: processMobileRecharge, processDTHRecharge, fetchElectricityBill, payElectricityBill, payCreditCardBill, payInsurancePremium
    - Define methods: getMobileRechargePlans, getDTHRechargePlans, validateProvider, getProviderStatus
    - _Requirements: 2.1, 3.1, 4.1, 5.1, 6.1_
  
  - [x] 4.2 Implement MockServiceProviderGateway for development


    - Create mock implementations returning simulated success responses
    - Add configurable delays to simulate real provider latency
    - Generate mock transaction references and bill details
    - _Requirements: 2.5, 3.3, 5.3, 6.3_
  
  - [x] 4.3 Create provider adapter factory


    - Implement factory pattern to select appropriate provider adapter based on provider code
    - Support switching between mock and real implementations via configuration
    - _Requirements: 11.3, 11.4_

- [x] 5. Implement Account Service client for balance operations

  - [x] 5.1 Create AccountServiceClient component


    - Implement getBalance method using WebClient with timeout and retry
    - Implement debitAmount method for balance deduction
    - Implement refundAmount method for payment failure rollback
    - Implement validateUpiId method to check UPI ID existence
    - Add error handling for account not found and insufficient balance scenarios
    - _Requirements: 2.3, 2.4, 3.5, 10.3, 12.2_

- [x] 6. Implement Transaction Service client for payment recording


  - [x] 6.1 Create TransactionServiceClient component


    - Implement recordUtilityPayment method using WebClient
    - Create UtilityPaymentRecord DTO with fields: upiId, providerName, amount, transactionRef, category
    - Add timeout configuration and error handling
    - _Requirements: 7.1, 8.1_

- [x] 7. Implement payment orchestration service


  - [x] 7.1 Create PaymentOrchestrationService


    - Implement processUtilityPayment method with @Transactional annotation
    - Step 1: Validate user account via AccountServiceClient
    - Step 2: Check balance sufficiency
    - Step 3: Validate service provider is active
    - Step 4: Create pending payment record in database
    - _Requirements: 2.3, 2.4, 3.2, 12.1, 12.2, 12.4_
  
  - [x] 7.2 Implement payment processing flow

    - Step 5: Debit user account via AccountServiceClient
    - Step 6: Process payment with ServiceProviderGateway
    - Step 7: Update payment status to COMPLETED with provider transaction ref
    - Step 8: Record payment in TransactionService
    - Return success response with transaction details
    - _Requirements: 2.5, 3.5, 6.4, 8.1, 8.2_
  

  - [x] 7.3 Implement payment failure handling and rollback



    - Catch exceptions during payment processing
    - Refund debited amount if payment fails after deduction
    - Update payment status to FAILED with failure reason
    - Log failure details for troubleshooting
    - Throw PaymentProcessingException with user-friendly message


    - _Requirements: 10.1, 10.2, 10.3, 10.5_



- [x] 8. Implement mobile recharge service


  - [x] 8.1 Create MobileRechargeService


    - Implement processMobileRecharge method
    - Validate mobile number format (10 digits)


    - Validate operator code exists and is active
    - Call PaymentOrchestrationService with mobile recharge details


    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_


  

  - [x] 8.2 Implement operator and plan management

    - Implement getMobileOperators method to fetch active operators
    - Implement getRechargePlans method to fetch plans by operator
    - Cache recharge plans in database for performance

    - _Requirements: 2.2_

- [x] 9. Implement DTH recharge service


  - [x] 9.1 Create DTHRechargeService


    - Implement processDTHRecharge method
    - Validate subscriber ID format
    - Validate DTH operator code exists and is active
    - Call PaymentOrchestrationService with DTH recharge details

    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_
  
  - [x] 9.2 Implement DTH operator and plan management

    - Implement getDTHOperators method to fetch active operators
    - Implement getDTHPlans method to fetch plans by operator and subscriber ID

    - Display plan details including validity and channels
    - _Requirements: 5.2, 5.3_

- [x] 10. Implement electricity bill payment service




  - [x] 10.1 Create ElectricityBillService

    - Implement fetchBillDetails method to retrieve bill from provider
    - Validate consumer number format
    - Display bill details: consumer name, amount due, due date, billing period
    - _Requirements: 3.1, 3.2, 3.3_

  


  - [x] 10.2 Implement electricity bill payment
    - Implement payElectricityBill method
    - Validate bill details before payment
    - Allow user confirmation before processing
    - Call PaymentOrchestrationService with electricity bill details
    - _Requirements: 3.4, 3.5_

- [x] 11. Implement credit card bill payment service


  - [x] 11.1 Create CreditCardBillService

    - Implement payCreditCardBill method
    - Validate card issuer code and card number format (last 4 digits)
    - Support partial and full payment amounts
    - Call PaymentOrchestrationService with credit card payment details
    - _Requirements: 4.1, 4.2, 4.3, 4.4_
  
  - [x] 11.2 Implement credit card issuer management

    - Implement getCreditCardIssuers method to fetch active issuers
    - Send payment confirmation to issuer via ServiceProviderGateway
    - _Requirements: 4.5_

- [x] 12. Implement insurance premium payment service


  - [x] 12.1 Create InsurancePremiumService


    - Implement payInsurancePremium method
    - Validate policy number with insurance provider
    - Display policy holder name, premium due date, and amount
    - Call PaymentOrchestrationService with insurance payment details
    - _Requirements: 6.1, 6.2, 6.3, 6.4_
  
  - [x] 12.2 Implement payment receipt generation

    - Generate payment receipt with transaction details after successful payment
    - Include policy number, premium amount, and payment date in receipt
    - _Requirements: 6.5_

- [x] 13. Implement saved biller management service


  - [x] 13.1 Create SavedBillerService


    - Implement saveBiller method to store biller details
    - Validate biller details before saving
    - Check for duplicate billers using unique constraint
    - _Requirements: 9.1, 9.2_
  

  - [x] 13.2 Implement biller retrieval and management

    - Implement getSavedBillers method to fetch user's saved billers
    - Implement getBillersByCategory method to filter by payment category
    - Implement updateBiller method to edit biller details
    - Implement deleteBiller method to remove saved biller
    - _Requirements: 9.3, 9.4_

  
  - [x] 13.3 Implement auto-populate functionality

    - When user selects saved biller, auto-fill payment form with stored details
    - _Requirements: 9.5_

- [x] 14. Implement payment history and receipt service


  - [x] 14.1 Create PaymentHistoryService


    - Implement getUserPayments method to fetch all utility payments for a user
    - Sort transactions by date in descending order
    - Display category, provider, amount, status, and timestamp for each payment
    - _Requirements: 7.1, 7.2, 7.3_

  
  - [x] 14.2 Implement payment filtering


    - Implement filterPaymentsByCategory method
    - Implement filterPaymentsByDateRange method

    - _Requirements: 7.4_
  

  - [x] 14.3 Implement payment details and receipt

    - Implement getPaymentDetails method to fetch complete transaction details
    - Implement generateReceipt method to create payment receipt
    - Include transaction ref, date, time, category, provider, amount, status in receipt
    - Provide download and share options for receipt
    - Store receipt for future access through transaction history
    - _Requirements: 7.5, 8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 15. Implement payment category management


  - [x] 15.1 Create PaymentCategoryService


    - Implement getAllCategories method to fetch all payment categories
    - Display category name, display name, and icon for each category
    - Filter to show only active categories to users
    - Mark unavailable categories with appropriate message
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 16. Implement service provider management (Admin)


  - [x] 16.1 Create ServiceProviderAdminService


    - Implement addServiceProvider method for admin to add new providers
    - Validate provider details: category, name, code, API endpoint
    - Encrypt API keys before storing in database
    - _Requirements: 11.1, 11.2_
  
  - [x] 16.2 Implement provider management operations

    - Implement getAllProviders method to list all service providers
    - Implement updateProvider method to edit provider details
    - Implement toggleProviderStatus method to enable/disable providers
    - Validate API connectivity before activating provider
    - Make newly activated providers available to users immediately
    - _Requirements: 11.3, 11.4, 11.5_

- [x] 17. Implement REST controllers


  - [x] 17.1 Create PaymentCategoryController


    - GET /api/utilities/categories - Get all payment categories
    - Map to PaymentCategoryService.getAllCategories
    - _Requirements: 1.1, 1.2_
  

  - [x] 17.2 Create MobileRechargeController

    - POST /api/utilities/recharge/mobile - Process mobile recharge
    - GET /api/utilities/recharge/mobile/operators - Get mobile operators
    - GET /api/utilities/recharge/mobile/plans/{operator} - Get recharge plans
    - Map to MobileRechargeService methods
    - _Requirements: 2.1, 2.2_

  


  - [x] 17.3 Create DTHRechargeController
    - POST /api/utilities/recharge/dth - Process DTH recharge
    - GET /api/utilities/recharge/dth/operators - Get DTH operators
    - GET /api/utilities/recharge/dth/plans/{operator}/{subscriberId} - Get DTH plans
    - Map to DTHRechargeService methods
    - _Requirements: 5.1, 5.2, 5.3_



  
  - [x] 17.4 Create BillPaymentController
    - POST /api/utilities/bills/electricity - Pay electricity bill
    - POST /api/utilities/bills/credit-card - Pay credit card bill
    - POST /api/utilities/bills/insurance - Pay insurance premium
    - GET /api/utilities/bills/electricity/providers - Get electricity providers
    - GET /api/utilities/bills/electricity/fetch - Fetch bill details
    - GET /api/utilities/bills/credit-card/issuers - Get credit card issuers
    - Map to respective service methods



    - _Requirements: 3.1, 3.3, 4.1, 6.1_
  
  - [x] 17.5 Create SavedBillerController
    - POST /api/utilities/billers - Save biller
    - GET /api/utilities/billers/{userId} - Get user's saved billers
    - GET /api/utilities/billers/{userId}/{category} - Get billers by category
    - PUT /api/utilities/billers/{id} - Update biller
    - DELETE /api/utilities/billers/{id} - Delete biller



    - Map to SavedBillerService methods
    - _Requirements: 9.1, 9.2, 9.3, 9.4_
  
  - [x] 17.6 Create PaymentHistoryController
    - GET /api/utilities/payments/{userId} - Get user's utility payments
    - GET /api/utilities/payments/{userId}/{category} - Get payments by category
    - GET /api/utilities/payments/transaction/{id} - Get payment details



    - POST /api/utilities/payments/{id}/receipt - Generate payment receipt
    - Map to PaymentHistoryService methods
    - _Requirements: 7.1, 7.3, 7.4, 7.5, 8.1, 8.4_
  
  - [x] 17.7 Create ServiceProviderAdminController

    - POST /api/utilities/admin/providers - Add service provider
    - GET /api/utilities/admin/providers - Get all providers
    - PUT /api/utilities/admin/providers/{id} - Update provider
    - PUT /api/utilities/admin/providers/{id}/status - Enable/disable provider
    - Map to ServiceProviderAdminService methods
    - _Requirements: 11.1, 11.2, 11.3, 11.5_

- [x] 18. Implement global exception handling


  - [x] 18.1 Create custom exceptions


    - InsufficientBalanceException for balance validation failures
    - ProviderUnavailableException for provider service issues
    - PaymentProcessingException for payment failures
    - InvalidAccountIdentifierException for validation errors
    - DuplicateTransactionException for idempotency violations
    - _Requirements: 10.1, 10.2, 12.3_
  
  - [x] 18.2 Create UtilityExceptionHandler with @ControllerAdvice

    - Handle InsufficientBalanceException - return HTTP 400 with balance error
    - Handle ProviderUnavailableException - return HTTP 503 with retry message
    - Handle PaymentProcessingException - return HTTP 500 with failure details
    - Handle InvalidAccountIdentifierException - return HTTP 400 with validation error
    - Handle DuplicateTransactionException - return HTTP 409 with duplicate error
    - Return UtilityErrorResponse with error code, message, and transaction ref
    - _Requirements: 10.1, 10.2, 10.5, 12.3, 12.4_

- [x] 19. Implement input validation


  - [x] 19.1 Add validation annotations to request DTOs


    - Add @NotNull, @NotBlank, @Pattern, @Min, @Max annotations
    - Validate mobile number format (10 digits)
    - Validate UPI ID format
    - Validate amount is positive and within limits
    - _Requirements: 12.1, 12.4_
  
  - [x] 19.2 Implement custom validators

    - Create UpiIdValidator to check UPI ID format and existence
    - Create AccountIdentifierValidator for provider-specific validation rules
    - Create ProviderCodeValidator to ensure provider exists and is active
    - _Requirements: 12.2, 12.3, 12.5_

- [x] 20. Update API Gateway configuration


  - [x] 20.1 Add utility-service route to API Gateway


    - Add route configuration for /api/utilities/** to lb://utility-service
    - Configure CORS settings for utility endpoints
    - Update gateway application.yml with new route
    - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1_

- [x] 21. Implement data initialization


  - [x] 21.1 Create data initialization component


    - Create @Component with @PostConstruct method
    - Initialize default payment categories (Mobile Recharge, DTH, Electricity, Credit Card, Insurance)
    - Initialize sample service providers for each category
    - Initialize sample recharge plans for testing
    - _Requirements: 1.1, 1.2, 11.2_

- [ ] 22. Create integration tests
  - [ ] 22.1 Test payment orchestration flow
    - Test successful mobile recharge end-to-end
    - Test successful electricity bill payment end-to-end
    - Test payment failure and rollback scenarios
    - Test insufficient balance handling
    - Test provider unavailability handling
    - _Requirements: 2.1, 3.1, 10.1, 10.3_
  
  - [ ] 22.2 Test inter-service communication
    - Test AccountServiceClient with WireMock
    - Test TransactionServiceClient with WireMock
    - Test retry and timeout mechanisms
    - _Requirements: 2.3, 3.5, 7.1_
  
  - [ ] 22.3 Test saved biller functionality
    - Test saving biller with duplicate detection
    - Test retrieving and filtering saved billers


    - Test updating and deleting billers


    - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [x] 23. Create Angular frontend components

  - [ ] 23.1 Create utility-services component
    - Display all payment categories with icons
    - Navigate to specific payment form on category selection
    - _Requirements: 1.1, 1.2, 1.3_
  

  - [ ] 23.2 Create mobile-recharge component
    - Form with mobile number, operator selection, and amount/plan selection
    - Fetch and display recharge plans
    - Submit recharge request and display confirmation
    - _Requirements: 2.1, 2.2, 2.5_
  

  - [ ] 23.3 Create bill-payment component
    - Reusable component for electricity, credit card, and insurance payments
    - Fetch bill details before payment
    - Display bill information for user confirmation
    - Submit payment and display receipt
    - _Requirements: 3.1, 3.3, 3.4, 4.1, 6.1_

  
  - [ ] 23.4 Create saved-billers component
    - Display user's saved billers grouped by category
    - Quick pay option for saved billers
    - Edit and delete biller functionality
    - _Requirements: 9.2, 9.3, 9.4, 9.5_
  
  - [ ] 23.5 Create payment-history component
    - Display utility payment history in table format
    - Filter by category and date range

    - View payment details and download receipt


    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 8.4_

- [ ] 24. Create Angular services
  - [x] 24.1 Create UtilityService

    - Implement methods for all utility payment APIs
    - Handle HTTP requests with proper error handling
    - Implement retry logic for failed requests

    - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1_


  
  - [ ] 24.2 Create SavedBillerService
    - Implement methods for saved biller management

    - Cache saved billers for performance
    - _Requirements: 9.1, 9.2, 9.3, 9.4_
  
  - [ ] 24.3 Create PaymentHistoryService
    - Implement methods for fetching payment history

    - Implement filtering and sorting logic
    - _Requirements: 7.1, 7.3, 7.4_

- [ ] 25. Update navigation and routing
  - [ ] 25.1 Add utility services to main navigation
    - Add "Utility Services" menu item in navigation bar
    - Create routes for all utility components
    - _Requirements: 1.1_
  
  - [ ] 25.2 Configure Angular routing
    - Add routes: /utilities, /utilities/mobile-recharge, /utilities/dth-recharge, /utilities/bills, /utilities/saved-billers, /utilities/history
    - Implement route guards for authenticated users
    - _Requirements: 1.1, 2.1, 3.1, 5.1, 7.1, 9.2_
