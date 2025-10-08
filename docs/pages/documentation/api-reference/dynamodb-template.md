---
layout: page
parent: API Reference
grand_parent: Documentation
title: DynamoDBTemplate
nav_order: 1
---

# DynamoDBTemplate API Reference

Central class for DynamoDB operations in Spring Data DynamoDB, similar to JdbcTemplate in Spring JDBC.

## Class Declaration

```java
package org.socialsignin.spring.data.dynamodb.core;

public class DynamoDBTemplate
    implements DynamoDBOperations, ApplicationContextAware
```

## Constructor

### DynamoDBTemplate

```java
@Autowired
public DynamoDBTemplate(
    AmazonDynamoDB amazonDynamoDB,
    DynamoDBMapper dynamoDBMapper,
    DynamoDBMapperConfig dynamoDBMapperConfig
)
```

Creates a new DynamoDBTemplate instance.

**Parameters:**

- `amazonDynamoDB` - AWS DynamoDB client (must not be null)
- `dynamoDBMapper` - AWS DynamoDB mapper (must not be null)
- `dynamoDBMapperConfig` - Mapper configuration (must not be null)

**Throws:**

- `IllegalArgumentException` - if any parameter is null

**Example:**

```java
@Configuration
public class DynamoDBConfig {

    @Bean
    public DynamoDBTemplate dynamoDBTemplate(
            AmazonDynamoDB amazonDynamoDB,
            DynamoDBMapper dynamoDBMapper,
            DynamoDBMapperConfig config) {
        return new DynamoDBTemplate(amazonDynamoDB, dynamoDBMapper, config);
    }
}
```

## Load Operations

### load (Hash Key Only)

```java
<T> T load(Class<T> domainClass, Object hashKey)
```

Loads an entity by hash key.

**Parameters:**

- `domainClass` - Entity class
- `hashKey` - Hash key value

**Returns:** Entity instance or null if not found

**Events:** Publishes `AfterLoadEvent` if entity is found

**Example:**

```java
User user = template.load(User.class, "user123");
if (user != null) {
    // Process user
}
```

### load (Hash and Range Key)

```java
<T> T load(Class<T> domainClass, Object hashKey, Object rangeKey)
```

Loads an entity by hash and range key.

**Parameters:**

- `domainClass` - Entity class
- `hashKey` - Hash key value
- `rangeKey` - Range key value

**Returns:** Entity instance or null if not found

**Events:** Publishes `AfterLoadEvent` if entity is found

**Example:**

```java
Order order = template.load(Order.class, "customer123", "2024-01-15");
```

### batchLoad

```java
<T> List<T> batchLoad(Map<Class<?>, List<KeyPair>> itemsToGet)
```

Loads multiple entities in batch operations.

**Parameters:**

- `itemsToGet` - Map of entity classes to key pairs

**Returns:** List of loaded entities

**Events:** Publishes `AfterLoadEvent` for each loaded entity

**Example:**

```java
List<KeyPair> userKeys = Arrays.asList(
    new KeyPair().withHashKey("user1"),
    new KeyPair().withHashKey("user2")
);

Map<Class<?>, List<KeyPair>> itemsToGet =
    Collections.singletonMap(User.class, userKeys);

List<User> users = template.batchLoad(itemsToGet);
```

## Save Operations

### save

```java
<T> T save(T entity)
```

Saves an entity to DynamoDB.

**Parameters:**

- `entity` - Entity to save

**Returns:** The saved entity

**Events:**

- Publishes `BeforeSaveEvent` before saving
- Publishes `AfterSaveEvent` after saving

**Example:**

```java
User user = new User();
user.setId("user123");
user.setName("John Doe");
user.setEmail("john@example.com");

template.save(user);
```

### batchSave

```java
List<FailedBatch> batchSave(Iterable<?> entities)
```

Saves multiple entities in batch operations.

**Parameters:**

- `entities` - Entities to save

**Returns:** List of failed batches (empty if all succeeded)

**Events:**

- Publishes `BeforeSaveEvent` for each entity before saving
- Publishes `AfterSaveEvent` for each entity after saving

**Example:**

```java
List<User> users = Arrays.asList(
    new User("user1", "Alice"),
    new User("user2", "Bob"),
    new User("user3", "Charlie")
);

List<FailedBatch> failures = template.batchSave(users);

if (!failures.isEmpty()) {
    // Handle failures
    for (FailedBatch batch : failures) {
        System.err.println("Failed: " + batch.getException().getMessage());
    }
}
```

## Delete Operations

### delete

```java
<T> T delete(T entity)
```

Deletes an entity from DynamoDB.

**Parameters:**

- `entity` - Entity to delete

**Returns:** The deleted entity

**Events:**

- Publishes `BeforeDeleteEvent` before deleting
- Publishes `AfterDeleteEvent` after deleting

**Example:**

```java
User user = template.load(User.class, "user123");
if (user != null) {
    template.delete(user);
}
```

### batchDelete

```java
List<FailedBatch> batchDelete(Iterable<?> entities)
```

Deletes multiple entities in batch operations.

**Parameters:**

- `entities` - Entities to delete

**Returns:** List of failed batches (empty if all succeeded)

**Events:**

- Publishes `BeforeDeleteEvent` for each entity before deleting
- Publishes `AfterDeleteEvent` for each entity after deleting

**Example:**

```java
List<User> usersToDelete = template.scan(
    User.class,
    new DynamoDBScanExpression()
        .withFilterExpression("inactive = :val")
        .withExpressionAttributeValues(
            Collections.singletonMap(":val", new AttributeValue().withBOOL(true))
        )
);

List<FailedBatch> failures = template.batchDelete(usersToDelete);
```

## Query Operations

### query (with DynamoDBQueryExpression)

```java
<T> PaginatedQueryList<T> query(
    Class<T> domainClass,
    DynamoDBQueryExpression<T> queryExpression
)
```

Executes a query using DynamoDBQueryExpression.

**Parameters:**

- `domainClass` - Entity class
- `queryExpression` - Query expression

**Returns:** Paginated lazy-loaded query results

**Events:** Publishes `AfterQueryEvent` with results

**Example:**

```java
User hashKeyValue = new User();
hashKeyValue.setId("user123");

DynamoDBQueryExpression<User> query =
    new DynamoDBQueryExpression<User>()
        .withHashKeyValues(hashKeyValue)
        .withRangeKeyCondition("orderDate",
            new Condition()
                .withComparisonOperator(ComparisonOperator.BETWEEN)
                .withAttributeValueList(
                    new AttributeValue("2024-01-01"),
                    new AttributeValue("2024-12-31")
                )
        );

PaginatedQueryList<User> results = template.query(User.class, query);
```

### query (with QueryRequest)

```java
<T> PaginatedQueryList<T> query(
    Class<T> clazz,
    QueryRequest queryRequest
)
```

Executes a query using low-level QueryRequest.

**Parameters:**

- `clazz` - Entity class
- `queryRequest` - AWS SDK QueryRequest

**Returns:** Paginated lazy-loaded query results

**Example:**

```java
QueryRequest queryRequest = new QueryRequest()
    .withTableName("users")
    .withKeyConditionExpression("userId = :userId")
    .withExpressionAttributeValues(
        Collections.singletonMap(
            ":userId",
            new AttributeValue("user123")
        )
    );

PaginatedQueryList<User> results = template.query(User.class, queryRequest);
```

## Scan Operations

### scan

```java
<T> PaginatedScanList<T> scan(
    Class<T> domainClass,
    DynamoDBScanExpression scanExpression
)
```

Executes a table scan.

**Parameters:**

- `domainClass` - Entity class
- `scanExpression` - Scan expression

**Returns:** Paginated lazy-loaded scan results

**Events:** Publishes `AfterScanEvent` with results

**Warning:** Scans are expensive operations. Use queries when possible.

**Example:**

```java
DynamoDBScanExpression scanExpression =
    new DynamoDBScanExpression()
        .withFilterExpression("age > :minAge")
        .withExpressionAttributeValues(
            Collections.singletonMap(
                ":minAge",
                new AttributeValue().withN("18")
            )
        );

PaginatedScanList<User> results = template.scan(User.class, scanExpression);
```

## Count Operations

### count (Query)

```java
<T> int count(
    Class<T> domainClass,
    DynamoDBQueryExpression<T> queryExpression
)
```

Counts items matching a query.

**Parameters:**

- `domainClass` - Entity class
- `queryExpression` - Query expression

**Returns:** Count of matching items

**Example:**

```java
User hashKeyValue = new User();
hashKeyValue.setId("user123");

DynamoDBQueryExpression<User> query =
    new DynamoDBQueryExpression<User>()
        .withHashKeyValues(hashKeyValue);

int count = template.count(User.class, query);
```

### count (Scan)

```java
<T> int count(
    Class<T> domainClass,
    DynamoDBScanExpression scanExpression
)
```

Counts items matching a scan.

**Parameters:**

- `domainClass` - Entity class
- `scanExpression` - Scan expression

**Returns:** Count of matching items

**Example:**

```java
DynamoDBScanExpression scanExpression =
    new DynamoDBScanExpression()
        .withFilterExpression("status = :status")
        .withExpressionAttributeValues(
            Collections.singletonMap(
                ":status",
                new AttributeValue("ACTIVE")
            )
        );

int activeCount = template.count(User.class, scanExpression);
```

### count (QueryRequest)

```java
<T> int count(Class<T> clazz, QueryRequest queryRequest)
```

Counts items using low-level QueryRequest. Handles pagination automatically.

**Parameters:**

- `clazz` - Entity class
- `queryRequest` - Query request (SELECT will be set to COUNT)

**Returns:** Total count across all pages

**Example:**

```java
QueryRequest queryRequest = new QueryRequest()
    .withTableName("orders")
    .withIndexName("customerId-index")
    .withKeyConditionExpression("customerId = :id")
    .withExpressionAttributeValues(
        Collections.singletonMap(
            ":id",
            new AttributeValue("customer123")
        )
    );

int orderCount = template.count(Order.class, queryRequest);
```

## Utility Operations

### getOverriddenTableName

```java
<T> String getOverriddenTableName(Class<T> domainClass, String tableName)
```

Gets the effective table name after applying configuration overrides.

**Parameters:**

- `domainClass` - Entity class
- `tableName` - Original table name from @DynamoDBTable

**Returns:** Effective table name with prefix or override applied

**Example:**

```java
String effectiveName = template.getOverriddenTableName(
    User.class,
    "users"
);
// May return "dev_users" if prefix is configured
```

### getTableModel

```java
<T> DynamoDBMapperTableModel<T> getTableModel(Class<T> domainClass)
```

Gets the DynamoDB mapper table model for an entity.

**Parameters:**

- `domainClass` - Entity class

**Returns:** Table model with mapping metadata

**Example:**

```java
DynamoDBMapperTableModel<User> model = template.getTableModel(User.class);

// Access table metadata
String tableName = model.tableName();
Collection<String> attributeNames = model.attributeNames();
```

## Lifecycle Events

DynamoDBTemplate publishes lifecycle events through Spring's ApplicationEventPublisher:

| Event             | Published When                   |
|-------------------|----------------------------------|
| BeforeSaveEvent   | Before save() or batchSave()     |
| AfterSaveEvent    | After save() or batchSave()      |
| AfterLoadEvent    | After load() or batchLoad()      |
| BeforeDeleteEvent | Before delete() or batchDelete() |
| AfterDeleteEvent  | After delete() or batchDelete()  |
| AfterQueryEvent   | After query()                    |
| AfterScanEvent    | After scan()                     |

**Example Event Listener:**

```java
@Component
public class DynamoDBEventListener {

    @EventListener
    public void handleAfterLoad(AfterLoadEvent<User> event) {
        User user = event.getSource();
        System.out.println("Loaded user: " + user.getId());
    }

    @EventListener
    public void handleBeforeSave(BeforeSaveEvent<User> event) {
        User user = event.getSource();
        user.setLastModified(Instant.now());
    }
}
```

## Thread Safety

DynamoDBTemplate is thread-safe and can be shared across multiple threads when:

- AmazonDynamoDB client is thread-safe (default AWS SDK clients are)
- DynamoDBMapper is thread-safe (it is, per AWS SDK documentation)
- ApplicationEventPublisher is thread-safe (Spring ApplicationContext is)

## Best Practices

1. **Reuse Template Instances** - Create one DynamoDBTemplate bean and inject it where needed
2. **Use Batch Operations** - Prefer batchSave/batchDelete for multiple items
3. **Handle Batch Failures** - Always check FailedBatch list for batch operations
4. **Prefer Queries Over Scans** - Use query() when you have hash/range key conditions
5. **Listen to Events** - Use lifecycle events for cross-cutting concerns (auditing, validation)
6. **Configure Wisely** - Set appropriate DynamoDBMapperConfig for your use case

## See Also

- [DynamoDBOperations Interface](repository-interfaces.html#dynamodboperations)
- [Configuration Options](configuration.html)
- [Repository Pattern](repository-interfaces.html)
- [Lifecycle Events](annotations.html#lifecycle-events)
