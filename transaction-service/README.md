# Transaction Service

Transaction Processing Service for UPI Microservices Architecture.

## Overview

This service handles money transfers between accounts and maintains transaction history in the UPI system. It communicates with the Account Service to validate balances and process transactions.

## Configuration

- **Port**: 8083
- **Database**: H2 in-memory database
- **Service Discovery**: Registers with Eureka Server at localhost:8761
- **Account Service**: Communicates with account-service via service discovery

## Features

- Money transfer processing
- Transaction validation and status management
- Transaction history and filtering
- Inter-service communication with Account Service
- Transaction reference generation
- Balance verification before processing

## Database

- **URL**: jdbc:h2:mem:transactiondb
- **Console**: Available at http://localhost:8083/h2-console
- **Username**: sa
- **Password**: password

## Running the Service

```bash
mvn spring-boot:run
```

## Dependencies

- Spring Boot Web
- Spring Boot WebFlux (for WebClient)
- Spring Data JPA
- H2 Database
- Eureka Client
- Validation

## Startup Requirements

1. Eureka Server must be running on port 8761
2. Account Service should be running and registered with Eureka
3. This service will register itself automatically with Eureka