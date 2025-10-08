---
layout: page
parent: API Reference
grand_parent: Documentation
title: Configuration
nav_order: 5
---

# Configuration API Reference

Configuration options for Spring Data DynamoDB using DynamoDBMapperConfig and related classes.

## DynamoDBMapperConfig

AWS SDK configuration class for customizing DynamoDB mapper behavior.

**Package:** `com.amazonaws.services.dynamodbv2.datamodeling`

### Builder Pattern

```java
DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withSaveBehavior(SaveBehavior.UPDATE)
    .withConsistentReads(ConsistentReads.CONSISTENT)
    .withTableNameOverride(TableNameOverride.withTableNamePrefix("dev_"))
    .withPaginationLoadingStrategy(PaginationLoadingStrategy.LAZY_LOADING)
    .build();
```

## Configuration Options

### SaveBehavior

Controls how save operations are performed.

**Enum Values:**

#### UPDATE (Default)

Updates only the attributes provided, leaving others unchanged.

```java
DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withSaveBehavior(SaveBehavior.UPDATE)
    .build();

// Example
User user = new User();
user.setId("user123");
user.setName("John Doe");  // Only name is updated, other attributes unchanged
template.save(user);
```

#### UPDATE_SKIP_NULL_ATTRIBUTES

Updates provided attributes but skips null values.

```java
DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withSaveBehavior(SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
    .build();

// Example
User user = new User();
user.setId("user123");
user.setName("John Doe");
user.setEmail(null);  // Email attribute is not updated (remains unchanged)
template.save(user);
```

#### CLOBBER

Replaces the entire item, removing attributes not provided.

```java
DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withSaveBehavior(SaveBehavior.CLOBBER)
    .build();

// Example - WARNING: Other attributes will be removed!
User user = new User();
user.setId("user123");
user.setName("John Doe");
// All other attributes (email, phone, etc.) will be removed from DynamoDB
template.save(user);
```

#### APPEND_SET

For set attributes, appends new values instead of replacing.

```java
DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withSaveBehavior(SaveBehavior.APPEND_SET)
    .build();

// Example
User user = new User();
user.setId("user123");
user.setTags(new HashSet<>(Arrays.asList("new-tag")));
// Appends "new-tag" to existing tags instead of replacing
template.save(user);
```

### ConsistentReads

Controls read consistency for query and scan operations.

**Enum Values:**

#### EVENTUAL (Default)

Uses eventually consistent reads (lower cost, higher throughput).

```java
DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withConsistentReads(ConsistentReads.EVENTUAL)
    .build();
```

#### CONSISTENT

Uses strongly consistent reads (higher cost, guaranteed latest data).

```java
DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withConsistentReads(ConsistentReads.CONSISTENT)
    .build();

// Reads will always return the latest data
User user = template.load(User.class, "user123");
```

### TableNameOverride

Overrides table names from @DynamoDBTable annotations.

**Options:**

#### Table Name Prefix

Adds a prefix to all table names.

```java
TableNameOverride override = TableNameOverride.withTableNamePrefix("dev_");

DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withTableNameOverride(override)
    .build();

// @DynamoDBTable(tableName = "users") becomes "dev_users"
```

**Use Cases:**

- Multi-environment deployments (dev, staging, prod)
- Multi-tenancy
- Testing

**Example Configuration:**

```java
@Configuration
public class DynamoDBConfig {

    @Value("${dynamodb.table.prefix:}")
    private String tablePrefix;

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        TableNameOverride override = null;

        if (!tablePrefix.isEmpty()) {
            override = TableNameOverride.withTableNamePrefix(tablePrefix);
        }

        return DynamoDBMapperConfig.builder()
            .withTableNameOverride(override)
            .build();
    }
}
```

#### Complete Table Name Override

Overrides all table names to a single table name.

```java
TableNameOverride override = TableNameOverride.withTableNameReplacement("single_table");

DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withTableNameOverride(override)
    .build();

// All entities use "single_table" regardless of @DynamoDBTable annotation
```

### TableNameResolver

Custom logic for resolving table names.

**Interface:**

```java
public interface TableNameResolver {
    String getTableName(Class<?> clazz, DynamoDBMapperConfig config);
}
```

**Example - Environment-based resolver:**

```java
public class EnvironmentTableNameResolver implements TableNameResolver {

    private final String environment;

    public EnvironmentTableNameResolver(String environment) {
        this.environment = environment;
    }

    @Override
    public String getTableName(Class<?> clazz, DynamoDBMapperConfig config) {
        DynamoDBTable annotation = clazz.getAnnotation(DynamoDBTable.class);
        String tableName = annotation.tableName();

        // Add environment prefix for non-production
        if (!"prod".equals(environment)) {
            return environment + "_" + tableName;
        }

        return tableName;
    }
}

@Configuration
public class DynamoDBConfig {

    @Value("${app.environment}")
    private String environment;

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        return DynamoDBMapperConfig.builder()
            .withTableNameResolver(new EnvironmentTableNameResolver(environment))
            .build();
    }
}
```

### PaginationLoadingStrategy

Controls how paginated results are loaded.

**Enum Values:**

#### LAZY_LOADING (Default)

Loads pages on demand as you iterate through results.

```java
DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withPaginationLoadingStrategy(PaginationLoadingStrategy.LAZY_LOADING)
    .build();

// Results are loaded page by page as you iterate
PaginatedQueryList<User> results = template.query(User.class, expression);
for (User user : results) {
    // Next page loaded automatically when needed
}
```

#### EAGER_LOADING

Loads all results immediately.

```java
DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withPaginationLoadingStrategy(PaginationLoadingStrategy.EAGER_LOADING)
    .build();

// All pages loaded immediately
PaginatedQueryList<User> results = template.query(User.class, expression);
System.out.println("Total loaded: " + results.size());
```

#### ITERATION_ONLY

Supports only forward iteration (most memory efficient).

```java
DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withPaginationLoadingStrategy(PaginationLoadingStrategy.ITERATION_ONLY)
    .build();

PaginatedQueryList<User> results = template.query(User.class, expression);
// Can only iterate once, cannot call size() or random access
for (User user : results) {
    processUser(user);
}
```

### RequestMetricCollector

Collects metrics for AWS SDK requests.

**Example:**

```java
RequestMetricCollector metricCollector = new RequestMetricCollector() {
    @Override
    public void collectMetrics(Request<?> request, Response<?> response) {
        // Collect and log metrics
        System.out.println("Request: " + request.getServiceName());
        System.out.println("Time: " + response.getAwsRequestMetrics()
            .getTimingInfo().getEndTimeNano());
    }
};

DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withRequestMetricCollector(metricCollector)
    .build();
```

### TypeConverterFactory

Custom type converters for attributes.

**Example:**

```java
public class CustomConverterFactory implements DynamoDBTypeConverterFactory {

    @Override
    public <T> DynamoDBTypeConverter<?, T> getConverter(Class<T> targetType) {
        if (targetType == LocalDate.class) {
            return (DynamoDBTypeConverter<?, T>) new LocalDateConverter();
        }
        return null;
    }
}

public class LocalDateConverter implements DynamoDBTypeConverter<String, LocalDate> {

    @Override
    public String convert(LocalDate date) {
        return date.toString();
    }

    @Override
    public LocalDate unconvert(String s) {
        return LocalDate.parse(s);
    }
}

DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withTypeConverterFactory(new CustomConverterFactory())
    .build();
```

### ConversionSchema

Defines how objects are converted to/from DynamoDB format.

**Standard Schemas:**

```java
// V2 schema (default, recommended)
DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withConversionSchema(ConversionSchemas.V2)
    .build();

// V1 schema (legacy)
DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withConversionSchema(ConversionSchemas.V1)
    .build();

// V2 with custom marshallers
ConversionSchema schema = ConversionSchemas.V2.with(customMarshallers);
DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
    .withConversionSchema(schema)
    .build();
```

## Complete Configuration Example

### Application Properties

```properties
# application.properties
amazon.dynamodb.endpoint=http://localhost:8000
amazon.aws.region=us-east-1
amazon.dynamodb.table.prefix=dev_

# Connection pool
amazon.dynamodb.max.connections=50
amazon.dynamodb.connection.timeout=10000
amazon.dynamodb.socket.timeout=10000
```

### Configuration Class

```java
@Configuration
@EnableDynamoDBRepositories(
    basePackages = "com.example.repositories",
    dynamoDBMapperConfigRef = "dynamoDBMapperConfig"
)
public class DynamoDBConfig {

    @Value("${amazon.dynamodb.endpoint}")
    private String endpoint;

    @Value("${amazon.aws.region}")
    private String region;

    @Value("${amazon.dynamodb.table.prefix:}")
    private String tablePrefix;

    @Value("${amazon.dynamodb.max.connections:50}")
    private int maxConnections;

    @Value("${amazon.dynamodb.connection.timeout:10000}")
    private int connectionTimeout;

    @Value("${amazon.dynamodb.socket.timeout:10000}")
    private int socketTimeout;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setMaxConnections(maxConnections);
        clientConfig.setConnectionTimeout(connectionTimeout);
        clientConfig.setSocketTimeout(socketTimeout);

        return AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(endpoint, region)
            )
            .withClientConfiguration(clientConfig)
            .build();
    }

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        DynamoDBMapperConfig.Builder builder = DynamoDBMapperConfig.builder()
            .withSaveBehavior(SaveBehavior.UPDATE)
            .withConsistentReads(ConsistentReads.EVENTUAL)
            .withPaginationLoadingStrategy(PaginationLoadingStrategy.LAZY_LOADING)
            .withTypeConverterFactory(customTypeConverterFactory());

        // Add table name prefix if configured
        if (!tablePrefix.isEmpty()) {
            builder.withTableNameOverride(
                TableNameOverride.withTableNamePrefix(tablePrefix)
            );
        }

        return builder.build();
    }

    @Bean
    public DynamoDBMapper dynamoDBMapper(
            AmazonDynamoDB amazonDynamoDB,
            DynamoDBMapperConfig config) {
        return new DynamoDBMapper(amazonDynamoDB, config);
    }

    @Bean
    public DynamoDBOperations dynamoDBTemplate(
            AmazonDynamoDB amazonDynamoDB,
            DynamoDBMapper mapper,
            DynamoDBMapperConfig config) {
        return new DynamoDBTemplate(amazonDynamoDB, mapper, config);
    }

    private DynamoDBTypeConverterFactory customTypeConverterFactory() {
        return new DynamoDBTypeConverterFactory() {
            @Override
            public <T> DynamoDBTypeConverter<?, T> getConverter(Class<T> targetType) {
                // Custom converters for specific types
                if (targetType == LocalDate.class) {
                    return (DynamoDBTypeConverter<?, T>) new LocalDateConverter();
                }
                if (targetType == LocalDateTime.class) {
                    return (DynamoDBTypeConverter<?, T>) new LocalDateTimeConverter();
                }
                return null;
            }
        };
    }
}
```

## Environment-Specific Configuration

### Development Configuration

```java
@Configuration
@Profile("dev")
public class DynamoDBDevConfig {

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        // Use DynamoDB Local
        return AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    "http://localhost:8000",
                    "us-east-1"
                )
            )
            .withCredentials(new AWSStaticCredentialsProvider(
                new BasicAWSCredentials("dummy", "dummy")
            ))
            .build();
    }

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        return DynamoDBMapperConfig.builder()
            .withSaveBehavior(SaveBehavior.UPDATE)
            .withConsistentReads(ConsistentReads.CONSISTENT)  // Strong consistency for dev
            .withTableNameOverride(
                TableNameOverride.withTableNamePrefix("dev_")
            )
            .build();
    }
}
```

### Production Configuration

```java
@Configuration
@Profile("prod")
public class DynamoDBProdConfig {

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        // Use real DynamoDB with auto-scaling
        ClientConfiguration clientConfig = new ClientConfiguration()
            .withMaxConnections(100)
            .withConnectionTimeout(10000)
            .withSocketTimeout(10000)
            .withRetryPolicy(PredefinedRetryPolicies.getDynamoDBDefaultRetryPolicy());

        return AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .withClientConfiguration(clientConfig)
            .build();
    }

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        return DynamoDBMapperConfig.builder()
            .withSaveBehavior(SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .withConsistentReads(ConsistentReads.EVENTUAL)  // Eventually consistent for better performance
            .withPaginationLoadingStrategy(PaginationLoadingStrategy.LAZY_LOADING)
            .build();
    }
}
```

## Testing Configuration

### Test Configuration

```java
@TestConfiguration
public class DynamoDBTestConfig {

    private static DynamoDBProxyServer server;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() throws Exception {
        // Start DynamoDB Local for tests
        String port = "8000";
        String[] localArgs = {"-inMemory", "-port", port};

        System.setProperty("sqlite4java.library.path", "target/lib");

        server = ServerRunner.createServerFromCommandLineArgs(localArgs);
        server.start();

        return AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    "http://localhost:" + port,
                    "us-east-1"
                )
            )
            .withCredentials(new AWSStaticCredentialsProvider(
                new BasicAWSCredentials("test", "test")
            ))
            .build();
    }

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        return DynamoDBMapperConfig.builder()
            .withSaveBehavior(SaveBehavior.CLOBBER)  // Clean saves for tests
            .withConsistentReads(ConsistentReads.CONSISTENT)
            .build();
    }

    @PreDestroy
    public void stopDynamoDBLocal() throws Exception {
        if (server != null) {
            server.stop();
        }
    }
}
```

## Best Practices

### 1. Use Environment-Specific Prefixes

```java
@Bean
public DynamoDBMapperConfig dynamoDBMapperConfig(
        @Value("${spring.profiles.active}") String profile) {

    DynamoDBMapperConfig.Builder builder = DynamoDBMapperConfig.builder();

    if (!"prod".equals(profile)) {
        builder.withTableNameOverride(
            TableNameOverride.withTableNamePrefix(profile + "_")
        );
    }

    return builder.build();
}
```

### 2. Configure Read Consistency Based on Use Case

```java
// For critical data requiring latest values
@Bean
@Qualifier("consistentConfig")
public DynamoDBMapperConfig consistentConfig() {
    return DynamoDBMapperConfig.builder()
        .withConsistentReads(ConsistentReads.CONSISTENT)
        .build();
}

// For read-heavy workloads where eventual consistency is acceptable
@Bean
@Qualifier("eventualConfig")
public DynamoDBMapperConfig eventualConfig() {
    return DynamoDBMapperConfig.builder()
        .withConsistentReads(ConsistentReads.EVENTUAL)
        .build();
}
```

### 3. Use UPDATE_SKIP_NULL_ATTRIBUTES for Partial Updates

```java
@Bean
public DynamoDBMapperConfig dynamoDBMapperConfig() {
    return DynamoDBMapperConfig.builder()
        .withSaveBehavior(SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
        .build();
}

// Allows partial updates without affecting null fields
User user = new User();
user.setId("user123");
user.setName("New Name");
user.setEmail(null);  // Email will not be updated
repository.save(user);
```

### 4. Configure Connection Pooling

```java
@Bean
public AmazonDynamoDB amazonDynamoDB() {
    ClientConfiguration clientConfig = new ClientConfiguration()
        .withMaxConnections(50)  // Adjust based on expected load
        .withConnectionTimeout(10000)
        .withSocketTimeout(10000)
        .withMaxErrorRetry(3)
        .withRetryPolicy(PredefinedRetryPolicies.getDynamoDBDefaultRetryPolicy());

    return AmazonDynamoDBClientBuilder.standard()
        .withClientConfiguration(clientConfig)
        .build();
}
```

### 5. Use Lazy Loading for Large Result Sets

```java
@Bean
public DynamoDBMapperConfig dynamoDBMapperConfig() {
    return DynamoDBMapperConfig.builder()
        .withPaginationLoadingStrategy(PaginationLoadingStrategy.LAZY_LOADING)
        .build();
}
```

## See Also

- [@EnableDynamoDBRepositories](annotations.html#enabledynamodbrepositories)
- [DynamoDBTemplate](dynamodb-template.html)
- [Repository Interfaces](repository-interfaces.html)
- [AWS DynamoDB Documentation](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/)
