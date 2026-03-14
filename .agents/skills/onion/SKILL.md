---
name: onion
description: 'Guidelines for implementing Onion Architecture pattern with clean architecture principles'
applyTo: '**/*.java'
---

# Onion Architecture Guidelines

> **Note:** This guide focuses on architectural patterns and layer organization. 

## Overview

Onion Architecture is a clean architecture pattern that emphasizes separation of concerns and dependency inversion. It ensures that business logic remains independent of frameworks, databases, and external systems.

---

## Architecture Layers

```
┌─────────────────────────────────────────┐
│         Infrastructure Layer            │  ← Frameworks, DB, External APIs
│  (Repositories, REST Clients, JPA)      │
├─────────────────────────────────────────┤
│        Application Layer                │  ← Use Cases, Orchestration
│  (Services, DTOs, Mappers)              │
├─────────────────────────────────────────┤
│          Domain Layer                   │  ← Business Logic, Entities
│  (Domain Models, Business Rules)        │
└─────────────────────────────────────────┘
         ↑ Dependencies point inward ↑
```

**Core Principle:** Dependencies point INWARD. The Domain layer has NO dependencies on outer layers.

---

## Layer Responsibilities

### 1. Domain Layer (Core - Innermost)

**What belongs here:**
- Domain entities (pure business objects, NOT JPA entities)
- Business logic and validation rules
- Domain events
- Value objects
- Domain exceptions
- Repository interfaces (contracts)

**What does NOT belong:**
- Database annotations (`@Entity`, `@Table`, `@Column`)
- REST annotations (`@Path`, `@GET`, `@POST`)
- Framework dependencies (Quarkus, Spring, etc.)
- DTOs for API requests/responses
- Transaction management (`@Transactional`)

**Example:**

```java
// domain/model/Order.java
package com.example.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItem> items = new ArrayList<>();

    // Business logic methods (NOT just getters/setters!)
    public void addItem(OrderItem item) {
        if (status != OrderStatus.DRAFT) {
            throw new OrderNotEditableException("Cannot add items to " + status + " order");
        }
        items.add(item);
        recalculateTotal();
    }

    public void submit() {
        if (items.isEmpty()) {
            throw new EmptyOrderException("Cannot submit order without items");
        }
        if (status != OrderStatus.DRAFT) {
            throw new InvalidOrderStateException("Order already submitted");
        }
        this.status = OrderStatus.SUBMITTED;
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.DRAFT || status == OrderStatus.SUBMITTED;
    }

    private void recalculateTotal() {
        totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getters only - mutations through business methods
    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
}
```

```java
// domain/repository/OrderRepository.java (interface only)
package com.example.domain.repository;

import com.example.domain.model.Order;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    void delete(Long id);
}
```

---

### 2. Application Layer (Orchestration)

**What belongs here:**
- Service interfaces and implementations
- Use case orchestration
- Transaction boundaries (`@Transactional`)
- DTO definitions (requests and responses)
- Mappers (Domain ↔ DTO)
- Input validation

**Responsibilities:**
- Coordinates domain objects to fulfill use cases
- Manages transaction boundaries
- Transforms DTOs to domain models and back
- Calls repositories to persist/retrieve domain objects

**Example:**

```java
// application/service/OrderService.java
package com.example.application.service;

import com.example.application.dto.CreateOrderRequest;
import com.example.application.dto.OrderResponse;
import com.example.application.mapper.OrderMapper;
import com.example.domain.model.Order;
import com.example.domain.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderService {

    @Inject
    OrderRepository orderRepository;

    @Inject
    OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. Map DTO to domain model
        Order order = orderMapper.toDomain(request);

        // 2. Apply business logic
        request.getItems().forEach(itemDto -> {
            var item = orderMapper.toOrderItem(itemDto);
            order.addItem(item); // Domain method handles validation
        });

        // 3. Persist
        Order savedOrder = orderRepository.save(order);

        // 4. Map to response DTO
        return orderMapper.toResponse(savedOrder);
    }
}
```

---

### 3. Infrastructure Layer (Outer - Framework Concerns)

**What belongs here:**
- JPA entities (persistence models)
- Repository implementations
- REST resources (controllers)
- External service clients (REST, SOAP)
- Message queue adapters
- Database configuration
- Framework-specific code

**Responsibilities:**
- Implements repository interfaces defined in domain layer
- Maps between domain models and JPA entities
- Handles HTTP requests/responses
- Integrates with external systems

**Example:**

```java
// infrastructure/persistence/entity/OrderEntity.java
package com.example.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatusEnum status;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItemEntity> items;

    // Getters and setters
}
```

```java
// infrastructure/persistence/repository/OrderRepositoryImpl.java
package com.example.infrastructure.persistence.repository;

import com.example.domain.model.Order;
import com.example.domain.repository.OrderRepository;
import com.example.infrastructure.persistence.entity.OrderEntity;
import com.example.infrastructure.persistence.mapper.OrderEntityMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class OrderRepositoryImpl implements OrderRepository {

    @Inject
    EntityManager em;

    @Inject
    OrderEntityMapper entityMapper;

    @Override
    public Order save(Order order) {
        OrderEntity entity = entityMapper.toEntity(order);
        
        if (entity.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        
        return entityMapper.toDomain(entity);
    }

    @Override
    public Optional<Order> findById(Long id) {
        OrderEntity entity = em.find(OrderEntity.class, id);
        return Optional.ofNullable(entity)
            .map(entityMapper::toDomain);
    }
}
```

```java
// infrastructure/api/rest/OrderResource.java
package com.example.infrastructure.api.rest;

import com.example.application.dto.CreateOrderRequest;
import com.example.application.dto.OrderResponse;
import com.example.application.service.OrderService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/orders")
@Produces("application/json")
@Consumes("application/json")
public class OrderResource {

    @Inject
    OrderService orderService;

    @POST
    public Response createOrder(CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return Response.status(Response.Status.CREATED).entity(order).build();
    }

    @GET
    @Path("/{id}")
    public Response getOrder(@PathParam("id") Long id) {
        OrderResponse order = orderService.getOrderDetails(id);
        return Response.ok(order).build();
    }
}
```

---

## Transaction Boundaries

**Rule:** Place `@Transactional` at the **Application Layer** (Service methods), NOT in other layers.

### Transaction Placement by Layer

| Layer | Use @Transactional? | Reason |
|-------|-------------------|---------|
| **REST Resources (Infrastructure)** | ❌ No | Let service layer control transactions |
| **Services (Application)** | ✅ Yes | This is your transaction boundary |
| **Repositories (Infrastructure)** | ❌ No | Called within service transaction |
| **Domain Models (Domain)** | ❌ No | Pure business logic, no framework deps |

**Example:**

```java
// ❌ WRONG: Transaction in REST layer
@Path("/orders")
public class OrderResource {
    @POST
    @Transactional // DON'T DO THIS
    public Response createOrder(OrderRequest request) {
        return Response.ok(orderService.createOrder(request)).build();
    }
}

// ✅ CORRECT: Transaction in Service layer
@ApplicationScoped
public class OrderService {
    
    @Transactional // THIS IS THE RIGHT PLACE
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = orderMapper.toDomain(request);
        order = orderRepository.save(order);
        return orderMapper.toResponse(order);
    }
}

// Repository runs within service transaction
@ApplicationScoped
public class OrderRepositoryImpl implements OrderRepository {
    
    // NO @Transactional - runs in active transaction from service
    public Order save(Order order) {
        OrderEntity entity = entityMapper.toEntity(order);
        em.persist(entity);
        return entityMapper.toDomain(entity);
    }
}
```

**See:** [Quarkus Transaction Guide](./quarkus-transaction.instruction.md) for detailed transaction management.

---

## Dependency Direction Rules

### ✅ Allowed Dependencies

```
Infrastructure → Application → Domain
     ↓              ↓
     └──────────────┘

REST Resource    →  Service     →  Domain Model
Repository Impl  →  Repository  →  Domain Model
JPA Entity       →  Domain Model (via mapper)
```

### ❌ Forbidden Dependencies

```
Domain → Application (NEVER)
Domain → Infrastructure (NEVER)
Application → Infrastructure (AVOID - use interfaces)
```

**Example:**

```java
// ✅ CORRECT: Domain model knows nothing about persistence
public class Order {
    // No @Entity, no @Table, no JPA annotations
    private Long id;
    private OrderStatus status;
    
    public void submit() { /* business logic */ }
}

// ❌ WRONG: Domain model depends on JPA (infrastructure concern)
@Entity // DON'T DO THIS IN DOMAIN MODEL
public class Order {
    @Id
    @GeneratedValue
    private Long id;
    
    public void submit() { /* business logic */ }
}
```

---

## Package Structure

### Recommended Structure

```
src/main/java/com/example/
├── domain/                          # Domain Layer (innermost)
│   ├── model/
│   │   ├── Order.java               # Pure domain model
│   │   ├── OrderItem.java
│   │   └── OrderStatus.java (enum)
│   ├── repository/                  # Repository interfaces
│   │   ├── OrderRepository.java
│   │   └── CustomerRepository.java
│   └── exception/
│       ├── OrderNotFoundException.java
│       └── InvalidOrderStateException.java
│
├── application/                     # Application Layer (orchestration)
│   ├── service/
│   │   ├── OrderService.java
│   │   └── CustomerService.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateOrderRequest.java
│   │   │   └── UpdateOrderRequest.java
│   │   └── response/
│   │       ├── OrderResponse.java
│   │       └── CustomerResponse.java
│   └── mapper/
│       ├── OrderMapper.java         # Domain ↔ DTO
│       └── CustomerMapper.java
│
└── infrastructure/                  # Infrastructure Layer (outermost)
    ├── persistence/
    │   ├── entity/
    │   │   ├── OrderEntity.java     # JPA entity
    │   │   └── OrderItemEntity.java
    │   ├── repository/
    │   │   └── OrderRepositoryImpl.java  # Repository implementation
    │   └── mapper/
    │       └── OrderEntityMapper.java    # JPA Entity ↔ Domain Model
    ├── api/
    │   ├── rest/                        # Use transport-specific subpackage
    │   │   ├── OrderResource.java       # For REST APIs
    │   │   └── CustomerResource.java
    │   ├── graphql/                     # For GraphQL (if applicable)
    │   │   └── OrderResolver.java
    │   └── grpc/                        # For gRPC (if applicable)
    │       └── OrderGrpcService.java
    └── client/
        └── PaymentGatewayClient.java
```

**Package Naming for API Layer:**

The `infrastructure/api` package should contain subpackages named after the transport protocol:

- **`infrastructure/api/rest`** - For JAX-RS REST endpoints (`@Path`, `@GET`, `@POST`)
- **`infrastructure/api/graphql`** - For GraphQL resolvers and schemas
- **`infrastructure/api/grpc`** - For gRPC service implementations

**Examples:**

```java
// REST API
package com.example.infrastructure.api.rest;

@Path("/api/v1/orders")
@Produces("application/json")
public class OrderResource {
    @Inject OrderService orderService;
    
    @POST
    public Response createOrder(CreateOrderRequest request) {
        return Response.ok(orderService.createOrder(request)).build();
    }
}
```

```java
// GraphQL Resolver (if using GraphQL)
package com.example.infrastructure.api.graphql;

@GraphQLApi
public class OrderResolver {
    @Inject OrderService orderService;
    
    @Query
    public OrderResponse getOrder(@Argument Long id) {
        return orderService.getOrderDetails(id);
    }
}
```

```java
// gRPC Service (if using gRPC)
package com.example.infrastructure.api.grpc;

@GrpcService
public class OrderGrpcService extends OrderServiceGrpc.OrderServiceImplBase {
    @Inject OrderService orderService;
    
    @Override
    public void createOrder(CreateOrderGrpcRequest request, StreamObserver<OrderGrpcResponse> responseObserver) {
        // Implementation
    }
}
```

**Note:** For existing projects using `infrastructure/rest`, you can keep that structure for backward compatibility. For new modules or services, use the `infrastructure/api/<transport>` pattern to keep the architecture flexible.

---

## Mapping Strategy

### Three Types of Mappers Needed

1. **Domain ↔ DTO Mapper** (Application Layer)
   - Maps DTOs to domain models and back
   - Used in services to transform API requests/responses

2. **Domain ↔ JPA Entity Mapper** (Infrastructure Layer)
   - Maps domain models to JPA entities and back
   - Used in repository implementations

3. **No direct DTO ↔ JPA Entity mapping** - Always go through domain model

**Flow:**

```
Request DTO → Domain Model → JPA Entity → Database
              ↓
         Business Logic
              ↓
Response DTO ← Domain Model ← JPA Entity ← Database
```

---

## Best Practices

### ✅ Do's

1. **Keep domain models pure** - No framework dependencies
2. **Business logic in domain layer** - Methods like `order.submit()`, not in services
3. **Define repository interfaces in domain** - Implementations in infrastructure
4. **Use @Transactional in service layer only** - Not in REST or repositories
5. **Map between layers** - Request DTO → Domain → JPA Entity → Response DTO
6. **Separate JPA entities from domain models** - They serve different purposes
7. **Dependency direction inward** - Infrastructure → Application → Domain
8. **One aggregate per service** - OrderService for Order aggregate
9. **Return domain models from repositories** - Not JPA entities
10. **Keep domain tests framework-independent** - Test business logic without DB

### ❌ Don'ts

1. **Don't put @Entity on domain models** - Use separate JPA entities
2. **Don't expose JPA entities to REST layer** - Use DTOs
3. **Don't let domain depend on outer layers** - Domain is framework-agnostic
4. **Don't put business logic in services** - Put it in domain models
5. **Don't call repositories from REST resources** - Use services
6. **Don't skip the mapper layer** - Always transform between boundaries
7. **Don't create anemic domain models** - Add behavior, not just getters/setters
8. **Don't use @Transactional in domain layer** - No framework dependencies
9. **Don't mix concerns** - Keep persistence separate from business logic
10. **Don't reuse DTOs across operations** - CreateOrderRequest ≠ UpdateOrderRequest

---

## Common Anti-Patterns

### ❌ Anti-Pattern 1: Anemic Domain Model

```java
// WRONG: Domain model is just a data bag
public class Order {
    private OrderStatus status;
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}

// Business logic leaks into service
public class OrderService {
    public void submitOrder(Long id) {
        Order order = repository.findById(id);
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new Exception("Invalid status");
        }
        order.setStatus(OrderStatus.SUBMITTED); // SERVICE doing DOMAIN work
    }
}
```

**✅ Correct: Rich Domain Model**

```java
public class Order {
    private OrderStatus status;
    
    // Business logic in domain
    public void submit() {
        if (status != OrderStatus.DRAFT) {
            throw new InvalidOrderStateException("Cannot submit " + status + " order");
        }
        this.status = OrderStatus.SUBMITTED;
    }
}

public class OrderService {
    @Transactional
    public void submitOrder(Long id) {
        Order order = repository.findById(id).orElseThrow();
        order.submit(); // Domain does its job
        repository.save(order);
    }
}
```

### ❌ Anti-Pattern 2: JPA Annotations in Domain Model

```java
// WRONG: Domain model depends on JPA (infrastructure)
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue
    private Long id;
    
    public void submit() { /* business logic */ }
}
```

**✅ Correct: Separate Domain and Persistence Models**

```java
// Domain model - clean
public class Order {
    private Long id;
    
    public void submit() { /* business logic */ }
}

// JPA entity - separate
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue
    private Long id;
    // Just persistence fields
}
```

### ❌ Anti-Pattern 3: Business Logic in Repository

```java
// WRONG: Repository doing business logic
public class OrderRepositoryImpl {
    public Order submitOrder(Long id) {
        Order order = findById(id);
        order.setStatus(OrderStatus.SUBMITTED); // Business logic here!
        return save(order);
    }
}
```

**✅ Correct: Repository Only Does CRUD**

```java
public class OrderRepositoryImpl {
    public Order save(Order order) { /* persist */ }
    public Optional<Order> findById(Long id) { /* find */ }
    // Pure data access, no business logic
}
```

---

## Benefits of Onion Architecture

1. **Testability** - Domain logic can be tested without database or frameworks
2. **Framework Independence** - Can switch from Quarkus to Spring without changing domain
3. **Database Independence** - Can swap databases without changing business logic
4. **UI Independence** - REST API, GraphQL, or CLI can all use same domain/services
5. **Maintainability** - Clear separation makes code easier to understand and modify
6. **Business Focus** - Domain layer clearly expresses business rules
7. **Delayed Decisions** - Can defer framework/database choices
8. **Team Scalability** - Different teams can work on different layers

---

## When to Use Onion Architecture

### ✅ Use When:
- Building enterprise applications with complex business logic
- Long-term project (5+ years expected lifetime)
- Multiple developers/teams working on codebase
- Business rules change frequently
- Need to support multiple UIs (REST, GraphQL, CLI, etc.)
- Framework/database might change in future
- **This sales-module project** ✅

### ⚠️ Consider Simpler Approaches When:
- Simple CRUD application with minimal business logic
- Prototype or proof-of-concept
- Very small team (1-2 developers)
- Short-lived project (< 1 year)
- Framework/database locked in contractually

---

## Related Guides

- [Java Database Patterns](./java-database-patterns.instruction.md) - Complete implementation examples with JPA and Panache
- [Quarkus Transaction Management](./quarkus-transaction.instruction.md) - Transaction boundaries and best practices
- [Unit Testing](./unit-testing.instructions.md) - Testing strategies for each layer
- [REST API Design](./rest-api.instructions.md) - Infrastructure layer best practices

---

## References

- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Onion Architecture by Jeffrey Palermo](https://jeffreypalermo.com/2008/07/the-onion-architecture-part-1/)
- [Domain-Driven Design by Eric Evans](https://www.domainlanguage.com/ddd/)
- [Hexagonal Architecture (Ports and Adapters)](https://alistair.cockburn.us/hexagonal-architecture/)

---

**Generated:** November 19, 2025  
**Based on:** Clean Architecture, Onion Architecture, and DDD principles
