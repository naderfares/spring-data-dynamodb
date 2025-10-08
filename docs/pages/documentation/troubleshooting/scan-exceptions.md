---
layout: page
parent: Troubleshooting
grand_parent: Documentation
title: Scan Exceptions
nav_order: 2
---

# Scan Exceptions and @EnableScan

This guide explains scan-related exceptions and how to properly use the `@EnableScan` annotation.

## Table of Contents

{: .no_toc .text-delta }

1. TOC
   {:toc}

---

## Understanding the Scan Protection

Spring Data DynamoDB protects you from accidentally performing expensive scan operations by requiring explicit opt-in
via the `@EnableScan` annotation. This is a **deliberate design decision** to prevent performance issues in production.

### Why Scan Protection Exists

1. **Cost** - Scans read every item in the table and consume throughput
2. **Performance** - Scans are slow on large tables
3. **Predictability** - Scan costs scale with table size, not query parameters
4. **Best Practices** - DynamoDB is designed for key-based lookups, not table scans

---

## Common Scan Exceptions

### "Scanning for unpaginated queries is not enabled"

**Error:**

```
java.lang.UnsupportedOperationException: Scanning for unpaginated queries is not enabled.
To enable annotate your repository method with @EnableScan, or enable scan for unpaginated
queries on repositories using @EnableScanCount.
```

**Cause:** Attempting to execute a query that requires scanning without enabling scan operations.

**Methods That Require Scans:**

- `findAll()`
- `count()`
- `deleteAll()`
- Query methods on non-key attributes
- Query methods with OR conditions

---

## Solutions

### Solution 1: Enable Scan on Specific Methods

```java
public interface UserRepository extends CrudRepository<User, String> {

    // ✅ Scan enabled for this method only
    @EnableScan
    List<User> findAll();

    // ✅ Scan enabled for count operation
    @EnableScan
    long count();

    // ✅ Scan enabled for non-key attribute query
    @EnableScan
    List<User> findByStatus(String status);

    // ✅ No scan needed - uses hash key
    Optional<User> findById(String id);
}
```

### Solution 2: Enable Scan on Repository Interface

```java
// ✅ Enable scan for all methods in this repository
@EnableScan
public interface UserRepository extends CrudRepository<User, String> {

    List<User> findAll();
    long count();
    List<User> findByStatus(String status);
}
```

### Solution 3: Enable Scan with Count Control

```java
public interface UserRepository extends CrudRepository<User, String> {

    // ✅ Enable scan but disable count to improve performance
    @EnableScan
    @EnableScanCount(false)
    Page<User> findByStatus(String status, Pageable pageable);

    // Returns a Slice instead of Page (no total count)
}
```

### Solution 4: Use Projections to Limit Data

```java
public interface UserRepository extends CrudRepository<User, String> {

    // ✅ Scan enabled with projection to reduce data transfer
    @EnableScan
    @Projection(projectionExpression = "id, #n, email",
                expressionAttributeNames = {"#n" = "name"})
    List<User> findByStatus(String status);
}
```

---

## Query vs Scan Operations

### Operations That Use Query (No @EnableScan Needed)

```java
public interface OrderRepository extends CrudRepository<Order, OrderId> {

    // ✅ Query - Uses hash key
    Optional<Order> findById(OrderId id);

    // ✅ Query - Uses hash key
    List<Order> findByCustomerId(String customerId);

    // ✅ Query - Uses hash key + range key
    Optional<Order> findByCustomerIdAndOrderDate(String customerId, String orderDate);

    // ✅ Query - Uses hash key + range key condition
    List<Order> findByCustomerIdAndOrderDateBetween(
        String customerId, String startDate, String endDate);

    // ✅ Query - Uses hash key + range key prefix
    List<Order> findByCustomerIdAndOrderDateStartingWith(
        String customerId, String prefix);
}
```

### Operations That Require Scan (@EnableScan Required)

```java
public interface OrderRepository extends CrudRepository<Order, OrderId> {

    // ❌ Scan - No key specified
    @EnableScan
    List<Order> findAll();

    // ❌ Scan - Non-key attribute
    @EnableScan
    List<Order> findByStatus(String status);

    // ❌ Scan - OR condition
    @EnableScan
    List<Order> findByStatusOrAmount(String status, Double amount);

    // ❌ Scan - Only range key (no hash key)
    @EnableScan
    List<Order> findByOrderDate(String orderDate);

    // ❌ Scan - Count operation
    @EnableScan
    long count();

    // ❌ Scan - Delete all
    @EnableScan
    void deleteAll();
}
```

---

## Global Secondary Index (GSI) Support

You can use GSI attributes in query methods to avoid scans:

```java
@DynamoDBTable(tableName = "User")
public class User {

    private String id;
    private String email;
    private String status;

    @DynamoDBHashKey
    public String getId() {
        return id;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "email-index")
    public String getEmail() {
        return email;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "status-index")
    public String getStatus() {
        return status;
    }

    // setters...
}

public interface UserRepository extends CrudRepository<User, String> {

    // ✅ Query - Uses GSI, no scan needed
    List<User> findByEmail(String email);

    // ✅ Query - Uses GSI, no scan needed
    List<User> findByStatus(String status);
}
```

---

## Performance Considerations

### Cost of Scan Operations

```java
// ❌ Expensive - Scans entire table
@EnableScan
public List<User> findByStatus(String status) {
    // Reads ALL items, filters in application
    // Cost: Entire table's RCUs
}

// ✅ Efficient - Uses GSI
public List<User> findByStatus(String status) {
    // Uses status-index GSI
    // Cost: Only matching items' RCUs
}
```

### Pagination with Scans

```java
public interface UserRepository extends CrudRepository<User, String> {

    // ✅ Better - Paginated scan
    @EnableScan
    @EnableScanCount(false)  // Disable count for performance
    Slice<User> findByStatus(String status, Pageable pageable);

    // ❌ Worse - Unpaginated scan
    @EnableScan
    List<User> findByStatus(String status);
}

// Usage
Pageable pageable = PageRequest.of(0, 100);
Slice<User> users = repository.findByStatus("ACTIVE", pageable);

while (users.hasNext()) {
    // Process current page
    users.getContent().forEach(this::processUser);

    // Get next page
    pageable = users.nextPageable();
    users = repository.findByStatus("ACTIVE", pageable);
}
```

---

## Best Practices

### 1. Avoid Scans in Production

```java
// ❌ Avoid in production
@EnableScan
public interface UserRepository extends CrudRepository<User, String> {
    List<User> findAll();
    List<User> findByLastName(String lastName);
}

// ✅ Better - Use GSI or limit usage to admin operations
public interface UserRepository extends CrudRepository<User, String> {

    // Admin operation only
    @EnableScan
    @EnableScanCount(false)
    Slice<User> findAll(Pageable pageable);

    // Use GSI for frequently queried attributes
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "lastName-index")
    List<User> findByLastName(String lastName);
}
```

### 2. Use Consistent Read Only When Necessary

```java
public interface UserRepository extends CrudRepository<User, String> {

    // ✅ Eventually consistent (cheaper, faster)
    @EnableScan
    List<User> findByStatus(String status);

    // ✅ Strongly consistent (when needed)
    @EnableScan
    @ConsistentRead
    List<User> findByStatus(String status);
}
```

### 3. Limit Result Size

```java
public interface UserRepository extends CrudRepository<User, String> {

    // ✅ Limit results to prevent large scans
    @EnableScan
    List<User> findFirst100ByStatus(String status);

    // ✅ Use pagination
    @EnableScan
    Slice<User> findByStatus(String status, Pageable pageable);
}
```

### 4. Use Projections

```java
public interface UserRepository extends CrudRepository<User, String> {

    // ✅ Only fetch needed attributes
    @EnableScan
    @Projection(projectionExpression = "id, #n",
                expressionAttributeNames = {"#n" = "name"})
    List<User> findByStatus(String status);
}
```

---

## Alternatives to Scanning

### 1. Design Tables for Access Patterns

```java
// Instead of scanning for all orders by status:
@DynamoDBTable(tableName = "Order")
public class Order {

    // Add status as GSI hash key
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "status-date-index")
    private String status;

    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "status-date-index")
    private String orderDate;
}

// Now you can query efficiently
List<Order> findByStatus(String status);
List<Order> findByStatusAndOrderDateBetween(String status, String start, String end);
```

### 2. Use DynamoDB Streams

For operations like "find all active users", consider:

- Maintaining a separate index table via DynamoDB Streams
- Using ElasticSearch/OpenSearch for complex queries
- Denormalizing data to support access patterns

### 3. Batch Reads for Known Keys

```java
// Instead of findAll()
List<User> users = repository.findAllById(knownUserIds);

// Or use DynamoDBMapper.batchLoad()
```

---

## Testing with Scans

For tests and development, scans are often acceptable:

```java
@TestConfiguration
public class TestRepositoryConfig {

    // ✅ Enable scans globally for tests
    @EnableScan
    public interface TestUserRepository extends CrudRepository<User, String> {
        List<User> findAll();
        void deleteAll();
    }
}

@SpringBootTest
class UserServiceTest {

    @Autowired
    private TestUserRepository repository;

    @BeforeEach
    void setUp() {
        // OK in tests
        repository.deleteAll();
    }

    @Test
    void testUserCreation() {
        // OK in tests
        List<User> users = repository.findAll();
        assertEquals(0, users.size());
    }
}
```

---

## Next Steps

- Learn about [Batch Operation Failures](batch-operation-failures)
- Review [Performance Tuning](performance-tuning) for optimization strategies
- Check the [FAQ](faq) for additional questions
