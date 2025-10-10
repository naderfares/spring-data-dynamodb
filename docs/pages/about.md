---
layout: page
title: About
nav_order: 2
permalink: /about/
---

[![Build Status](https://github.com/naderfares/spring-data-dynamodb/workflows/CI/badge.svg)](https://github.com/naderfares/spring-data-dynamodb/actions)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.naderfares/spring-data-dynamodb/badge.svg)](https://search.maven.org/search?q=g:io.github.naderfares)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Spring Data DynamoDB

**Version 6.0.11** - Latest stable release for Spring Boot 3.x and Spring Framework 6.x

The primary goal of the [Spring® Data](https://projects.spring.io/spring-data/) project is to make it easier to build
Spring-powered applications that use data access technologies.

This module provides enhanced support for building data access layers
on [AWS DynamoDB](https://aws.amazon.com/dynamodb/), bringing the familiar Spring Data programming model to DynamoDB
with repositories, query methods, and advanced features.

## Feature Highlights

### Core Capabilities

- **Repository Abstraction** - Use familiar `@Repository` interfaces with CRUD operations
- **Query Methods** - Derive queries from method names like `findByLastName(String lastName)`
- **Paging & Sorting** - Built-in support for paginated results
- **Query DSL** - Type-safe queries using method name derivation
- **Custom Implementations** - Extend repositories with custom logic
- **DynamoDB Template** - Low-level operations for advanced use cases

### DynamoDB-Specific Features

- **Hash & Range Keys** - Full support for composite primary keys
- **Global Secondary Indexes** - Query on alternate keys efficiently
- **Local Secondary Indexes** - Additional sort keys on the same partition
- **Projections** - Fetch only required attributes to reduce cost
- **Batch Operations** - Efficient batch reads and writes
- **Scan Protection** - Prevent accidental expensive scans with `@EnableScan`

### Integration Features

- **Spring Boot Auto-Configuration** - Zero-configuration setup
- **Spring Data REST** - RESTful endpoints from repositories
- **Spring Events** - Lifecycle events (BeforeSave, AfterLoad, etc.)
- **AWS SDK Integration** - Seamless integration with AWS SDK v1
- **DynamoDB Local** - Test support with local DynamoDB

## Quick Start

Download the JAR through [Maven Central](https://mvnrepository.com/artifact/io.github.naderfares/spring-data-dynamodb)

### Maven

Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.naderfares</groupId>
    <artifactId>spring-data-dynamodb</artifactId>
    <version>6.0.11</version>
</dependency>
```

### Gradle

```gradle
implementation 'io.github.naderfares:spring-data-dynamodb:6.0.11'
```

### Gradle (Kotlin DSL)

```kotlin
implementation("io.github.naderfares:spring-data-dynamodb:6.0.11")
```

## Getting Started

Ready to dive in? Check out these resources:

- [Getting Started Guide](/documentation/getting-started/) - Step-by-step setup and examples
- [Configuration](/documentation/api-reference/configuration.html) - Configure DynamoDB repositories
- [Query Methods](/documentation/api-reference/query-methods.html) - Learn query method patterns
- [Full Documentation](/documentation/) - Complete reference documentation

## Version & Spring Framework Compatibility

The major and minor version numbers align with the compatible Spring Framework version. The patch version follows
SEMVER.

API changes follow SEMVER and align loosely with Spring Framework releases.

| Version    | Spring Boot       | Spring Framework | Spring Data    | Status      |
|------------|-------------------|------------------|----------------|-------------|
| **6.0.11** | **>= 3.5.6**      | **>= 6.2.11**    | **2025.0.4**   | **Current** |
| 6.0.x      | >= 3.2.5          | >= 6.1.6         | 2023.1.5       | Maintained  |
| 5.2.x      | >= 2.2            | >= 5.2           | Moore, Neumann | Legacy      |
| 5.1.x      | 2.1               | >= 5.1           | Lovelace-SR1   | Legacy      |
| 5.0.x      | >= 2.0, < 2.1     | >= 5.0, < 5.1    | Kay-SR1        | End of Life |
| 4.5.x      | >= 1.4.0, < 2.0   | >= 4.3, < 5.0    | Ingalls        | End of Life |
| 4.4.x      | >= 1.4.0, < 2.0   | >= 4.3, < 5.0    | Hopper-SR2     | End of Life |
| 4.3.x      | >= 1.4.0, < 2.0   | >= 4.3, < 5.0    | Gosling-SR1    | End of Life |
| 4.2.x      | >= 1.3.0, < 1.4.0 | >= 4.2, < 4.3    | Gosling-SR1    | End of Life |
| 1.0.x      | N/A               | >= 3.1, < 4.2    | N/A            | End of Life |

`spring-data-dynamodb` depends directly on `spring-data` and `spring-tx`.

`compile` and `runtime` dependencies are kept to a minimum to allow easy integration, especially in Spring Boot
projects.

## About the Project

Spring Data DynamoDB brings the powerful Spring Data abstraction layer to AWS DynamoDB, enabling developers to build scalable, production-ready applications with minimal boilerplate code. The project provides repository-based data access, query method derivation, and advanced DynamoDB features while maintaining the familiar Spring programming model.

### Goals

- **Developer Productivity** - Reduce boilerplate and accelerate development with repository abstractions
- **Best Practices** - Encourage DynamoDB best practices through intuitive APIs and scan protection
- **Spring Integration** - Seamless integration with Spring Boot, Spring Data, and the Spring ecosystem
- **Production Ready** - Robust error handling, batch operations, and comprehensive testing

## Community & Support

- **GitHub Repository**: [naderfares/spring-data-dynamodb](https://github.com/naderfares/spring-data-dynamodb)
- **Issue Tracker**: [Report bugs or request features](https://github.com/naderfares/spring-data-dynamodb/issues)
- **Discussions**: [Community discussions and Q&A](https://github.com/naderfares/spring-data-dynamodb/discussions)
- **Maven Central**: [Latest releases](https://search.maven.org/search?q=g:io.github.naderfares)

### Contributing

Contributions are welcome! Please see our [contributing guidelines](https://github.com/naderfares/spring-data-dynamodb/blob/develop/CONTRIBUTING.md) for details on:

- Reporting bugs and requesting features
- Submitting pull requests
- Code style and testing requirements
- Release process

## Project History

This project has evolved through community contributions across several forks:

- **Original**: [michaellavelle/spring-data-dynamodb](https://github.com/michaellavelle/spring-data-dynamodb) - Initial implementation
- **Fork**: [derjust/spring-data-dynamodb](https://github.com/derjust/spring-data-dynamodb) (`com.github.derjust:spring-data-dynamodb`)
- **Fork**: [boostchicken/spring-data-dynamodb](https://github.com/boostchicken/spring-data-dynamodb) (`io.github.boostchicken:spring-data-dynamodb`)
- **Fork**: [prasanna0586/spring-data-dynamodb](https://github.com/prasanna0586/spring-data-dynamodb) (`io.github.prasanna0586:spring-data-dynamodb`)
- **Current**: [naderfares/spring-data-dynamodb](https://github.com/naderfares/spring-data-dynamodb) (`io.github.naderfares:spring-data-dynamodb`)

Each fork has contributed improvements, bug fixes, and adaptations to newer Spring Framework versions, culminating in the current Spring Boot 3.x and Spring Framework 6.x support.

## License

Spring Data DynamoDB is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).
