---
layout: page
parent: API Reference
grand_parent: Documentation
title: Annotations
nav_order: 3
---

# Annotations API Reference

Complete reference for all Spring Data DynamoDB annotations.

## Configuration Annotations

### @EnableDynamoDBRepositories

Enables DynamoDB repository support in Spring configuration.

**Package:** `org.socialsignin.spring.data.dynamodb.repository.config`

**Target:** TYPE (class level)

**Declaration:**

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(DynamoDBRepositoriesRegistrar.class)
public @interface EnableDynamoDBRepositories
```

**Attributes:**

#### basePackages

```java
String[] basePackages() default {}
```

Base packages to scan for repository interfaces.

**Example:**

```java
@Configuration
@EnableDynamoDBRepositories(basePackages = "com.example.repositories")
public class DynamoDBConfig {
}
```

#### value

```java
String[] value() default {}
```

Alias for `basePackages()`. Allows concise declarations.

**Example:**

```java
@EnableDynamoDBRepositories("com.example.repositories")
public class DynamoDBConfig {
}
```

#### basePackageClasses

```java
Class<?>[] basePackageClasses() default {}
```

Type-safe alternative to basePackages. Scans the package of each specified class.

**Example:**

```java
@EnableDynamoDBRepositories(basePackageClasses = UserRepository.class)
public class DynamoDBConfig {
}
```

#### amazonDynamoDBRef

```java
String amazonDynamoDBRef() default ""
```

Bean name of the AmazonDynamoDB client to use.

**Example:**

```java
@EnableDynamoDBRepositories(amazonDynamoDBRef = "amazonDynamoDB")
public class DynamoDBConfig {

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .build();
    }
}
```

#### dynamoDBMapperRef

```java
String dynamoDBMapperRef() default ""
```

Bean name of the DynamoDBMapper to use.

**Example:**

```java
@EnableDynamoDBRepositories(
    amazonDynamoDBRef = "amazonDynamoDB",
    dynamoDBMapperRef = "dynamoDBMapper"
)
public class DynamoDBConfig {

    @Bean
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB) {
        return new DynamoDBMapper(amazonDynamoDB);
    }
}
```

#### dynamoDBMapperConfigRef

```java
String dynamoDBMapperConfigRef() default ""
```

Bean name of the DynamoDBMapperConfig to use.

**Example:**

```java
@EnableDynamoDBRepositories(
    dynamoDBMapperConfigRef = "dynamoDBMapperConfig"
)
public class DynamoDBConfig {

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        return DynamoDBMapperConfig.builder()
            .withTableNameOverride(
                TableNameOverride.withTableNamePrefix("dev_")
            )
            .build();
    }
}
```

#### dynamoDBOperationsRef

```java
String dynamoDBOperationsRef() default ""
```

Bean name of the DynamoDBOperations/DynamoDBTemplate to use.

**Example:**

```java
@EnableDynamoDBRepositories(
    dynamoDBOperationsRef = "dynamoDBTemplate"
)
public class DynamoDBConfig {

    @Bean
    public DynamoDBOperations dynamoDBTemplate(
            AmazonDynamoDB amazonDynamoDB,
            DynamoDBMapper mapper,
            DynamoDBMapperConfig config) {
        return new DynamoDBTemplate(amazonDynamoDB, mapper, config);
    }
}
```

#### mappingContextRef

```java
String mappingContextRef() default ""
```

Bean name of the DynamoDBMappingContext to use.

**Example:**

```java
@EnableDynamoDBRepositories(mappingContextRef = "dynamoDBMappingContext")
public class DynamoDBConfig {

    @Bean
    public DynamoDBMappingContext dynamoDBMappingContext() {
        return new DynamoDBMappingContext();
    }
}
```

#### repositoryImplementationPostfix

```java
String repositoryImplementationPostfix() default "Impl"
```

Postfix for custom repository implementation classes.

**Example:**

```java
@EnableDynamoDBRepositories(repositoryImplementationPostfix = "Custom")
public class DynamoDBConfig {
}

// Implementation class should be named UserRepositoryCustom
public class UserRepositoryCustom implements UserRepositoryCustomMethods {
    // Custom implementation
}
```

#### includeFilters

```java
Filter[] includeFilters() default {}
```

Filters to include specific repository types.

**Example:**

```java
@EnableDynamoDBRepositories(
    includeFilters = @Filter(type = FilterType.ANNOTATION, classes = MyRepositoryMarker.class)
)
public class DynamoDBConfig {
}
```

#### excludeFilters

```java
Filter[] excludeFilters() default {}
```

Filters to exclude specific repository types.

**Example:**

```java
@EnableDynamoDBRepositories(
    excludeFilters = @Filter(type = FilterType.REGEX, pattern = ".*Test.*Repository")
)
public class DynamoDBConfig {
}
```

#### queryLookupStrategy

```java
Key queryLookupStrategy() default Key.CREATE_IF_NOT_FOUND
```

Strategy for looking up query methods.

**Values:**

- `CREATE` - Only create queries from method names
- `USE_DECLARED_QUERY` - Only use declared queries
- `CREATE_IF_NOT_FOUND` - Try declared, then create (default)

#### repositoryFactoryBeanClass

```java
Class<?> repositoryFactoryBeanClass() default DynamoDBRepositoryFactoryBean.class
```

Factory bean class for repository instances.

#### considerNestedRepositories

```java
boolean considerNestedRepositories() default false
```

Whether to discover nested repository interfaces (e.g., inner classes).

**Complete Example:**

```java
@Configuration
@EnableDynamoDBRepositories(
    basePackages = "com.example.repositories",
    amazonDynamoDBRef = "amazonDynamoDB",
    dynamoDBMapperConfigRef = "dynamoDBMapperConfig"
)
public class DynamoDBConfig {

    @Value("${amazon.dynamodb.endpoint}")
    private String endpoint;

    @Value("${amazon.aws.region}")
    private String region;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(
                new EndpointConfiguration(endpoint, region)
            )
            .build();
    }

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        return DynamoDBMapperConfig.builder()
            .withSaveBehavior(SaveBehavior.UPDATE)
            .withConsistentReads(ConsistentReads.CONSISTENT)
            .withTableNameOverride(
                TableNameOverride.withTableNamePrefix("prod_")
            )
            .build();
    }
}
```

## Repository Annotations

### @EnableScan

Enables table scan operations for methods that would otherwise be prohibited.

**Package:** `org.socialsignin.spring.data.dynamodb.repository`

**Target:** TYPE, METHOD, ANNOTATION_TYPE

**Declaration:**

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Documented
public @interface EnableScan
```

**Usage:**

Scans are expensive operations in DynamoDB and are disabled by default for:

- `findAll()`
- `count()`
- `deleteAll()`
- Query methods that filter on non-key attributes

**Example - Repository Level:**

```java
@EnableScan
public interface UserRepository extends DynamoDBCrudRepository<User, String> {
    // All scan operations are enabled
    List<User> findAll();
    long count();
    void deleteAll();
}
```

**Example - Method Level:**

```java
public interface UserRepository extends DynamoDBCrudRepository<User, String> {

    // This specific method is allowed to scan
    @EnableScan
    List<User> findAll();

    // This will throw exception (scan not enabled)
    // long count();

    // Custom query method requiring scan (filters on non-key attribute)
    @EnableScan
    List<User> findByEmail(String email);
}
```

**Example - Annotation Type:**

```java
@EnableScan
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AllowExpensiveQuery {
}

public interface UserRepository extends DynamoDBCrudRepository<User, String> {

    @AllowExpensiveQuery
    List<User> findByStatus(String status);
}
```

**Best Practices:**

1. Enable scans only when absolutely necessary
2. Prefer method-level over repository-level
3. Consider using Global Secondary Indexes instead
4. Monitor costs when scans are enabled

### @EnableScanCount

Enables count operations that require scanning.

**Package:** `org.socialsignin.spring.data.dynamodb.repository`

**Target:** TYPE, METHOD

**Declaration:**

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface EnableScanCount
```

**Usage:**

Specifically enables count operations that would require a scan, without enabling full scans.

**Example:**

```java
public interface UserRepository extends DynamoDBCrudRepository<User, String> {

    @EnableScanCount
    long countByStatus(String status);

    // Regular count requires @EnableScan
    // long count();
}
```

## Entity Annotations (AWS SDK)

Spring Data DynamoDB uses AWS SDK annotations for entity mapping.

### @DynamoDBTable

Marks a class as a DynamoDB entity.

**Package:** `com.amazonaws.services.dynamodbv2.datamodeling`

**Example:**

```java
@DynamoDBTable(tableName = "users")
public class User {
    private String id;
    private String name;

    @DynamoDBHashKey
    public String getId() {
        return id;
    }

    // Other fields
}
```

### @DynamoDBHashKey

Marks the hash key (partition key) attribute.

**Example:**

```java
@DynamoDBHashKey(attributeName = "userId")
public String getId() {
    return id;
}
```

### @DynamoDBRangeKey

Marks the range key (sort key) attribute.

**Example:**

```java
@DynamoDBTable(tableName = "orders")
public class Order {

    @DynamoDBHashKey
    public String getCustomerId() {
        return customerId;
    }

    @DynamoDBRangeKey(attributeName = "orderDate")
    public String getOrderDate() {
        return orderDate;
    }
}
```

### @DynamoDBIndexHashKey

Marks the hash key for a Global Secondary Index.

**Example:**

```java
@DynamoDBTable(tableName = "users")
public class User {

    @DynamoDBHashKey
    public String getId() {
        return id;
    }

    @DynamoDBIndexHashKey(
        globalSecondaryIndexName = "email-index",
        attributeName = "email"
    )
    public String getEmail() {
        return email;
    }
}
```

### @DynamoDBIndexRangeKey

Marks the range key for a Global or Local Secondary Index.

**Example:**

```java
@DynamoDBIndexRangeKey(
    globalSecondaryIndexName = "status-created-index",
    localSecondaryIndexName = "lsi-created"
)
public String getCreatedDate() {
    return createdDate;
}
```

### @DynamoDBAttribute

Maps a property to a DynamoDB attribute with custom name.

**Example:**

```java
@DynamoDBAttribute(attributeName = "user_name")
public String getName() {
    return name;
}
```

### @DynamoDBIgnore

Excludes a property from DynamoDB mapping.

**Example:**

```java
@DynamoDBIgnore
public String getTransientValue() {
    return transientValue;
}
```

### @DynamoDBVersionAttribute

Marks an attribute for optimistic locking.

**Example:**

```java
@DynamoDBVersionAttribute
public Long getVersion() {
    return version;
}
```

### @DynamoDBAutoGeneratedKey

Auto-generates the key value if not set.

**Example:**

```java
@DynamoDBHashKey
@DynamoDBAutoGeneratedKey
public String getId() {
    return id;
}
```

### @DynamoDBAutoGeneratedTimestamp

Auto-generates timestamp on save.

**Example:**

```java
@DynamoDBAutoGeneratedTimestamp(strategy = DynamoDBAutoGenerateStrategy.CREATE)
public Date getCreatedAt() {
    return createdAt;
}

@DynamoDBAutoGeneratedTimestamp(strategy = DynamoDBAutoGenerateStrategy.ALWAYS)
public Date getUpdatedAt() {
    return updatedAt;
}
```

### @DynamoDBTypeConverted

Uses a custom converter for the attribute.

**Example:**

```java
@DynamoDBTypeConverted(converter = StatusConverter.class)
public Status getStatus() {
    return status;
}

public static class StatusConverter implements DynamoDBTypeConverter<String, Status> {

    @Override
    public String convert(Status status) {
        return status.name();
    }

    @Override
    public Status unconvert(String s) {
        return Status.valueOf(s);
    }
}
```

### @DynamoDBDocument

Marks a class as an embedded document.

**Example:**

```java
@DynamoDBDocument
public class Address {
    private String street;
    private String city;
    private String zipCode;

    // Getters and setters
}

@DynamoDBTable(tableName = "users")
public class User {

    @DynamoDBHashKey
    public String getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }
}
```

## Query Method Annotations

### @Query (Not Supported)

Spring Data DynamoDB does not support @Query annotation. Use derived query methods or custom implementations instead.

### @Projection

Limits which attributes are retrieved (projection expression).

**Package:** Custom annotation (if implemented in project)

**Example:**

```java
public interface UserRepository extends DynamoDBCrudRepository<User, String> {

    @Projection(attributes = {"id", "name", "email"})
    List<User> findByStatus(String status);
}
```

### @ConsistentRead

Forces consistent read operations.

**Example:**

```java
public interface UserRepository extends DynamoDBCrudRepository<User, String> {

    @ConsistentRead
    Optional<User> findByEmail(String email);
}
```

## Lifecycle Event Annotations

Use Spring's @EventListener to handle DynamoDB lifecycle events.

**Example:**

```java
@Component
public class DynamoDBEventListener {

    @EventListener
    public void handleBeforeSave(BeforeSaveEvent<?> event) {
        Object entity = event.getSource();
        System.out.println("About to save: " + entity);
    }

    @EventListener
    public void handleAfterLoad(AfterLoadEvent<?> event) {
        Object entity = event.getSource();
        System.out.println("Loaded: " + entity);
    }

    @EventListener
    public void handleAfterSave(AfterSaveEvent<?> event) {
        Object entity = event.getSource();
        System.out.println("Saved: " + entity);
    }

    @EventListener
    public void handleBeforeDelete(BeforeDeleteEvent<?> event) {
        Object entity = event.getSource();
        System.out.println("About to delete: " + entity);
    }

    @EventListener
    public void handleAfterDelete(AfterDeleteEvent<?> event) {
        Object entity = event.getSource();
        System.out.println("Deleted: " + entity);
    }

    @EventListener
    public void handleAfterQuery(AfterQueryEvent<?> event) {
        System.out.println("Query executed");
    }

    @EventListener
    public void handleAfterScan(AfterScanEvent<?> event) {
        System.out.println("Scan executed");
    }
}
```

## Complete Entity Example

```java
@DynamoDBTable(tableName = "orders")
public class Order {

    @Id
    private OrderId id;

    private String status;
    private BigDecimal amount;
    private Date createdAt;
    private Date updatedAt;
    private Long version;
    private Address shippingAddress;

    @DynamoDBHashKey(attributeName = "customerId")
    public String getCustomerId() {
        return id != null ? id.getCustomerId() : null;
    }

    public void setCustomerId(String customerId) {
        if (this.id == null) {
            this.id = new OrderId();
        }
        this.id.setCustomerId(customerId);
    }

    @DynamoDBRangeKey(attributeName = "orderDate")
    public String getOrderDate() {
        return id != null ? id.getOrderDate() : null;
    }

    public void setOrderDate(String orderDate) {
        if (this.id == null) {
            this.id = new OrderId();
        }
        this.id.setOrderDate(orderDate);
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "status-index")
    @DynamoDBAttribute(attributeName = "order_status")
    public String getStatus() {
        return status;
    }

    @DynamoDBAttribute(attributeName = "total_amount")
    public BigDecimal getAmount() {
        return amount;
    }

    @DynamoDBAutoGeneratedTimestamp(strategy = DynamoDBAutoGenerateStrategy.CREATE)
    public Date getCreatedAt() {
        return createdAt;
    }

    @DynamoDBAutoGeneratedTimestamp(strategy = DynamoDBAutoGenerateStrategy.ALWAYS)
    public Date getUpdatedAt() {
        return updatedAt;
    }

    @DynamoDBVersionAttribute
    public Long getVersion() {
        return version;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    // Setters omitted for brevity
}
```

## See Also

- [Repository Interfaces](repository-interfaces.html)
- [Configuration Options](configuration.html)
- [Query Methods](query-methods.html)
- [AWS DynamoDB Documentation](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/)
