---
layout: page
title: Documentation
permalink: /documentation/
---

# Spring Data DynamoDB Documentation

Comprehensive documentation for building data access layers with AWS DynamoDB using Spring Data.

## Overview

Spring Data DynamoDB brings the familiar Spring Data programming model to AWS DynamoDB, enabling developers to build
data access layers using repository patterns, query methods, and Spring conventions while maintaining DynamoDB-specific
optimizations.

This documentation covers everything from quick start guides to advanced implementation patterns, operational best
practices, and complete API reference.

## What's New in 6.0.x

### Version 6.0.11 (Current)

- **Spring Boot 3.5.6+ Support** - Full compatibility with latest Spring Boot 3.x releases
- **Spring Framework 6.2.11+** - Updated for Spring Framework 6.2.11 and above
- **Spring Data 2025.0.4** - Support for latest Spring Data release train
- **Enhanced Repository Configuration** - Improved configuration options and flexibility
- **Performance Improvements** - Optimized query execution and batch operations
- **Bug Fixes** - Various stability and reliability improvements

### Version 6.0.x Series

- **Spring Boot 3.x Support** - Migration to Spring Boot 3 and Jakarta EE
- **Java 17+ Requirement** - Aligned with Spring Boot 3 requirements
- **Improved Type Safety** - Enhanced compile-time checks and type inference
- **Modernized APIs** - Updated to follow latest Spring Data patterns
- **Better Error Messages** - More descriptive exceptions and validation messages

## Documentation Sections

### Getting Started

**[Implementation Guide](/documentation/implementation/)** - Step-by-step guides for implementing DynamoDB solutions

Essential topics to get you started quickly:

- [Quick Start - XML Configuration](/documentation/implementation/Quick-Start---XML-based-configuration.html)
- [DynamoDB Hash Key Example](/documentation/implementation/DynamoDB-Hash-Key-Example.html)
- [Global Secondary Index Example](/documentation/implementation/DynamoDB-GSI-Example.html)
- [Composite Primary Keys (Kotlin)](/documentation/implementation/Composite-Primary-Keys-Kotlin-Example.html)
- [Access to Releases](/documentation/implementation/Access-to-releases.html)

### API Reference

**[API Reference](/documentation/api-reference/)** - Complete reference for all APIs, interfaces, and annotations

Core API documentation:

- [DynamoDBTemplate](/documentation/api-reference/dynamodb-template.html) - Central class for DynamoDB operations
- [Repository Interfaces](/documentation/api-reference/repository-interfaces.html) - CRUD and paging repository
  contracts
- [Query Methods](/documentation/api-reference/query-methods.html) - Supported keywords and query patterns
- [Annotations](/documentation/api-reference/annotations.html) - All configuration and entity annotations
- [Configuration](/documentation/api-reference/configuration.html) - Repository and mapper configuration options

### Implementation Details

**[Implementation](/documentation/implementation/)** - Practical examples and implementation patterns

Advanced implementation topics:

- [Custom Repository Implementations](/documentation/implementation/Custom-repository-implementations.html)
- [Projections](/documentation/implementation/Projections.html) - Fetch only required attributes
- [Query Size Limits and Pageable](/documentation/implementation/Query-Size-Limits-and-Pageable.html)
- [Auto-create Tables](/documentation/implementation/Autocreate-Tables.html)

### Operational Guide

**[Operational](/documentation/operational/)** - DevOps, deployment, and operational topics

Production deployment and operations:

- [Multi-Repository Configuration](/documentation/operational/Multi-Repository-configuration.html)
- [Alter Table Name at Runtime](/documentation/operational/Alter-table-name-during-runtime.html)
- [Spring Data REST Integration](/documentation/operational/Spring-Data-REST.html)
- [Amazon DynamoDB Accelerator (DAX)](/documentation/operational/Amazon-DynamoDB-Accelerator-(DAX).html)

### Conceptual Guides

**[Conceptual](/documentation/conceptual/)** - Understand core concepts and design patterns

Understand DynamoDB concepts in Spring Data context:

- [Using Hash & Range Keys](/documentation/conceptual/Use-Hash-Range-keys.html)
- [Supported Spring Data Comparison Operators](/documentation/conceptual/Supported-Spring-Data-Comparison-Operators.html)

### Troubleshooting

**[Troubleshooting](/documentation/troubleshooting/)** - Common issues and solutions

Tips for diagnosing and resolving issues (coming soon).

## Quick Links

### Common Tasks

| Task                      | Documentation                                                                                  |
|---------------------------|------------------------------------------------------------------------------------------------|
| Add dependency to project | [Access to Releases](/documentation/implementation/Access-to-releases.html)                    |
| Configure repositories    | [Configuration](/documentation/api-reference/configuration.html)                               |
| Create first entity       | [DynamoDB Hash Key Example](/documentation/implementation/DynamoDB-Hash-Key-Example.html)      |
| Query with method names   | [Query Methods](/documentation/api-reference/query-methods.html)                               |
| Use composite keys        | [Hash & Range Keys](/documentation/conceptual/Use-Hash-Range-keys.html)                        |
| Implement custom logic    | [Custom Implementations](/documentation/implementation/Custom-repository-implementations.html) |
| Setup pagination          | [Query Size Limits](/documentation/implementation/Query-Size-Limits-and-Pageable.html)         |
| Use secondary indexes     | [GSI Example](/documentation/implementation/DynamoDB-GSI-Example.html)                         |
| Reduce data transfer      | [Projections](/documentation/implementation/Projections.html)                                  |
| Deploy to production      | [Operational Guide](/documentation/operational/)                                               |

### By Role

**Developer** - Start
with [Implementation Guide](/documentation/implementation/) → [API Reference](/documentation/api-reference/)

**DevOps Engineer** -
Check [Operational Guide](/documentation/operational/) → [Configuration](/documentation/api-reference/configuration.html)

**Architect** - Review [Conceptual Guides](/documentation/conceptual/) → [API Reference](/documentation/api-reference/)

## Need Help?

- **GitHub Issues** - [Report bugs or request features](https://github.com/naderfares/spring-data-dynamodb/issues)
- **Stack Overflow** - Tag questions with `spring-data-dynamodb`
- **Source Code** - [Browse on GitHub](https://github.com/naderfares/spring-data-dynamodb)

## Contributing

Contributions are welcome! Please read the contributing guidelines in the project repository.

## License

Spring Data DynamoDB is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).
