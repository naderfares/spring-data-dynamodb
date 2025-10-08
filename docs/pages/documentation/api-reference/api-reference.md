---
layout: page
parent: Documentation
title: API Reference
nav_order: 5
has_children: true
---

# API Reference

Complete reference documentation for Spring Data DynamoDB APIs, annotations, and configuration options.

## Overview

Spring Data DynamoDB provides a comprehensive API for building data access layers on AWS DynamoDB. The API follows
Spring Data conventions while exposing DynamoDB-specific features.

## API Components

### Core APIs

- **[DynamoDBTemplate](dynamodb-template.html)** - Central class for DynamoDB operations, providing low-level access and
  event publishing
- **[Repository Interfaces](repository-interfaces.html)** - DynamoDBCrudRepository and
  DynamoDBPagingAndSortingRepository for CRUD and paging operations

### Query APIs

- **[Query Methods](query-methods.html)** - Supported keywords and patterns for derived query methods
- **Query Expressions** - DynamoDBQueryExpression and DynamoDBScanExpression for advanced queries

### Annotation APIs

- **[Annotations](annotations.html)** - All Spring Data DynamoDB annotations including:
    - Configuration annotations (@EnableDynamoDBRepositories)
    - Entity annotations (@DynamoDBTable, @DynamoDBHashKey, @DynamoDBRangeKey)
    - Repository annotations (@EnableScan, @Projection)

### Configuration APIs

- **[Configuration](configuration.html)** - DynamoDBMapperConfig and repository configuration options

## Key Interfaces and Classes

### Core Operations

| Interface/Class    | Purpose                                                    |
|--------------------|------------------------------------------------------------|
| DynamoDBOperations | Contract for DynamoDB data access operations               |
| DynamoDBTemplate   | Implementation of DynamoDBOperations with event publishing |
| DynamoDBMapper     | AWS SDK mapper wrapper for entity-table mapping            |

### Repository Layer

| Interface                          | Purpose                                              |
|------------------------------------|------------------------------------------------------|
| DynamoDBCrudRepository             | Basic CRUD operations (save, findById, delete, etc.) |
| DynamoDBPagingAndSortingRepository | CRUD + pagination and sorting support                |
| SimpleDynamoDBCrudRepository       | Default repository implementation                    |

### Entity Metadata

| Interface/Class            | Purpose                                   |
|----------------------------|-------------------------------------------|
| DynamoDBEntityInformation  | Metadata about DynamoDB entities          |
| DynamoDBPersistentEntity   | Spring Data mapping metadata for entities |
| DynamoDBPersistentProperty | Property-level mapping metadata           |

### Query Execution

| Class                 | Purpose                                                    |
|-----------------------|------------------------------------------------------------|
| PartTreeDynamoDBQuery | Parses method names into DynamoDB queries                  |
| DynamoDBQueryCreator  | Translates Spring Data query parts to DynamoDB expressions |
| AbstractDynamoDBQuery | Base query execution with strategy pattern                 |

## Exception Hierarchy

Spring Data DynamoDB uses Spring's DataAccessException hierarchy:

```
DataAccessException
├── BatchWriteException - Failed batch write operations
├── BatchDeleteException - Failed batch delete operations
└── EmptyResultDataAccessException - Expected entity not found
```

## Return Types

### Query Results

- **PaginatedQueryList&lt;T&gt;** - Lazy-loaded query results with pagination
- **PaginatedScanList&lt;T&gt;** - Lazy-loaded scan results with pagination
- **Page&lt;T&gt;** - Spring Data Page with total count
- **Slice&lt;T&gt;** - Spring Data Slice without total count

### Batch Operations

- **List&lt;FailedBatch&gt;** - Detailed failure information for batch operations
- Wrapped into BatchWriteException or BatchDeleteException by repository layer

## Package Structure

```
org.socialsignin.spring.data.dynamodb
├── core                    - DynamoDBTemplate and operations
├── repository              - Repository interfaces and annotations
│   ├── config             - @EnableDynamoDBRepositories
│   ├── query              - Query creation and execution
│   └── support            - Repository implementations
├── mapping                 - Entity mapping and metadata
│   └── event              - Lifecycle events (BeforeSave, AfterLoad, etc.)
└── query                   - Query expression builders
```

## AWS SDK Integration

Spring Data DynamoDB integrates with AWS SDK for Java v1:

- **AmazonDynamoDB** - AWS DynamoDB client
- **DynamoDBMapper** - AWS entity-table mapper
- **DynamoDBMapperConfig** - AWS mapper configuration
- **DynamoDB Annotations** - AWS entity annotations (@DynamoDBTable, @DynamoDBHashKey, etc.)

## Thread Safety

- **DynamoDBTemplate** - Thread-safe when properly configured
- **Repository Instances** - Thread-safe, can be shared across threads
- **DynamoDBMapper** - Thread-safe per AWS SDK documentation
- **Query Results** - PaginatedQueryList/ScanList are NOT thread-safe

## Next Steps

- Review [DynamoDBTemplate API](dynamodb-template.html) for low-level operations
- Explore [Repository Interfaces](repository-interfaces.html) for CRUD operations
- Learn about [Query Methods](query-methods.html) for derived queries
- Configure repositories using [@EnableDynamoDBRepositories](annotations.html#enabledynamodbrepositories)
