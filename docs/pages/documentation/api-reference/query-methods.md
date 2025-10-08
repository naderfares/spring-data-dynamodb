---
layout: page
parent: API Reference
grand_parent: Documentation
title: Query Methods
nav_order: 4
---

# Query Methods API Reference

Supported keywords and patterns for derived query methods in Spring Data DynamoDB.

## Overview

Spring Data DynamoDB supports query method derivation following Spring Data conventions. Method names are parsed into
DynamoDB queries or scans based on the entity structure and query conditions.

## Query vs Scan Execution

### Query Operations (Efficient)

Query operations are used when:

- Filtering by hash key (required)
- Optionally filtering by range key
- Using equality or range conditions on range key

**Benefits:**

- Fast and cost-effective
- Uses DynamoDB's native query operation
- Automatically uses indexes when appropriate

**Example:**

```java
public interface OrderRepository extends DynamoDBCrudRepository<Order, OrderId> {
    // Query: uses hash key + range key condition
    List<Order> findByCustomerIdAndOrderDateBetween(
        String customerId,
        String startDate,
        String endDate
    );
}
```

### Scan Operations (Expensive)

Scan operations are used when:

- Filtering by non-key attributes
- Complex conditions not supported by queries
- No hash key in the query

**Requirements:**

- Must use @EnableScan annotation
- More expensive in terms of read capacity and cost
- Slower for large tables

**Example:**

```java
public interface UserRepository extends DynamoDBCrudRepository<User, String> {

    // Scan: filters by non-key attribute
    @EnableScan
    List<User> findByEmail(String email);

    @EnableScan
    List<User> findByAgeGreaterThan(int age);
}
```

## Supported Keywords

### Query Subject Keywords

#### find...By / read...By / get...By / query...By

Returns all matching entities.

**Return Types:** List, Collection, Iterable, Stream

**Examples:**

```java
List<User> findByName(String name);
Collection<User> readByStatus(String status);
Iterable<User> getByCategory(String category);
Stream<User> queryByType(String type);
```

#### findOne...By / findFirst...By

Returns the first matching entity.

**Return Types:** Entity type, Optional

**Examples:**

```java
Optional<User> findFirstByName(String name);
User findOneByEmail(String email);
```

#### exists...By

Checks if matching entities exist.

**Return Type:** boolean

**Example:**

```java
boolean existsByEmail(String email);
```

#### count...By

Counts matching entities.

**Return Type:** long, int

**Example:**

```java
@EnableScanCount
long countByStatus(String status);

int countByCategory(String category);
```

#### delete...By / remove...By

Deletes matching entities.

**Return Types:** void, Long (number of deleted entities)

**Example:**

```java
@EnableScan
void deleteByStatus(String status);

@EnableScan
Long removeByCreatedDateBefore(Date date);
```

### Property Expressions

#### Simple Properties

```java
List<User> findByName(String name);
List<User> findById(String id);
```

#### Nested Properties

Use camel case to traverse nested properties.

**Example:**

```java
@DynamoDBTable(tableName = "users")
public class User {
    private Address address;

    @DynamoDBDocument
    public static class Address {
        private String city;
        private String zipCode;
    }
}

public interface UserRepository extends DynamoDBCrudRepository<User, String> {
    @EnableScan
    List<User> findByAddressCity(String city);

    @EnableScan
    List<User> findByAddressZipCode(String zipCode);
}
```

### Conditional Keywords

#### Equality

**Keyword:** Is, Equals (or omitted)

**DynamoDB Operator:** EQ

**Examples:**

```java
List<User> findByName(String name);
List<User> findByNameIs(String name);
List<User> findByNameEquals(String name);
```

#### Null Checks

**Keywords:** IsNull, IsNotNull

**DynamoDB Operator:** NULL, NOT_NULL

**Examples:**

```java
@EnableScan
List<User> findByMiddleNameIsNull();

@EnableScan
List<User> findByPhoneNumberIsNotNull();
```

#### Boolean

**Keywords:** IsTrue, IsFalse

**DynamoDB Operator:** EQ with boolean value

**Examples:**

```java
@EnableScan
List<User> findByActiveIsTrue();

@EnableScan
List<User> findByDeletedIsFalse();
```

#### Comparison

**Keywords:** LessThan, LessThanEqual, GreaterThan, GreaterThanEqual

**DynamoDB Operators:** LT, LE, GT, GE

**Examples:**

```java
// Range key comparisons (query)
List<Order> findByCustomerIdAndOrderDateGreaterThan(
    String customerId,
    String date
);

// Non-key comparisons (scan)
@EnableScan
List<User> findByAgeGreaterThan(int age);

@EnableScan
List<Product> findByPriceLessThanEqual(BigDecimal maxPrice);
```

#### Between

**Keyword:** Between

**DynamoDB Operator:** BETWEEN

**Examples:**

```java
// Range key between (query)
List<Order> findByCustomerIdAndOrderDateBetween(
    String customerId,
    String startDate,
    String endDate
);

// Non-key between (scan)
@EnableScan
List<User> findByAgeBetween(int minAge, int maxAge);
```

#### Pattern Matching

**Keywords:** Like, NotLike, StartingWith, EndingWith, Containing

**DynamoDB Operators:** BEGINS_WITH, CONTAINS

**Note:** DynamoDB has limited string pattern matching:

- BEGINS_WITH: Supported on range keys in queries
- CONTAINS: Only supported in scans

**Examples:**

```java
// Range key begins with (query)
List<Order> findByCustomerIdAndOrderDateStartingWith(
    String customerId,
    String datePrefix
);

// Scan operations
@EnableScan
List<User> findByNameStartingWith(String prefix);

@EnableScan
List<User> findByEmailContaining(String substring);
```

#### Collection Membership

**Keywords:** In, NotIn

**DynamoDB Operator:** IN

**Examples:**

```java
@EnableScan
List<User> findByStatusIn(List<String> statuses);

@EnableScan
List<User> findByIdNotIn(Collection<String> excludeIds);
```

### Logical Operators

#### And

Combines conditions with logical AND.

**Examples:**

```java
// Query: hash key + range key conditions
List<Order> findByCustomerIdAndOrderDateGreaterThan(
    String customerId,
    String date
);

// Scan: multiple non-key conditions
@EnableScan
List<User> findByNameAndStatus(String name, String status);

@EnableScan
List<Product> findByCategoryAndPriceLessThan(
    String category,
    BigDecimal maxPrice
);
```

#### Or

Combines conditions with logical OR.

**Note:** OR conditions generally require scans.

**Examples:**

```java
@EnableScan
List<User> findByNameOrEmail(String name, String email);

@EnableScan
List<Product> findByCategoryOrBrand(String category, String brand);
```

### Result Modifiers

#### Distinct

**Keyword:** Distinct

**Note:** Has limited effect in DynamoDB (no server-side DISTINCT).

**Example:**

```java
@EnableScan
List<User> findDistinctByStatus(String status);
```

#### OrderBy

**Keyword:** OrderBy[Property][Asc|Desc]

**Note:** Only range key can be sorted natively in DynamoDB queries.

**Examples:**

```java
// Natural order by range key (query)
List<Order> findByCustomerIdOrderByOrderDateAsc(String customerId);
List<Order> findByCustomerIdOrderByOrderDateDesc(String customerId);

// Client-side sorting (scan)
@EnableScan
List<User> findByStatusOrderByNameAsc(String status);
```

#### Top / First

**Keywords:** Top[N], First[N]

**DynamoDB:** Limits query/scan results.

**Examples:**

```java
User findFirstByCustomerId(String customerId);

List<Order> findTop10ByCustomerIdOrderByOrderDateDesc(String customerId);

List<User> findFirst5ByStatus(String status);
```

## Pagination Support

### Pageable Parameter

Add a Pageable parameter to enable pagination.

**Return Types:**

- Page - Includes total count (may require scan for count)
- Slice - No total count (more efficient)

**Examples:**

```java
public interface OrderRepository
    extends DynamoDBPagingAndSortingRepository<Order, OrderId> {

    // Returns Page with total count
    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    // Returns Slice without total count (more efficient)
    Slice<Order> findByStatus(String status, Pageable pageable);

    // Combined with other conditions
    Page<Order> findByCustomerIdAndOrderDateGreaterThan(
        String customerId,
        String date,
        Pageable pageable
    );
}

// Usage
PageRequest pageable = PageRequest.of(0, 20);
Page<Order> page = orderRepository.findByCustomerId("customer123", pageable);

System.out.println("Total: " + page.getTotalElements());
System.out.println("Has next: " + page.hasNext());

for (Order order : page.getContent()) {
    // Process order
}
```

### Sort Parameter

Add a Sort parameter for sorting.

**Note:** DynamoDB has limited sorting capabilities.

**Example:**

```java
Sort sort = Sort.by(Sort.Direction.ASC, "orderDate");
List<Order> orders = orderRepository.findByCustomerId("customer123", sort);
```

## Special Query Methods

### Custom Queries with @Query

**Not supported** - Spring Data DynamoDB does not support @Query annotation.

Alternative: Implement custom repository methods.

```java
public interface UserRepositoryCustom {
    List<User> complexQuery(Map<String, Object> params);
}

public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @Autowired
    private DynamoDBOperations operations;

    @Override
    public List<User> complexQuery(Map<String, Object> params) {
        DynamoDBQueryExpression<User> expression = // Build expression
        return operations.query(User.class, expression);
    }
}

public interface UserRepository
    extends DynamoDBCrudRepository<User, String>, UserRepositoryCustom {
}
```

## Query Method Examples

### Simple Queries

```java
public interface UserRepository extends DynamoDBCrudRepository<User, String> {

    // Find by ID (inherited from CrudRepository)
    Optional<User> findById(String id);

    // Custom query by email (requires scan)
    @EnableScan
    Optional<User> findByEmail(String email);

    // Query by boolean property
    @EnableScan
    List<User> findByActiveIsTrue();

    // Check existence
    @EnableScan
    boolean existsByEmail(String email);
}
```

### Hash and Range Key Queries

```java
public interface OrderRepository extends DynamoDBCrudRepository<Order, OrderId> {

    // Query by hash key only
    List<Order> findByCustomerId(String customerId);

    // Query by hash key with range key condition
    List<Order> findByCustomerIdAndOrderDateGreaterThan(
        String customerId,
        String date
    );

    List<Order> findByCustomerIdAndOrderDateBetween(
        String customerId,
        String startDate,
        String endDate
    );

    // With limit
    List<Order> findTop5ByCustomerIdOrderByOrderDateDesc(String customerId);
}
```

### Global Secondary Index Queries

```java
@DynamoDBTable(tableName = "orders")
public class Order {

    @DynamoDBHashKey
    public String getCustomerId() { return customerId; }

    @DynamoDBRangeKey
    public String getOrderDate() { return orderDate; }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "status-index")
    public String getStatus() { return status; }

    @DynamoDBIndexRangeKey(
        globalSecondaryIndexName = "status-index",
        attributeName = "createdAt"
    )
    public String getCreatedAt() { return createdAt; }
}

public interface OrderRepository extends DynamoDBCrudRepository<Order, OrderId> {

    // Uses status-index GSI
    List<Order> findByStatus(String status);

    List<Order> findByStatusAndCreatedAtGreaterThan(
        String status,
        String date
    );
}
```

### Scan Operations

```java
public interface ProductRepository extends DynamoDBCrudRepository<Product, String> {

    // Scan by category
    @EnableScan
    List<Product> findByCategory(String category);

    // Scan with multiple conditions
    @EnableScan
    List<Product> findByCategoryAndPriceLessThan(
        String category,
        BigDecimal maxPrice
    );

    // Scan with OR condition
    @EnableScan
    List<Product> findByCategoryOrBrand(String category, String brand);

    // Scan with pattern matching
    @EnableScan
    List<Product> findByNameContaining(String keyword);

    @EnableScan
    List<Product> findByDescriptionStartingWith(String prefix);

    // Scan with null checks
    @EnableScan
    List<Product> findByDiscountIsNotNull();

    // Count (requires @EnableScanCount)
    @EnableScanCount
    long countByCategory(String category);

    // Delete
    @EnableScan
    void deleteByDiscontinuedIsTrue();
}
```

### Pagination Examples

```java
public interface OrderRepository
    extends DynamoDBPagingAndSortingRepository<Order, OrderId> {

    // Page with total count
    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    // Slice without total count (more efficient)
    Slice<Order> findByStatus(String status, Pageable pageable);
}

// Usage
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public void processOrdersInBatches(String customerId) {
        int pageSize = 100;
        int pageNumber = 0;
        Slice<Order> slice;

        do {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            slice = orderRepository.findByStatus("PENDING", pageable);

            for (Order order : slice.getContent()) {
                processOrder(order);
            }

            pageNumber++;
        } while (slice.hasNext());
    }

    public Page<Order> getCustomerOrdersPage(String customerId, int page) {
        Pageable pageable = PageRequest.of(page, 20);
        return orderRepository.findByCustomerId(customerId, pageable);
    }
}
```

## Query Method Limitations

### DynamoDB Constraints

1. **Hash Key Required for Queries** - Cannot query without hash key (requires scan)
2. **Single Range Key Condition** - Can only filter on one range key per query
3. **Limited String Matching** - Only BEGINS_WITH on range keys, CONTAINS requires scan
4. **No Server-Side Joins** - Cannot join across tables
5. **No Aggregations** - No SUM, AVG, GROUP BY operations
6. **Limited Sorting** - Only by range key in queries

### Workarounds

**Use Global Secondary Indexes:**

```java
@DynamoDBTable(tableName = "users")
public class User {

    @DynamoDBHashKey
    public String getId() { return id; }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "email-index")
    public String getEmail() { return email; }
}

public interface UserRepository extends DynamoDBCrudRepository<User, String> {
    // Uses email-index GSI instead of scan
    Optional<User> findByEmail(String email);
}
```

**Custom Implementation for Complex Queries:**

```java
public interface OrderRepositoryCustom {
    List<Order> findComplexOrders(ComplexQueryParams params);
}

public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    @Autowired
    private DynamoDBTemplate template;

    @Override
    public List<Order> findComplexOrders(ComplexQueryParams params) {
        // Build custom DynamoDBQueryExpression or DynamoDBScanExpression
        // Execute using template
    }
}
```

## Best Practices

1. **Prefer Queries Over Scans** - Design tables with appropriate keys
2. **Use GSIs for Alternate Access Patterns** - Avoid scans for common queries
3. **Enable Scans Selectively** - Only on specific methods, not repository-wide
4. **Use Slice Instead of Page** - When total count is not needed
5. **Limit Result Sets** - Use Top/First or Pageable to limit results
6. **Index Wisely** - Create GSIs for frequently queried non-key attributes
7. **Monitor Costs** - Scan operations can be expensive

## See Also

- [Repository Interfaces](repository-interfaces.html)
- [Annotations](annotations.html)
- [DynamoDBTemplate](dynamodb-template.html)
- [Configuration](configuration.html)
