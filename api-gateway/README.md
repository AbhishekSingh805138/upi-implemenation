# API Gateway

Central API Gateway for UPI Microservices Architecture using Spring Cloud Gateway.

## Overview

This API Gateway serves as the single entry point for all client requests to the UPI microservices ecosystem. It provides routing, load balancing, and service discovery integration.

## Configuration

- **Port**: 8080
- **Service Discovery**: Integrates with Eureka Server at localhost:8761
- **Load Balancing**: Automatic load balancing via service discovery

## Features

- **Dynamic Routing**: Routes requests to appropriate microservices based on path patterns
- **Service Discovery Integration**: Automatically discovers and routes to available service instances
- **Load Balancing**: Built-in load balancing for multiple service instances
- **CORS Support**: Global CORS configuration for cross-origin requests
- **Health Monitoring**: Actuator endpoints for health checks and monitoring
- **Gateway Metrics**: Built-in metrics for monitoring gateway performance

## Route Configuration

The gateway automatically routes requests based on path patterns:

- `/api/accounts/**` → Account Service (port 8082)
- `/api/transactions/**` → Transaction Service (port 8083)
- `/api/users/**` → User Service (port 8081)

## Running the Gateway

```bash
mvn spring-boot:run
```

## Endpoints

### Health Check
- `GET /actuator/health` - Gateway health status
- `GET /actuator/gateway/routes` - View configured routes

### Service Routes
All microservice endpoints are accessible through the gateway:

- `http://localhost:8080/api/accounts/*` - Account operations
- `http://localhost:8080/api/transactions/*` - Transaction operations
- `http://localhost:8080/api/users/*` - User operations

## Dependencies

- Spring Cloud Gateway
- Eureka Client
- Spring Boot Actuator

## Startup Requirements

1. Eureka Server must be running on port 8761
2. Microservices should be registered with Eureka for routing to work
3. This gateway will register itself with Eureka

## Development Features

- **Service Discovery**: Automatically discovers services via Eureka
- **Dynamic Configuration**: Routes are configured via application.yml
- **Debugging**: Debug logging enabled for gateway operations
- **Monitoring**: Actuator endpoints for operational insights