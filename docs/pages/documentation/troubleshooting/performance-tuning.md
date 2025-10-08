---
layout: page
parent: Troubleshooting
grand_parent: Documentation
title: Performance Tuning
nav_order: 4
---

# Performance Tuning

This guide covers performance optimization strategies for Spring Data DynamoDB applications.

## Table of Contents

{: .no_toc .text-delta }

1. TOC
   {:toc}

---

## Common Performance Issues

### ProvisionedThroughputExceededException

**Error:**

```
com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException:
The level of configured provisioned throughput for the table was exceeded.
```

**Cause:** Your application is consuming read/write capacity faster than provisioned.

**Solutions:**

#### 1. Increase Provisioned Capacity

```java
// Using AWS Console or CLI
aws dynamodb update-table \
    --table-name User \
    --provisioned-throughput \
        ReadCapacityUnits=100,WriteCapacityUnits=100
```

#### 2. Enable Auto Scaling

```java
// Configure auto-scaling in AWS Console or via CloudFormation
{
  "Resources": {
    "UserTable": {
      "Type": "AWS::DynamoDB::Table",
      "Properties": {
        "TableName": "User",
        "BillingMode": "PROVISIONED",
        "ProvisionedThroughput": {
          "ReadCapacityUnits": 10,
          "WriteCapacityUnits": 10
        }
      }
    },
    "UserTableReadAutoScaling": {
      "Type": "AWS::ApplicationAutoScaling::ScalableTarget",
      "Properties": {
        "MaxCapacity": 100,
        "MinCapacity": 10,
        "ResourceId": "table/User",
        "RoleARN": {"Fn::GetAtt": ["AutoScalingRole", "Arn"]},
        "ScalableDimension": "dynamodb:table:ReadCapacityUnits",
        "ServiceNamespace": "dynamodb"
      }
    }
  }
}
```

#### 3. Switch to On-Demand Billing

```java
aws dynamodb update-table \
    --table-name User \
    --billing-mode PAY_PER_REQUEST
```

#### 4. Implement Client-Side Rate Limiting

```java
import com.google.common.util.concurrent.RateLimiter;

@Service
public class RateLimitedUserService {

    @Autowired
    private UserRepository repository;

    // Limit to 10 operations per second
    private final RateLimiter rateLimiter = RateLimiter.create(10.0);

    public User saveUser(User user) {
        rateLimiter.acquire(); // Wait for permit
        return repository.save(user);
    }

    public List<User> saveUsers(List<User> users) {
        List<User> saved = new ArrayList<>();

        for (User user : users) {
            rateLimiter.acquire();
            saved.add(repository.save(user));
        }

        return saved;
    }
}
```

#### 5. Use Batch Operations

```java
// ❌ Inefficient - Individual writes
users.forEach(user -> repository.save(user));

// ✅ Efficient - Batch write
repository.saveAll(users);
```

#### 6. Implement Exponential Backoff

```java
@Service
public class ThrottlingAwareService {

    private static final int MAX_RETRIES = 5;
    private static final long BASE_DELAY_MS = 100;

    public void saveWithBackoff(User user, UserRepository repository) {
        int retries = 0;

        while (retries < MAX_RETRIES) {
            try {
                repository.save(user);
                return; // Success

            } catch (ProvisionedThroughputExceededException e) {
                retries++;

                if (retries >= MAX_RETRIES) {
                    throw e;
                }

                long delayMs = BASE_DELAY_MS * (long) Math.pow(2, retries);
                long jitter = ThreadLocalRandom.current().nextLong(0, delayMs / 2);

                try {
                    Thread.sleep(delayMs + jitter);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during backoff", ie);
                }
            }
        }
    }
}
```

---

## Query Optimization

### Use Projections to Reduce Data Transfer

```java
public interface UserRepository extends CrudRepository<User, String> {

    // ❌ Fetches all attributes
    List<User> findByStatus(String status);

    // ✅ Fetches only specified attributes
    @Projection(
        projectionExpression = "id, #n, email",
        expressionAttributeNames = {"#n" = "name"}
    )
    List<User> findByStatus(String status);
}
```

### Optimize Range Key Queries

```java
public interface OrderRepository extends CrudRepository<Order, OrderId> {

    // ❌ Inefficient - Scans all orders for customer
    @EnableScan
    List<Order> findByCustomerIdAndStatus(String customerId, String status);

    // ✅ Efficient - Uses hash + range key query
    List<Order> findByCustomerIdAndOrderDateBetween(
        String customerId, String startDate, String endDate);
}
```

### Use Global Secondary Indexes

```java
@DynamoDBTable(tableName = "Order")
public class Order {

    private String orderId;
    private String customerId;
    private String status;
    private String orderDate;

    @DynamoDBHashKey
    public String getOrderId() {
        return orderId;
    }

    // ✅ Create GSI for frequently queried attribute
    @DynamoDBIndexHashKey(
        globalSecondaryIndexName = "status-date-index",
        attributeName = "status"
    )
    public String getStatus() {
        return status;
    }

    @DynamoDBIndexRangeKey(
        globalSecondaryIndexName = "status-date-index",
        attributeName = "orderDate"
    )
    public String getOrderDate() {
        return orderDate;
    }

    // setters...
}

// Now queries on status use GSI instead of scan
public interface OrderRepository extends CrudRepository<Order, OrderId> {
    List<Order> findByStatus(String status);
    List<Order> findByStatusAndOrderDateBetween(
        String status, String start, String end);
}
```

### Limit Result Sizes

```java
public interface UserRepository extends CrudRepository<User, String> {

    // ❌ Could return millions of records
    @EnableScan
    List<User> findByStatus(String status);

    // ✅ Limit to first 100 results
    @EnableScan
    List<User> findFirst100ByStatus(String status);

    // ✅ Use pagination
    @EnableScan
    Slice<User> findByStatus(String status, Pageable pageable);
}
```

---

## Scan Optimization

### Use Parallel Scans

```java
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;

@Service
public class ParallelScanService {

    @Autowired
    private DynamoDBMapper mapper;

    public List<User> parallelScan(int segments) {
        List<CompletableFuture<List<User>>> futures = new ArrayList<>();

        for (int segment = 0; segment < segments; segment++) {
            final int currentSegment = segment;

            CompletableFuture<List<User>> future = CompletableFuture.supplyAsync(() -> {
                DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                    .withTotalSegments(segments)
                    .withSegment(currentSegment);

                PaginatedScanList<User> results =
                    mapper.scan(User.class, scanExpression);

                return results.stream().collect(Collectors.toList());
            });

            futures.add(future);
        }

        // Combine results from all segments
        return futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }
}
```

### Use FilterExpression for Scans

```java
@Service
public class FilteredScanService {

    @Autowired
    private DynamoDBMapper mapper;

    public List<User> scanWithFilter(String status, int minAge) {
        // ✅ Filter on server side
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":status", new AttributeValue().withS(status));
        eav.put(":minAge", new AttributeValue().withN(String.valueOf(minAge)));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
            .withFilterExpression("#s = :status AND age >= :minAge")
            .withExpressionAttributeNames(
                Collections.singletonMap("#s", "status"))
            .withExpressionAttributeValues(eav);

        return mapper.scan(User.class, scanExpression);
    }
}
```

### Avoid Scans in Hot Paths

```java
// ❌ Don't use scans in high-traffic endpoints
@GetMapping("/users")
public List<User> getAllUsers() {
    return repository.findAll(); // Scans entire table!
}

// ✅ Use pagination with reasonable limits
@GetMapping("/users")
public Slice<User> getUsers(@PageableDefault(size = 50) Pageable pageable) {
    return repository.findAll(pageable);
}

// ✅ Or use specific queries
@GetMapping("/users")
public List<User> getActiveUsers() {
    return repository.findByStatus("ACTIVE"); // Uses GSI
}
```

---

## Pagination Strategies

### Efficient Pagination

```java
@Service
public class PaginationService {

    @Autowired
    private UserRepository repository;

    public Slice<User> getUsers(Pageable pageable) {
        // ✅ Use Slice (no count query) instead of Page
        return repository.findByStatus("ACTIVE", pageable);
    }

    public void processAllUsers() {
        Pageable pageable = PageRequest.of(0, 100);
        Slice<User> slice;

        do {
            slice = repository.findByStatus("ACTIVE", pageable);

            // Process current page
            slice.getContent().forEach(this::processUser);

            // Get next page
            pageable = slice.nextPageable();

        } while (slice.hasNext());
    }
}
```

### Avoid Count Queries

```java
public interface UserRepository extends CrudRepository<User, String> {

    // ❌ Returns Page - requires additional count query
    @EnableScan
    Page<User> findByStatus(String status, Pageable pageable);

    // ✅ Returns Slice - no count query
    @EnableScan
    @EnableScanCount(false)
    Slice<User> findByStatus(String status, Pageable pageable);
}
```

---

## Batch Operation Optimization

### Optimize Batch Size

```java
@Service
public class OptimizedBatchService {

    private static final int OPTIMAL_BATCH_SIZE = 25; // DynamoDB limit

    public void saveUsers(List<User> users, UserRepository repository) {
        // Split into optimal batch sizes
        Lists.partition(users, OPTIMAL_BATCH_SIZE).forEach(batch -> {
            try {
                repository.saveAll(batch);
            } catch (BatchWriteException e) {
                handleBatchFailure(e, batch);
            }
        });
    }
}
```

### Parallel Batch Processing

```java
@Service
public class ParallelBatchService {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public CompletableFuture<Void> saveUsersParallel(
            List<User> users,
            UserRepository repository) {

        List<List<User>> batches = Lists.partition(users, 25);

        List<CompletableFuture<Void>> futures = batches.stream()
            .map(batch -> CompletableFuture.runAsync(
                () -> repository.saveAll(batch),
                executor))
            .collect(Collectors.toList());

        return CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0]));
    }
}
```

---

## Caching Strategies

### Spring Cache Integration

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("users", "orders");
    }
}

@Service
public class CachedUserService {

    @Autowired
    private UserRepository repository;

    @Cacheable(value = "users", key = "#id")
    public Optional<User> findById(String id) {
        return repository.findById(id);
    }

    @CachePut(value = "users", key = "#user.id")
    public User save(User user) {
        return repository.save(user);
    }

    @CacheEvict(value = "users", key = "#id")
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
```

### Redis Caching

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```java
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()));
    }
}
```

### DAX (DynamoDB Accelerator)

```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>amazon-dax-client</artifactId>
    <version>2.0.4</version>
</dependency>
```

```java
@Configuration
public class DAXConfig {

    @Value("${aws.dax.endpoint}")
    private String daxEndpoint;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        // Use DAX cluster endpoint for caching
        return AmazonDaxClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .withEndpointConfiguration(daxEndpoint)
            .build();
    }
}
```

---

## Connection Pool Optimization

### Configure HTTP Client

```java
@Configuration
public class DynamoDBConfig {

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        ClientConfiguration clientConfig = new ClientConfiguration()
            .withMaxConnections(100)              // Increase connection pool
            .withConnectionTimeout(2000)          // 2 second connection timeout
            .withSocketTimeout(5000)              // 5 second socket timeout
            .withRequestTimeout(10000)            // 10 second request timeout
            .withClientExecutionTimeout(15000)    // 15 second total timeout
            .withMaxErrorRetry(5)                 // Retry failed requests
            .withThrottledRetries(true);          // Enable throttling backoff

        return AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .withClientConfiguration(clientConfig)
            .build();
    }
}
```

---

## Monitoring and Metrics

### Enable CloudWatch Metrics

```java
@Configuration
public class MetricsConfig {

    @Bean
    public AmazonCloudWatch cloudWatch() {
        return AmazonCloudWatchClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .build();
    }

    @Bean
    public RequestMetricCollector requestMetricCollector(AmazonCloudWatch cloudWatch) {
        return new CloudWatchRequestMetricCollector(cloudWatch);
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB(RequestMetricCollector collector) {
        return AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .withMetricsCollector(collector)
            .build();
    }
}
```

### Custom Metrics with Micrometer

```java
@Service
public class MetricsService {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private UserRepository repository;

    public User findUserWithMetrics(String id) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Optional<User> user = repository.findById(id);

            meterRegistry.counter("dynamodb.query.success",
                "operation", "findById")
                .increment();

            return user.orElse(null);

        } catch (Exception e) {
            meterRegistry.counter("dynamodb.query.failure",
                "operation", "findById",
                "error", e.getClass().getSimpleName())
                .increment();

            throw e;

        } finally {
            sample.stop(meterRegistry.timer("dynamodb.query.duration",
                "operation", "findById"));
        }
    }
}
```

---

## Best Practices Summary

### 1. Choose the Right Billing Mode

- **On-Demand**: Unpredictable traffic, development/testing
- **Provisioned**: Predictable traffic, cost optimization with auto-scaling

### 2. Design Tables for Access Patterns

```java
// ✅ Design table with access patterns in mind
@DynamoDBTable(tableName = "Order")
public class Order {
    @DynamoDBHashKey
    private String customerId;  // Primary access pattern

    @DynamoDBRangeKey
    private String orderDate;   // Sorting/filtering

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "status-index")
    private String status;      // Secondary access pattern
}
```

### 3. Use Batch Operations

```java
// ✅ Batch operations are more efficient
repository.saveAll(users);      // Batch write
repository.findAllById(ids);    // Batch get
repository.deleteAll(users);    // Batch delete

// ❌ Avoid individual operations in loops
users.forEach(user -> repository.save(user));
```

### 4. Implement Proper Error Handling

```java
// ✅ Handle throttling with retry
@Retryable(
    value = {ProvisionedThroughputExceededException.class},
    maxAttempts = 5,
    backoff = @Backoff(delay = 100, multiplier = 2, maxDelay = 5000)
)
public void saveUser(User user) {
    repository.save(user);
}
```

### 5. Monitor Performance

- Enable CloudWatch metrics
- Track consumed capacity
- Monitor throttling events
- Set up alerts for high latency

---

## Next Steps

- Review [Scan Exceptions](scan-exceptions) for scan optimization
- Check [Batch Operation Failures](batch-operation-failures) for handling failures
- See the [FAQ](faq) for additional questions
