---
layout: home
redirect_to: /about
---

# Spring Data DynamoDB

Build powerful data access layers for AWS DynamoDB using the familiar Spring Data programming model.

## Why Spring Data DynamoDB?

Spring Data DynamoDB brings the productivity and elegance of Spring Data to AWS DynamoDB, enabling you to:

- Use familiar repository patterns instead of low-level SDK calls
- Derive queries from method names automatically
- Leverage Spring Boot auto-configuration for zero-config setup
- Build type-safe queries with compile-time checking
- Integrate seamlessly with Spring ecosystem (Boot, REST, Events)

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>io.github.naderfares</groupId>
    <artifactId>spring-data-dynamodb</artifactId>
    <version>6.0.11</version>
</dependency>
```

### 2. Define Entity

```java
@DynamoDBTable(tableName = "User")
public class User {
    @Id
    @DynamoDBHashKey
    private String userId;

    @DynamoDBAttribute
    private String email;

    @DynamoDBAttribute
    private String name;

    // getters and setters
}
```

### 3. Create Repository

```java
public interface UserRepository extends DynamoDBCrudRepository<User, String> {
    List<User> findByName(String name);
    Optional<User> findByEmail(String email);
}
```

### 4. Use It

```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User createUser(String userId, String email, String name) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setName(name);
        return userRepository.save(user);
    }

    public List<User> findByName(String name) {
        return userRepository.findByName(name);
    }
}
```

That's it! No DynamoDB SDK boilerplate required.

## Key Features

### Repository Abstraction

Use standard Spring Data repository interfaces for CRUD operations, paging, and sorting.

```java
public interface ProductRepository
    extends DynamoDBPagingAndSortingRepository<Product, String> {
}
```

### Query Method Derivation

Derive queries automatically from method names:

```java
List<Order> findByCustomerIdAndStatusOrderByCreatedDateDesc(
    String customerId,
    String status
);
```

### DynamoDB-Specific Features

Full support for hash keys, range keys, global secondary indexes, and local secondary indexes:

```java
@DynamoDBTable(tableName = "Order")
public class Order {
    @DynamoDBHashKey
    private String customerId;

    @DynamoDBRangeKey
    private String orderId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "status-index")
    private String status;
}
```

### Pagination Support

Built-in pagination for efficient data retrieval:

```java
Page<Product> products = productRepository.findAll(
    PageRequest.of(0, 20)
);
```

### Projections

Fetch only the attributes you need to reduce cost:

```java
@Projection(projectionFields = {"userId", "email"})
List<User> findByName(String name);
```

### Batch Operations

Efficient batch reads and writes with automatic error handling:

```java
Iterable<User> users = userRepository.saveAll(userList);
```

## What's Supported

### Spring Data Features

- Repository interfaces (CrudRepository, PagingAndSortingRepository)
- Query method derivation with keywords (findBy, countBy, deleteBy)
- Pagination and sorting
- Custom repository implementations
- Spring Data REST integration
- Lifecycle events (BeforeSave, AfterLoad, etc.)

### DynamoDB Features

- Hash keys and range keys (composite primary keys)
- Global secondary indexes (GSI)
- Local secondary indexes (LSI)
- Projections (fetch specific attributes)
- Batch operations (batchLoad, batchSave, batchDelete)
- Conditional writes
- Scan protection with @EnableScan
- Table name overrides at runtime

### Integration Features

- Spring Boot 3.x auto-configuration
- AWS SDK v1 integration
- DynamoDB Local support for testing
- Spring transaction support
- Spring events for entity lifecycle

## Version Compatibility

**Current Release: 6.0.11**

| Spring Data DynamoDB | Spring Boot | Spring Framework | Spring Data |
|----------------------|-------------|------------------|-------------|
| 6.0.11 (current)     | >= 3.5.6    | >= 6.2.11        | 2025.0.4    |
| 6.0.x                | >= 3.2.5    | >= 6.1.6         | 2023.1.5    |

Requires Java 17 or higher.

## Documentation

Explore comprehensive documentation:

- [About](/about/) - Project overview and compatibility
- [Documentation](/documentation/) - Complete reference documentation
- [Getting Started](/documentation/implementation/) - Step-by-step guides
- [API Reference](/documentation/api-reference/) - Complete API documentation
- [Examples](https://github.com/naderfares/spring-data-dynamodb/tree/develop/src/test/java/org/socialsignin/spring/data/dynamodb) -
  Code examples

## Community

- **GitHub**: [naderfares/spring-data-dynamodb](https://github.com/naderfares/spring-data-dynamodb)
- **Issues**: [Report bugs or request features](https://github.com/naderfares/spring-data-dynamodb/issues)
- **Maven Central**: [View releases](https://search.maven.org/artifact/io.github.naderfares/spring-data-dynamodb)

## License

Apache License 2.0 - See [LICENSE](https://github.com/naderfares/spring-data-dynamodb/blob/develop/LICENSE.txt)
