---
layout: page
parent: Troubleshooting
grand_parent: Documentation
title: FAQ
nav_order: 5
---

# Frequently Asked Questions

Common questions about Spring Data DynamoDB and their answers.

## Table of Contents

{: .no_toc .text-delta }

1. TOC
   {:toc}

---

## General Questions

### Q: What version of AWS SDK does this library use?

**A:** Spring Data DynamoDB currently uses AWS SDK v1. Support for AWS SDK v2 is planned for future releases.

### Q: Can I use this library with DynamoDB Local for testing?

**A:** Yes! Configure your DynamoDB client to point to localhost:

```java
@Bean
public AmazonDynamoDB amazonDynamoDB() {
    return AmazonDynamoDBClientBuilder.standard()
        .withEndpointConfiguration(
            new AwsClientBuilder.EndpointConfiguration(
                "http://localhost:8000", "us-east-1"))
        .withCredentials(new AWSStaticCredentialsProvider(
            new BasicAWSCredentials("dummy", "dummy")))
        .build();
}
```

### Q: Is this library compatible with Spring Boot 3.x?

**A:** Check the [compatibility matrix](../getting-started/compatibility) for supported Spring Boot versions. The
library is actively maintained to support recent Spring Boot releases.

### Q: How do I enable debug logging?

**A:** Add to your `application.properties`:

```properties
logging.level.org.socialsignin.spring.data.dynamodb=DEBUG
logging.level.com.amazonaws.request=DEBUG
```

---

## Configuration Questions

### Q: How do I configure multiple DynamoDB clients?

**A:** Use `@Qualifier` annotations:

```java
@Configuration
@EnableDynamoDBRepositories(
    basePackages = "com.example.repo.primary",
    dynamoDBMapperRef = "primaryDynamoDBMapper"
)
public class PrimaryDynamoDBConfig {

    @Bean(name = "primaryDynamoDB")
    @Primary
    public AmazonDynamoDB primaryDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .build();
    }

    @Bean(name = "primaryDynamoDBMapper")
    @Primary
    public DynamoDBMapper primaryDynamoDBMapper(
            @Qualifier("primaryDynamoDB") AmazonDynamoDB amazonDynamoDB) {
        return new DynamoDBMapper(amazonDynamoDB);
    }
}

@Configuration
@EnableDynamoDBRepositories(
    basePackages = "com.example.repo.secondary",
    dynamoDBMapperRef = "secondaryDynamoDBMapper"
)
public class SecondaryDynamoDBConfig {

    @Bean(name = "secondaryDynamoDB")
    public AmazonDynamoDB secondaryDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.EU_WEST_1)
            .build();
    }

    @Bean(name = "secondaryDynamoDBMapper")
    public DynamoDBMapper secondaryDynamoDBMapper(
            @Qualifier("secondaryDynamoDB") AmazonDynamoDB amazonDynamoDB) {
        return new DynamoDBMapper(amazonDynamoDB);
    }
}
```

### Q: Can I use a custom table name prefix?

**A:** Yes, configure `DynamoDBMapperConfig`:

```java
@Bean
public DynamoDBMapperConfig dynamoDBMapperConfig() {
    return DynamoDBMapperConfig.builder()
        .withTableNameOverride(
            DynamoDBMapperConfig.TableNameOverride
                .withTableNamePrefix("dev_"))
        .build();
}
```

Now `@DynamoDBTable(tableName = "User")` will use table `dev_User`.

### Q: How do I configure consistent reads?

**A:** Use the `@ConsistentRead` annotation:

```java
public interface UserRepository extends CrudRepository<User, String> {

    @ConsistentRead
    Optional<User> findById(String id);

    @ConsistentRead
    List<User> findByEmail(String email);
}
```

Or configure globally:

```java
@Bean
public DynamoDBMapperConfig dynamoDBMapperConfig() {
    return DynamoDBMapperConfig.builder()
        .withConsistentReads(ConsistentReads.CONSISTENT)
        .build();
}
```

---

## Entity Mapping Questions

### Q: How do I map Java 8 date/time types?

**A:** Spring Data DynamoDB includes converters for Java 8 date/time types:

```java
@DynamoDBTable(tableName = "Event")
public class Event {

    private String id;
    private Instant createdAt;
    private LocalDate eventDate;
    private LocalDateTime startTime;

    @DynamoDBHashKey
    public String getId() {
        return id;
    }

    @DynamoDBTypeConverted(converter = InstantConverter.class)
    public Instant getCreatedAt() {
        return createdAt;
    }

    @DynamoDBTypeConverted(converter = LocalDateConverter.class)
    public LocalDate getEventDate() {
        return eventDate;
    }

    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    public LocalDateTime getStartTime() {
        return startTime;
    }

    // setters...
}
```

### Q: How do I store nested objects?

**A:** Use `@DynamoDBDocument`:

```java
@DynamoDBTable(tableName = "User")
public class User {

    private String id;
    private Address address;

    @DynamoDBHashKey
    public String getId() {
        return id;
    }

    @DynamoDBAttribute
    public Address getAddress() {
        return address;
    }

    // setters...
}

@DynamoDBDocument
public class Address {

    private String street;
    private String city;
    private String zipCode;

    @DynamoDBAttribute
    public String getStreet() {
        return street;
    }

    @DynamoDBAttribute
    public String getCity() {
        return city;
    }

    @DynamoDBAttribute
    public String getZipCode() {
        return zipCode;
    }

    // setters...
}
```

### Q: How do I store collections?

**A:** DynamoDB supports Lists, Sets, and Maps:

```java
@DynamoDBTable(tableName = "User")
public class User {

    private String id;
    private List<String> tags;
    private Set<String> permissions;
    private Map<String, String> metadata;

    @DynamoDBHashKey
    public String getId() {
        return id;
    }

    @DynamoDBAttribute
    public List<String> getTags() {
        return tags;
    }

    @DynamoDBAttribute
    public Set<String> getPermissions() {
        return permissions;
    }

    @DynamoDBAttribute
    public Map<String, String> getMetadata() {
        return metadata;
    }

    // setters...
}
```

### Q: Can I use enums?

**A:** Yes, enums are automatically converted to strings:

```java
public enum UserStatus {
    ACTIVE, INACTIVE, SUSPENDED
}

@DynamoDBTable(tableName = "User")
public class User {

    private String id;
    private UserStatus status;

    @DynamoDBHashKey
    public String getId() {
        return id;
    }

    @DynamoDBTypeConvertedEnum
    @DynamoDBAttribute
    public UserStatus getStatus() {
        return status;
    }

    // setters...
}
```

---

## Query Questions

### Q: Can I use OR conditions in queries?

**A:** OR conditions require scan operations:

```java
public interface UserRepository extends CrudRepository<User, String> {

    // ❌ Not supported in query methods
    // List<User> findByNameOrEmail(String name, String email);

    // ✅ Use scan with @EnableScan
    @EnableScan
    @Query(scanExpression = "#n = :name OR email = :email")
    List<User> findByNameOrEmail(
        @Param("name") String name,
        @Param("email") String email);
}
```

### Q: How do I query with BETWEEN?

**A:** Use the `Between` keyword:

```java
public interface OrderRepository extends CrudRepository<Order, OrderId> {

    // Query with range key between dates
    List<Order> findByCustomerIdAndOrderDateBetween(
        String customerId, String startDate, String endDate);

    // Query with non-key attribute (requires scan)
    @EnableScan
    List<Order> findByAmountBetween(Double minAmount, Double maxAmount);
}
```

### Q: Can I use LIKE queries?

**A:** Use `StartingWith` or `Containing`:

```java
public interface UserRepository extends CrudRepository<User, String> {

    // Range key prefix match
    List<User> findByIdAndNameStartingWith(String id, String namePrefix);

    // Requires scan for non-key attributes
    @EnableScan
    List<User> findByNameStartingWith(String namePrefix);

    // Contains requires scan
    @EnableScan
    List<User> findByNameContaining(String substring);
}
```

### Q: How do I query with IN?

**A:** Use the `In` keyword:

```java
public interface UserRepository extends CrudRepository<User, String> {

    // Batch get by IDs
    List<User> findByIdIn(List<String> ids);

    // Scan for non-key attributes
    @EnableScan
    List<User> findByStatusIn(List<String> statuses);
}
```

### Q: Can I use COUNT queries?

**A:** Yes, but count requires scanning:

```java
public interface UserRepository extends CrudRepository<User, String> {

    @EnableScan
    long count();

    @EnableScan
    long countByStatus(String status);
}
```

---

## Performance Questions

### Q: What's the difference between Query and Scan?

**A:**

| Operation       | Query                           | Scan                        |
|-----------------|---------------------------------|-----------------------------|
| **Uses**        | Hash key (+ optional range key) | Filter expression           |
| **Performance** | Fast, efficient                 | Slow, expensive             |
| **Cost**        | Reads only matching items       | Reads entire table          |
| **When to use** | Key-based lookups               | Non-key attribute filtering |

```java
// ✅ Query - Fast
List<Order> findByCustomerId(String customerId);

// ❌ Scan - Slow
@EnableScan
List<Order> findByStatus(String status);
```

### Q: Should I use Page or Slice?

**A:** Use `Slice` to avoid expensive count queries:

```java
// ❌ Page requires additional count query
Page<User> findAll(Pageable pageable);

// ✅ Slice doesn't perform count
Slice<User> findAll(Pageable pageable);
```

### Q: How do I optimize large result sets?

**A:** Use pagination:

```java
@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public void processAllUsers() {
        Pageable pageable = PageRequest.of(0, 100);
        Slice<User> slice;

        do {
            slice = repository.findAll(pageable);

            // Process current page
            slice.getContent().forEach(this::processUser);

            pageable = slice.nextPageable();

        } while (slice.hasNext());
    }
}
```

### Q: What's the maximum item size?

**A:** DynamoDB has a 400KB item size limit. For larger data:

```java
// Store large data in S3, reference in DynamoDB
@DynamoDBTable(tableName = "Document")
public class Document {

    private String id;
    private String s3Bucket;
    private String s3Key;

    @DynamoDBHashKey
    public String getId() {
        return id;
    }

    @DynamoDBAttribute
    public String getS3Bucket() {
        return s3Bucket;
    }

    @DynamoDBAttribute
    public String getS3Key() {
        return s3Key;
    }

    // setters...
}
```

---

## Transaction Questions

### Q: Does Spring Data DynamoDB support transactions?

**A:** Yes, using `DynamoDBTransactionMapper`:

```java
@Service
public class TransactionalService {

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    public void transferFunds(String fromAccount, String toAccount, double amount) {
        TransactionWriteRequest request = new TransactionWriteRequest()
            .addUpdate(
                new Update()
                    .withTableName("Account")
                    .withKey(Collections.singletonMap(
                        "id", new AttributeValue(fromAccount)))
                    .withUpdateExpression("SET balance = balance - :amount")
                    .withExpressionAttributeValues(Collections.singletonMap(
                        ":amount", new AttributeValue().withN(String.valueOf(amount)))))
            .addUpdate(
                new Update()
                    .withTableName("Account")
                    .withKey(Collections.singletonMap(
                        "id", new AttributeValue(toAccount)))
                    .withUpdateExpression("SET balance = balance + :amount")
                    .withExpressionAttributeValues(Collections.singletonMap(
                        ":amount", new AttributeValue().withN(String.valueOf(amount)))));

        amazonDynamoDB.transactWriteItems(request);
    }
}
```

### Q: Can I use @Transactional with DynamoDB?

**A:** `@Transactional` is for relational databases. Use DynamoDB transactions directly:

```java
// ❌ @Transactional doesn't work with DynamoDB
@Transactional
public void saveUser(User user) {
    repository.save(user);
}

// ✅ Use DynamoDB transactions
public void saveUserAndOrder(User user, Order order) {
    TransactionWriteRequest request = new TransactionWriteRequest()
        .addPut(createPut(user))
        .addPut(createPut(order));

    amazonDynamoDB.transactWriteItems(request);
}
```

---

## Testing Questions

### Q: How do I set up integration tests?

**A:** Use DynamoDB Local:

```java
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserRepositoryIT {

    @Autowired
    private UserRepository repository;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @BeforeAll
    public void setup() {
        System.setProperty("sqlite4java.library.path", "target/lib");
        createTable();
    }

    private void createTable() {
        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);
        CreateTableRequest request = mapper.generateCreateTableRequest(User.class);
        request.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));

        try {
            amazonDynamoDB.createTable(request);
        } catch (ResourceInUseException e) {
            // Table already exists
        }
    }

    @BeforeEach
    public void cleanup() {
        repository.deleteAll();
    }

    @Test
    public void testSaveAndFind() {
        User user = new User();
        user.setId("123");
        user.setName("John");

        repository.save(user);

        Optional<User> found = repository.findById("123");
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getName());
    }
}
```

### Q: How do I mock DynamoDB in unit tests?

**A:** Use Mockito:

```java
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;

    @Test
    public void testFindUser() {
        User user = new User();
        user.setId("123");
        user.setName("John");

        when(repository.findById("123"))
            .thenReturn(Optional.of(user));

        Optional<User> result = service.findUser("123");

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getName());
        verify(repository).findById("123");
    }
}
```

---

## Migration Questions

### Q: How do I migrate from JPA to DynamoDB?

**A:** Key differences to consider:

| JPA          | DynamoDB                            |
|--------------|-------------------------------------|
| `@Entity`    | `@DynamoDBTable`                    |
| `@Id`        | `@DynamoDBHashKey`                  |
| `@Column`    | `@DynamoDBAttribute`                |
| `@OneToMany` | Denormalize or use references       |
| `JOIN`       | Not supported - denormalize         |
| Transactions | Limited - use DynamoDB transactions |

```java
// JPA
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "user_name")
    private String name;
}

// DynamoDB
@DynamoDBTable(tableName = "User")
public class User {
    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    private String id;

    @DynamoDBAttribute(attributeName = "user_name")
    private String name;
}
```

### Q: How do I handle schema changes?

**A:** DynamoDB is schemaless - update your entity classes:

```java
// Old version
@DynamoDBTable(tableName = "User")
public class User {
    private String id;
    private String name;
    // ...
}

// New version - just add fields
@DynamoDBTable(tableName = "User")
public class User {
    private String id;
    private String name;
    private String email;  // New field
    private Instant createdAt;  // New field
    // ...
}
```

Old items will have null values for new fields until updated.

---

## Error Handling Questions

### Q: How do I handle optimistic locking?

**A:** Use `@DynamoDBVersionAttribute`:

```java
@DynamoDBTable(tableName = "User")
public class User {

    private String id;
    private String name;
    private Long version;

    @DynamoDBHashKey
    public String getId() {
        return id;
    }

    @DynamoDBAttribute
    public String getName() {
        return name;
    }

    @DynamoDBVersionAttribute
    public Long getVersion() {
        return version;
    }

    // setters...
}

// Usage
try {
    repository.save(user);
} catch (ConditionalCheckFailedException e) {
    // Version conflict - item was modified by another process
    throw new OptimisticLockingException("User was modified", e);
}
```

### Q: How do I handle validation errors?

**A:** Use Bean Validation:

```java
@DynamoDBTable(tableName = "User")
public class User {

    private String id;

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @Email
    private String email;

    @Min(18)
    @Max(120)
    private Integer age;

    // getters/setters...
}

@Service
public class UserService {

    @Autowired
    private Validator validator;

    @Autowired
    private UserRepository repository;

    public User saveUser(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return repository.save(user);
    }
}
```

---

## Next Steps

- Review [Common Errors](common-errors) for specific error solutions
- Check [Performance Tuning](performance-tuning) for optimization tips
- See [Scan Exceptions](scan-exceptions) for scan-related questions
- Read [Batch Operation Failures](batch-operation-failures) for batch handling
