---
layout: page
parent: Documentation
title: Troubleshooting
nav_order: 6
has_children: true
---

# Troubleshooting Guide

This guide helps you diagnose and resolve common issues when working with Spring Data DynamoDB.

## Quick Navigation

### Common Issues

- **[Common Errors](common-errors)** - Frequently encountered errors and their solutions
- **[Scan Exceptions](scan-exceptions)** - Understanding and resolving scan-related errors
- **[Batch Operation Failures](batch-operation-failures)** - Handling batch write and delete failures
- **[Performance Tuning](performance-tuning)** - Optimizing DynamoDB query and scan performance
- **[FAQ](faq)** - Frequently asked questions

## Getting Help

Before diving into troubleshooting:

1. **Check the error message** - DynamoDB exceptions usually contain helpful details
2. **Enable debug logging** - Set `logging.level.org.socialsignin.spring.data.dynamodb=DEBUG`
3. **Review your entity mappings** - Many issues stem from incorrect annotations
4. **Verify AWS credentials** - Ensure your AWS configuration is correct
5. **Check table schema** - Confirm your DynamoDB table matches your entity definitions

## Debug Logging

Enable detailed logging in your `application.properties`:

```properties
# Spring Data DynamoDB debug logging
logging.level.org.socialsignin.spring.data.dynamodb=DEBUG

# AWS SDK logging
logging.level.com.amazonaws.request=DEBUG
```

Or in `logback.xml`:

```xml
<configuration>
    <logger name="org.socialsignin.spring.data.dynamodb" level="DEBUG"/>
    <logger name="com.amazonaws.request" level="DEBUG"/>
</configuration>
```

## Common Error Categories

| Category             | Description                                       | Guide                                                |
|----------------------|---------------------------------------------------|------------------------------------------------------|
| **Scan Operations**  | "Scanning for unpaginated queries is not enabled" | [Scan Exceptions](scan-exceptions)                   |
| **Batch Operations** | BatchWriteException, BatchDeleteException         | [Batch Operation Failures](batch-operation-failures) |
| **Throughput**       | ProvisionedThroughputExceededException            | [Performance Tuning](performance-tuning)             |
| **Configuration**    | AWS credentials, DynamoDB Local setup             | [Common Errors](common-errors)                       |
| **Mapping**          | Entity annotation issues, key extraction          | [Common Errors](common-errors)                       |
| **Performance**      | Slow queries, large result sets                   | [Performance Tuning](performance-tuning)             |

## Quick Fixes

### Enable Scan Operations

```java
@EnableScan
public interface UserRepository extends CrudRepository<User, String> {
    List<User> findAll();
}
```

### Handle Batch Failures

```java
try {
    repository.saveAll(users);
} catch (BatchWriteException e) {
    List<DynamoDBMapper.FailedBatch> failures = e.getFailedBatches();
    // Handle failed writes
}
```

### Configure AWS Credentials

```java
@Bean
public AmazonDynamoDB amazonDynamoDB() {
    return AmazonDynamoDBClientBuilder.standard()
        .withCredentials(new DefaultAWSCredentialsProviderChain())
        .withRegion(Regions.US_EAST_1)
        .build();
}
```

## Still Having Issues?

If you can't find a solution in this guide:

1. Check the [GitHub Issues](https://github.com/derjust/spring-data-dynamodb/issues)
2. Review the [AWS DynamoDB Documentation](https://docs.aws.amazon.com/dynamodb/)
3. Enable debug logging and examine the stack trace
4. Create a minimal reproducible example to isolate the issue

## Next Steps

Start with [Common Errors](common-errors) to see if your issue is listed, or browse the specific troubleshooting guides
for your error category.
