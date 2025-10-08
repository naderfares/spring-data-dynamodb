---
layout: page
parent: Troubleshooting
grand_parent: Documentation
title: Batch Operation Failures
nav_order: 3
---

# Batch Operation Failures

This guide explains how to handle batch write and delete operation failures in Spring Data DynamoDB.

## Table of Contents

{: .no_toc .text-delta }

1. TOC
   {:toc}

---

## Understanding Batch Operations

DynamoDB's batch operations can process up to 25 items per request. However, **batch operations can partially fail**,
meaning some items succeed while others fail.

### Key Characteristics

- **Partial Failures**: Some items may succeed while others fail
- **No Transactions**: BatchWrite is not transactional
- **Automatic Retry**: Failed items are NOT automatically retried
- **Return Type**: Operations return `List<FailedBatch>` for failed items

---

## Batch Write Exceptions

### BatchWriteException

**Error:**

```
org.socialsignin.spring.data.dynamodb.exception.BatchWriteException:
Failed to write 3 items to DynamoDB
```

**Cause:** One or more items in a `saveAll()` operation failed to write.

**Common Reasons:**

- Provisioned throughput exceeded
- Item size exceeds 400KB
- Invalid attribute values
- Conditional check failures
- Internal server errors

### Handling BatchWriteException

```java
import org.socialsignin.spring.data.dynamodb.exception.BatchWriteException;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper.FailedBatch;

public class UserService {

    @Autowired
    private UserRepository repository;

    public void saveUsers(List<User> users) {
        try {
            repository.saveAll(users);
            log.info("Successfully saved {} users", users.size());

        } catch (BatchWriteException e) {
            List<FailedBatch> failedBatches = e.getFailedBatches();

            log.error("Failed to save {} batches", failedBatches.size());

            for (FailedBatch failedBatch : failedBatches) {
                // Get the exception for this batch
                Exception batchException = failedBatch.getException();

                // Get the unprocessed items
                Map<String, List<WriteRequest>> unprocessedItems =
                    failedBatch.getUnprocessedItems();

                log.error("Batch failed with {} unprocessed items: {}",
                    unprocessedItems.size(), batchException.getMessage());

                // Handle based on exception type
                handleFailedBatch(failedBatch);
            }
        }
    }

    private void handleFailedBatch(FailedBatch batch) {
        Exception ex = batch.getException();

        if (ex instanceof ProvisionedThroughputExceededException) {
            // Retry with exponential backoff
            retryWithBackoff(batch);

        } else if (ex instanceof AmazonServiceException) {
            AmazonServiceException ase = (AmazonServiceException) ex;

            if (ase.getErrorCode().equals("ValidationException")) {
                // Log invalid items for manual review
                logInvalidItems(batch);
            } else {
                // Retry other service exceptions
                retryWithBackoff(batch);
            }

        } else {
            // Unknown error - log for investigation
            log.error("Unknown batch failure", ex);
        }
    }
}
```

### Extracting Failed Items

```java
public List<User> extractFailedUsers(BatchWriteException e) {
    List<User> failedUsers = new ArrayList<>();

    for (FailedBatch batch : e.getFailedBatches()) {
        Map<String, List<WriteRequest>> unprocessedItems =
            batch.getUnprocessedItems();

        for (Map.Entry<String, List<WriteRequest>> entry : unprocessedItems.entrySet()) {
            String tableName = entry.getKey();
            List<WriteRequest> requests = entry.getValue();

            for (WriteRequest request : requests) {
                // Extract item attributes
                if (request.getPutRequest() != null) {
                    Map<String, AttributeValue> item =
                        request.getPutRequest().getItem();

                    // Convert back to User object
                    User user = convertToUser(item);
                    failedUsers.add(user);
                }
            }
        }
    }

    return failedUsers;
}

private User convertToUser(Map<String, AttributeValue> item) {
    User user = new User();
    user.setId(item.get("id").getS());
    user.setName(item.get("name").getS());
    // ... set other attributes
    return user;
}
```

---

## Batch Delete Exceptions

### BatchDeleteException

**Error:**

```
org.socialsignin.spring.data.dynamodb.exception.BatchDeleteException:
Failed to delete 5 items from DynamoDB
```

**Cause:** One or more items in a `deleteAll()` operation failed to delete.

### Handling BatchDeleteException

```java
import org.socialsignin.spring.data.dynamodb.exception.BatchDeleteException;

public class UserService {

    @Autowired
    private UserRepository repository;

    public void deleteUsers(List<User> users) {
        try {
            repository.deleteAll(users);
            log.info("Successfully deleted {} users", users.size());

        } catch (BatchDeleteException e) {
            List<FailedBatch> failedBatches = e.getFailedBatches();

            log.error("Failed to delete {} batches", failedBatches.size());

            // Retry failed deletions
            retryFailedDeletions(failedBatches);
        }
    }

    private void retryFailedDeletions(List<FailedBatch> failedBatches) {
        for (FailedBatch batch : failedBatches) {
            Map<String, List<WriteRequest>> unprocessedItems =
                batch.getUnprocessedItems();

            // Extract keys and retry individual deletes
            List<String> failedIds = extractDeleteKeys(unprocessedItems);

            for (String id : failedIds) {
                try {
                    repository.deleteById(id);
                } catch (Exception ex) {
                    log.error("Failed to delete user {}: {}", id, ex.getMessage());
                }
            }
        }
    }

    private List<String> extractDeleteKeys(
            Map<String, List<WriteRequest>> unprocessedItems) {

        List<String> ids = new ArrayList<>();

        for (List<WriteRequest> requests : unprocessedItems.values()) {
            for (WriteRequest request : requests) {
                if (request.getDeleteRequest() != null) {
                    Map<String, AttributeValue> key =
                        request.getDeleteRequest().getKey();
                    ids.add(key.get("id").getS());
                }
            }
        }

        return ids;
    }
}
```

---

## Retry Strategies

### Simple Retry with Exponential Backoff

```java
public class BatchRetryService {

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 100;

    public void saveWithRetry(List<User> users, UserRepository repository) {
        List<User> remainingUsers = new ArrayList<>(users);
        int retryCount = 0;

        while (!remainingUsers.isEmpty() && retryCount < MAX_RETRIES) {
            try {
                repository.saveAll(remainingUsers);
                remainingUsers.clear(); // Success - clear list

            } catch (BatchWriteException e) {
                retryCount++;

                // Extract failed items
                remainingUsers = extractFailedUsers(e);

                if (!remainingUsers.isEmpty()) {
                    long backoffMs = INITIAL_BACKOFF_MS * (long) Math.pow(2, retryCount);

                    log.warn("Batch write failed, retrying {} items after {}ms (attempt {}/{})",
                        remainingUsers.size(), backoffMs, retryCount, MAX_RETRIES);

                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }

        if (!remainingUsers.isEmpty()) {
            throw new RuntimeException(
                "Failed to save " + remainingUsers.size() +
                " items after " + MAX_RETRIES + " retries");
        }
    }
}
```

### Using Spring Retry

```xml
<!-- Add dependency -->
<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aspects</artifactId>
</dependency>
```

```java
@Configuration
@EnableRetry
public class RetryConfig {
    // Enable retry support
}

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Retryable(
        value = {BatchWriteException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void saveUsers(List<User> users) {
        repository.saveAll(users);
    }

    @Recover
    public void recoverFromBatchWrite(BatchWriteException e, List<User> users) {
        log.error("Failed to save users after retries: {}", e.getMessage());

        // Handle permanent failure
        List<User> failedUsers = extractFailedUsers(e);
        saveToDeadLetterQueue(failedUsers);
    }
}
```

---

## Batch Size Optimization

### Split Large Batches

```java
public class BatchService {

    private static final int BATCH_SIZE = 25; // DynamoDB limit

    public void saveInBatches(List<User> users, UserRepository repository) {
        // Split into batches of 25
        List<List<User>> batches = Lists.partition(users, BATCH_SIZE);

        List<User> allFailedUsers = new ArrayList<>();

        for (int i = 0; i < batches.size(); i++) {
            List<User> batch = batches.get(i);

            try {
                repository.saveAll(batch);
                log.info("Saved batch {}/{} ({} users)",
                    i + 1, batches.size(), batch.size());

            } catch (BatchWriteException e) {
                List<User> failedUsers = extractFailedUsers(e);
                allFailedUsers.addAll(failedUsers);

                log.warn("Batch {}/{} had {} failures",
                    i + 1, batches.size(), failedUsers.size());
            }

            // Small delay between batches to avoid throttling
            if (i < batches.size() - 1) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Retry failed items
        if (!allFailedUsers.isEmpty()) {
            log.warn("Retrying {} failed users", allFailedUsers.size());
            retryFailedUsers(allFailedUsers, repository);
        }
    }
}
```

### Parallel Batch Processing

```java
@Service
public class ParallelBatchService {

    @Autowired
    private UserRepository repository;

    private final ExecutorService executor =
        Executors.newFixedThreadPool(10);

    public CompletableFuture<Void> saveInParallel(List<User> users) {
        List<List<User>> batches = Lists.partition(users, 25);

        List<CompletableFuture<Void>> futures = batches.stream()
            .map(batch -> CompletableFuture.runAsync(
                () -> saveBatchWithRetry(batch),
                executor))
            .collect(Collectors.toList());

        return CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0]));
    }

    private void saveBatchWithRetry(List<User> batch) {
        int retries = 0;
        List<User> remaining = new ArrayList<>(batch);

        while (!remaining.isEmpty() && retries < 3) {
            try {
                repository.saveAll(remaining);
                remaining.clear();
            } catch (BatchWriteException e) {
                remaining = extractFailedUsers(e);
                retries++;

                if (!remaining.isEmpty()) {
                    try {
                        Thread.sleep(100 * (long) Math.pow(2, retries));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
```

---

## Monitoring and Alerting

### Metrics Collection

```java
@Service
public class MetricsAwareBatchService {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private UserRepository repository;

    public void saveUsersWithMetrics(List<User> users) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            repository.saveAll(users);

            meterRegistry.counter("dynamodb.batch.write.success",
                "size", String.valueOf(users.size()))
                .increment();

        } catch (BatchWriteException e) {
            List<FailedBatch> failed = e.getFailedBatches();

            meterRegistry.counter("dynamodb.batch.write.failure",
                "batches", String.valueOf(failed.size()))
                .increment();

            // Track failure reasons
            for (FailedBatch batch : failed) {
                String errorType = batch.getException().getClass().getSimpleName();
                meterRegistry.counter("dynamodb.batch.write.error",
                    "type", errorType)
                    .increment();
            }

            throw e;

        } finally {
            sample.stop(meterRegistry.timer("dynamodb.batch.write.duration"));
        }
    }
}
```

### Logging Failed Items

```java
@Slf4j
public class BatchAuditService {

    public void logBatchFailures(BatchWriteException e, List<User> originalUsers) {
        List<User> failedUsers = extractFailedUsers(e);

        // Log to structured logging system
        failedUsers.forEach(user ->
            log.error("Failed to save user: id={}, name={}, reason={}",
                user.getId(),
                user.getName(),
                getFailureReason(user, e)));

        // Save to dead letter queue
        saveToDeadLetterQueue(failedUsers);
    }

    private String getFailureReason(User user, BatchWriteException e) {
        // Extract specific failure reason for this user
        for (FailedBatch batch : e.getFailedBatches()) {
            // Check if this batch contains this user
            // Return exception message
        }
        return "Unknown";
    }

    private void saveToDeadLetterQueue(List<User> users) {
        // Persist failed items for manual review
        // Could use: S3, SQS, separate DynamoDB table, etc.
    }
}
```

---

## Best Practices

### 1. Handle Partial Failures

```java
// ✅ Always catch and handle batch exceptions
try {
    repository.saveAll(users);
} catch (BatchWriteException e) {
    // Extract and retry failed items
    List<User> failed = extractFailedUsers(e);
    retryFailedUsers(failed);
}

// ❌ Don't ignore batch exceptions
repository.saveAll(users); // Some items might have failed silently
```

### 2. Implement Retry Logic

```java
// ✅ Retry with exponential backoff
@Retryable(
    value = {BatchWriteException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 100, multiplier = 2)
)
public void saveUsers(List<User> users) {
    repository.saveAll(users);
}

// ❌ Don't retry immediately without backoff
for (int i = 0; i < 3; i++) {
    try {
        repository.saveAll(users);
        break;
    } catch (BatchWriteException e) {
        // No backoff = likely to fail again
    }
}
```

### 3. Monitor Failures

```java
// ✅ Track metrics and alert on high failure rates
meterRegistry.counter("batch.failures").increment();
if (failureRate > threshold) {
    alerting.sendAlert("High batch failure rate detected");
}
```

### 4. Use Appropriate Batch Sizes

```java
// ✅ Respect DynamoDB limits
List<List<User>> batches = Lists.partition(users, 25);

// ❌ Don't send oversized batches
repository.saveAll(users); // If > 25 items, will fail
```

---

## Next Steps

- Review [Performance Tuning](performance-tuning) for throughput optimization
- Check [Common Errors](common-errors) for configuration issues
- See the [FAQ](faq) for additional questions
