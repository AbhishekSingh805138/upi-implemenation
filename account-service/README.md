# Account Service

Account Management Service for UPI Microservices Architecture.

## Overview

This service manages user bank accounts and balances in the UPI system. It provides REST APIs for account creation, balance management, and account queries.

## Configuration

- **Port**: 8082
- **Database**: H2 in-memory database
- **Service Discovery**: Registers with Eureka Server at localhost:8761

## Features

- Account creation with initial balance
- UPI ID generation and management
- Balance inquiry and updates
- Account validation
- H2 console for database inspection

## Database

- **URL**: jdbc:h2:mem:accountdb
- **Console**: Available at http://localhost:8082/h2-console
- **Username**: sa
- **Password**: password

## Running the Service

```bash
mvn spring-boot:run
```

## Dependencies

- Spring Boot Web
- Spring Data JPA
- H2 Database
- Eureka Client
- Validation

## Startup Requirements

1. Eureka Server must be running on port 8761
2. This service will register itself automatically with Eureka