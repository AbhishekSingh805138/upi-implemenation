package com.upi.utility.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ServiceProviderGatewayFactory {

    private final ServiceProviderGateway mockGateway;
    
    @Value("${provider-gateway.mock-mode:true}")
    private boolean mockMode;

    public ServiceProviderGatewayFactory(ServiceProviderGateway mockGateway) {
        this.mockGateway = mockGateway;
    }

    /**
     * Get the appropriate gateway implementation based on provider code and configuration
     * 
     * @param providerCode The provider code
     * @return ServiceProviderGateway implementation
     */
    public ServiceProviderGateway getGateway(String providerCode) {
        if (mockMode) {
            log.debug("Using mock gateway for provider: {}", providerCode);
            return mockGateway;
        }
        
        // In production, this would return provider-specific implementations
        // For now, return mock gateway
        log.warn("Real provider gateway not implemented for: {}, using mock", providerCode);
        return mockGateway;
    }

    /**
     * Get the default gateway implementation
     * 
     * @return ServiceProviderGateway implementation
     */
    public ServiceProviderGateway getDefaultGateway() {
        return mockMode ? mockGateway : mockGateway;
    }

    public boolean isMockMode() {
        return mockMode;
    }
}
