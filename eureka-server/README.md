# Eureka Server

Service Discovery Server for UPI Microservices Architecture.

## Overview

This Eureka Server provides service registry and discovery capabilities for the UPI microservices ecosystem. All microservices register themselves with this server, and the API Gateway uses it to discover available service instances.

## Configuration

- **Port**: 8761
- **Mode**: Standalone (no clustering)
- **Dashboard**: Available at http://localhost:8761

## Running the Server

```bash
mvn spring-boot:run
```

## Features

- Service registration and discovery
- Health monitoring of registered services
- Web dashboard for monitoring registered services
- Automatic service deregistration for failed instances

## Registered Services

The following services will register with this Eureka Server:

- Account Service (port 8082)
- Transaction Service (port 8083)
- API Gateway (port 8080)
- User Service (port 8081)

## Startup Order

This service should be started **first** before any other microservices to ensure proper service registration.