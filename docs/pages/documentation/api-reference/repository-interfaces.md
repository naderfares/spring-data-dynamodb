---
layout: page
parent: API Reference
grand_parent: Documentation
title: Repository Interfaces
nav_order: 2
---

# Repository Interfaces API Reference

Spring Data DynamoDB repository interfaces following Spring Data conventions.

## DynamoDBCrudRepository

Basic CRUD operations for DynamoDB entities.

### Interface Declaration

```java
package org.socialsignin.spring.data.dynamodb.repository;

@NoRepositoryBean
public interface DynamoDBCrudRepository<T, ID>
    extends CrudRepository<T, ID>
```

### Type Parameters

- `T` - Entity type
- `ID` - Identifier type (can be simple type or composite ID class)

### Inherited Methods

All methods from Spring Data's `CrudRepository<T, ID>`:

#### save

```java
<S extends T> S save(S entity)
```

Saves a given entity.

**Parameters:**

- `entity` - Entity to save (must not be null)

**Returns:** The saved entity (never null)

**Events:**

- BeforeSaveEvent published before saving
- AfterSaveEvent published after saving

**Example:**

```java
public interface UserRepository extends DynamoDBCrudRepository<User, String> {
}

// Usage
User user = new User("user123", "John Doe");
user = userRepository.save(user);
```

#### saveAll

```java
<S extends T> Iterable<S> saveAll(Iterable<S> entities)
```

Saves all given entities in batch operations.

**Parameters:**

- `entities` - Entities to save (must not be null)

**Returns:** The saved entities (never null)

**Throws:**

- `BatchWriteException` - If any batch operation fails
- `IllegalArgumentException` - If entities is null

**Example:**

```java
List<User> users = Arrays.asList(
    new User("user1", "Alice"),
    new User("user2", "Bob")
);

userRepository.saveAll(users);
```

#### findById

```java
Optional<T> findById(ID id)
```

Retrieves an entity by its id.

**Parameters:**

- `id` - Entity identifier (must not be null)

**Returns:** Optional containing the entity or empty if not found

**Events:**

- AfterLoadEvent published if entity is found

**Example - Simple ID:**

```java
// For entity with hash key only
Optional<User> user = userRepository.findById("user123");
```

**Example - Composite ID:**

```java
// For entity with hash and range key
OrderId id = new OrderId("customer123", "2024-01-15");
Optional<Order> order = orderRepository.findById(id);
```

#### findAllById

```java
Iterable<T> findAllById(Iterable<ID> ids)
```

Retrieves all entities with the given ids using batch operations.

**Parameters:**

- `ids` - Entity identifiers (must not be null)

**Returns:** Entities with given ids (never null)

**Events:**

- AfterLoadEvent published for each loaded entity

**Example:**

```java
List<String> userIds = Arrays.asList("user1", "user2", "user3");
List<User> users = userRepository.findAllById(userIds);
```

#### existsById

```java
boolean existsById(ID id)
```

Returns whether an entity with the given id exists.

**Parameters:**

- `id` - Entity identifier (must not be null)

**Returns:** true if entity exists, false otherwise

**Note:** Implemented as `findById(id).isPresent()`, so it performs a load operation.

**Example:**

```java
if (userRepository.existsById("user123")) {
    // User exists
}
```

#### findAll

```java
Iterable<T> findAll()
```

Returns all instances of the type.

**Returns:** All entities

**Requires:** @EnableScan annotation on repository interface or method

**Warning:** Performs a table scan - expensive for large tables

**Example:**

```java
@EnableScan
public interface UserRepository extends DynamoDBCrudRepository<User, String> {
}

// Usage
List<User> allUsers = userRepository.findAll();
```

#### count

```java
long count()
```

Returns the number of entities available.

**Returns:** Count of entities

**Requires:** @EnableScan annotation on repository interface or method

**Warning:** Performs a table scan - expensive for large tables

**Example:**

```java
@EnableScan
public interface UserRepository extends DynamoDBCrudRepository<User, String> {
}

// Usage
long totalUsers = userRepository.count();
```

#### deleteById

```java
void deleteById(ID id)
```

Deletes the entity with the given id.

**Parameters:**

- `id` - Entity identifier (must not be null)

**Throws:**

- `EmptyResultDataAccessException` - If entity does not exist

**Events:**

- BeforeDeleteEvent published before deleting
- AfterDeleteEvent published after deleting

**Example:**

```java
try {
    userRepository.deleteById("user123");
} catch (EmptyResultDataAccessException e) {
    // User not found
}
```

#### delete

```java
void delete(T entity)
```

Deletes a given entity.

**Parameters:**

- `entity` - Entity to delete (must not be null)

**Events:**

- BeforeDeleteEvent published before deleting
- AfterDeleteEvent published after deleting

**Example:**

```java
User user = userRepository.findById("user123").orElseThrow();
userRepository.delete(user);
```

#### deleteAllById

```java
void deleteAllById(Iterable<? extends ID> ids)
```

Deletes all entities with the given ids in batch operations.

**Parameters:**

- `ids` - Entity identifiers (must not be null)

**Note:** First loads entities, then batch deletes them

**Example:**

```java
List<String> idsToDelete = Arrays.asList("user1", "user2", "user3");
userRepository.deleteAllById(idsToDelete);
```

#### deleteAll (with entities)

```java
void deleteAll(Iterable<? extends T> entities)
```

Deletes the given entities in batch operations.

**Parameters:**

- `entities` - Entities to delete (must not be null)

**Example:**

```java
List<User> inactiveUsers = userRepository.findByStatus("INACTIVE");
userRepository.deleteAll(inactiveUsers);
```

#### deleteAll

```java
void deleteAll()
```

Deletes all entities managed by the repository.

**Requires:** @EnableScan annotation on repository interface or method

**Warning:** Performs scan then batch delete - expensive operation

**Example:**

```java
@EnableScan
public interface UserRepository extends DynamoDBCrudRepository<User, String> {
}

// Usage - BE CAREFUL!
userRepository.deleteAll();
```

### Usage Example

```java
@EnableDynamoDBRepositories
public class DynamoDBConfig {
}

public interface UserRepository extends DynamoDBCrudRepository<User, String> {
    // Custom query methods can be added here
}

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void demonstrateOperations() {
        // Save
        User user = new User("user123", "John Doe");
        userRepository.save(user);

        // Find
        Optional<User> found = userRepository.findById("user123");

        // Update
        found.ifPresent(u -> {
            u.setName("Jane Doe");
            userRepository.save(u);
        });

        // Delete
        userRepository.deleteById("user123");
    }
}
```

## DynamoDBPagingAndSortingRepository

Extends DynamoDBCrudRepository with pagination and sorting support.

### Interface Declaration

```java
package org.socialsignin.spring.data.dynamodb.repository;

@NoRepositoryBean
public interface DynamoDBPagingAndSortingRepository<T, ID>
    extends PagingAndSortingRepository<T, ID>
```

### Additional Methods

#### findAll (with Sort)

```java
Iterable<T> findAll(Sort sort)
```

Returns all entities sorted by the given options.

**Parameters:**

- `sort` - Sort specification (must not be null)

**Returns:** All entities in sorted order

**Requires:** @EnableScan annotation

**Note:** DynamoDB sorting is limited - only range key can be sorted natively

**Example:**

```java
@EnableScan
public interface UserRepository
    extends DynamoDBPagingAndSortingRepository<User, String> {
}

// Usage
Sort sort = Sort.by(Sort.Direction.ASC, "name");
List<User> users = userRepository.findAll(sort);
```

#### findAll (with Pageable)

```java
Page<T> findAll(Pageable pageable)
```

Returns a Page of entities meeting the paging restriction.

**Parameters:**

- `pageable` - Paging specification (must not be null)

**Returns:** Page of entities

**Requires:** @EnableScan annotation

**Example:**

```java
@EnableScan
public interface UserRepository
    extends DynamoDBPagingAndSortingRepository<User, String> {
}

// Usage
Pageable pageable = PageRequest.of(0, 20);
Page<User> page = userRepository.findAll(pageable);

System.out.println("Total: " + page.getTotalElements());
System.out.println("Pages: " + page.getTotalPages());
```

### Usage Example

```java
public interface OrderRepository
    extends DynamoDBPagingAndSortingRepository<Order, OrderId> {

    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    Slice<Order> findByStatus(String status, Pageable pageable);
}

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Page<Order> getCustomerOrders(String customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByCustomerId(customerId, pageable);
    }

    public void processOrdersInBatches(String status) {
        Pageable pageable = PageRequest.of(0, 100);
        Slice<Order> slice;

        do {
            slice = orderRepository.findByStatus(status, pageable);
            // Process slice
            for (Order order : slice) {
                processOrder(order);
            }
            pageable = slice.nextPageable();
        } while (slice.hasNext());
    }
}
```

## SimpleDynamoDBCrudRepository

Default implementation of DynamoDBCrudRepository.

### Class Declaration

```java
package org.socialsignin.spring.data.dynamodb.repository.support;

public class SimpleDynamoDBCrudRepository<T, ID>
    implements DynamoDBCrudRepository<T, ID>
```

### Key Features

- Handles both hash-key-only and hash-range-key entities
- Enforces @EnableScan requirements for scan operations
- Wraps batch operation failures into exceptions
- Provides detailed error messages

### Scan Permission Validation

Methods that require @EnableScan:

```java
void assertScanEnabled(boolean scanEnabled, String methodName)
```

Throws `IllegalArgumentException` if scan is not enabled for:

- `findAll()`
- `count()`
- `deleteAll()`

## DynamoDBOperations

Low-level interface for DynamoDB operations.

### Interface Declaration

```java
package org.socialsignin.spring.data.dynamodb.core;

public interface DynamoDBOperations
```

### Core Methods

See [DynamoDBTemplate](dynamodb-template.html) for detailed documentation of all operations.

## Composite ID Support

For entities with both hash and range keys, create a composite ID class:

### ID Class Requirements

```java
public class OrderId implements Serializable {

    private String customerId;  // Hash key
    private String orderDate;   // Range key

    // Must have no-arg constructor
    public OrderId() {
    }

    public OrderId(String customerId, String orderDate) {
        this.customerId = customerId;
        this.orderDate = orderDate;
    }

    // Getters and setters
    // equals() and hashCode() based on both fields
}
```

### Entity with Composite ID

```java
@DynamoDBTable(tableName = "orders")
public class Order {

    @Id
    private OrderId id;

    @DynamoDBHashKey
    public String getCustomerId() {
        return id != null ? id.getCustomerId() : null;
    }

    @DynamoDBRangeKey
    public String getOrderDate() {
        return id != null ? id.getOrderDate() : null;
    }

    // Other fields and methods
}
```

### Repository with Composite ID

```java
public interface OrderRepository
    extends DynamoDBCrudRepository<Order, OrderId> {

    // findById automatically handles composite ID
    Optional<Order> findById(OrderId id);

    // Derived queries can use ID components
    List<Order> findByCustomerId(String customerId);
    List<Order> findByOrderDate(String orderDate);
}
```

## Exception Handling

### BatchWriteException

Thrown when batch save operations fail:

```java
try {
    userRepository.saveAll(users);
} catch (BatchWriteException e) {
    List<FailedBatch> failures = e.getFailedBatches();
    for (FailedBatch batch : failures) {
        // Handle each failed batch
        System.err.println("Failed: " + batch.getException().getMessage());
    }
}
```

### EmptyResultDataAccessException

Thrown when expected entity is not found:

```java
try {
    userRepository.deleteById("nonexistent");
} catch (EmptyResultDataAccessException e) {
    // Entity not found
}
```

## Best Practices

1. **Use @EnableScan Carefully** - Only enable scans when absolutely necessary
2. **Batch Operations** - Use saveAll/deleteAll for multiple items
3. **Composite IDs** - Properly implement equals() and hashCode()
4. **Pagination** - Use Pageable for large result sets
5. **Existence Checks** - Use existsById() instead of findById() when you only need to check existence
6. **Custom Methods** - Add derived query methods for common queries

## See Also

- [DynamoDBTemplate](dynamodb-template.html)
- [Query Methods](query-methods.html)
- [Annotations](annotations.html)
- [Configuration](configuration.html)
