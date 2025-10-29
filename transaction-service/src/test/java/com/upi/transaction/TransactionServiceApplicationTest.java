package com.upi.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "eureka.client.enabled=false"
})
class TransactionServiceApplicationTest {

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
    }
}