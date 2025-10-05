# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Data DynamoDB is a Spring Data module that provides enhanced support for building data access layers on AWS
DynamoDB. It follows the Spring Data programming model, enabling developers to use familiar repository patterns with
DynamoDB.

## Build and Test Commands

### Building the Project

```bash
mvn clean install
```

### Running Tests

```bash
# Run all tests (unit + integration)
mvn test

# Run only unit tests
mvn surefire:test

# Run only integration tests
mvn failsafe:integration-test

# Run a specific test class
mvn test -Dtest=ClassName

# Run a specific test method
mvn test -Dtest=ClassName#methodName
```

### Code Quality and Validation

```bash
# Check code formatting (Google Java Style)
mvn formatter:validate

# Auto-format code
mvn formatter:format

# Run checkstyle validation
mvn checkstyle:check

# Run code coverage with JaCoCo
mvn jacoco:report

# Check license headers
mvn license:check

# Add missing license headers
mvn license:format
```

### Release Commands

```bash
# Prepare release
mvn release:prepare

# Perform release
mvn release:perform
```

## Architecture Overview

### Core Components

**DynamoDBTemplate** (`org.socialsignin.spring.data.dynamodb.core`): Central class for DynamoDB operations, similar to
JdbcTemplate. Provides low-level DynamoDB operations and event publishing for lifecycle events.

**DynamoDBOperations**: Interface defining the contract for DynamoDB data access operations including load, save, scan,
query, and batch operations.

**DynamoDBMapper Integration**: Wraps AWS SDK's DynamoDBMapper to provide Spring Data abstractions while maintaining AWS
SDK compatibility.

### Repository Layer

**SimpleDynamoDBCrudRepository**: Base implementation of CRUD repository operations. Handles:

- Hash key only entities via `load(hashKey)`
- Hash + Range key entities via `load(hashKey, rangeKey)`
- Batch operations with proper error handling
- Scan permissions validation via `@EnableScan` annotations

**DynamoDBRepositoryFactory**: Creates repository instances and applies custom implementations. Handles entity metadata
extraction for both hash-only and hash-range key entities.

**Query Execution Strategy** (`AbstractDynamoDBQuery`): Determines execution mode based on method signature:

- `CollectionExecution` - Returns List
- `PagedExecution` - Returns Page with total count
- `SlicedExecution` - Returns Slice (no count)
- `SingleEntityExecution` - Returns single entity or Optional
- `DeleteExecution` - Batch delete operations

### Query Creation

**PartTreeDynamoDBQuery**: Parses method names into DynamoDB queries. Creates either:

- **Query operations**: For hash key + optional range key conditions (efficient)
- **Scan operations**: For non-key attributes (requires `@EnableScan`)

**DynamoDBQueryCreator**: Translates Spring Data query parts into DynamoDB-specific query/scan expressions with proper
condition building.

### Entity Mapping

**DynamoDBPersistentEntity/Property**: Spring Data mapping metadata for DynamoDB entities. Handles:

- Hash key and range key identification
- Global/Local secondary index metadata
- Custom marshallers for date/time types
- Composite key support

**Key Extractors**: Separate strategies for extracting identifiers:

- `HashKeyExtractor` - For simple hash key entities
- `HashAndRangeKeyExtractor` - For composite key entities
- `CompositeIdHashAndRangeKeyExtractor` - For entities using `@DynamoDBHashAndRangeKey` annotation

### Configuration

**@EnableDynamoDBRepositories**: Activates repository infrastructure. Key attributes:

- `amazonDynamoDBRef` - Reference to AmazonDynamoDB client bean
- `dynamoDBMapperRef` - Reference to DynamoDBMapper bean
- `dynamoDBMapperConfigRef` - Reference to DynamoDBMapperConfig bean
- `dynamoDBOperationsRef` - Reference to DynamoDBOperations/Template bean

## Key Design Patterns

**Scan Protection**: Operations like `findAll()`, `count()`, and `deleteAll()` require explicit `@EnableScan` annotation
to prevent accidental expensive scans.

**Batch Operations**: Batch writes/deletes return `List<FailedBatch>` which are wrapped into typed exceptions (
`BatchWriteException`, `BatchDeleteException`) with detailed failure information.

**Event Publishing**: Lifecycle events (BeforeSave, AfterSave, AfterLoad, etc.) published through Spring's event
infrastructure for auditing and custom processing.

**Lazy Loading**: Query/Scan results return `PaginatedQueryList`/`PaginatedScanList` for memory-efficient iteration over
large result sets.

## Testing

**DynamoDB Local**: Integration tests use DynamoDB Local (in-memory) configured via SQLite4Java. The Maven build
automatically downloads platform-specific SQLite libraries to `target/lib`.

**Test Structure**:

- Unit tests: `src/test/java/**/*Test.java`
- Integration tests: `src/test/java/**/*IT.java`
- Test configuration: XML-based and Java-based configs in `src/test/resources`

## Important Implementation Notes

- All repository interfaces must extend `DynamoDBCrudRepository` or `DynamoDBPagingAndSortingRepository`
- Entity classes must be annotated with `@DynamoDBTable` and have hash key defined via `@DynamoDBHashKey`
- Range keys are optional and defined via `@DynamoDBRangeKey`
- Query methods are limited to hash key + range key conditions; filtering on other attributes requires scan operations
- Projection expressions can be used via the `@Projection` annotation for reducing data transfer
- The library supports both AWS SDK v1 (current) DynamoDB client
