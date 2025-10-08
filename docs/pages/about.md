---
layout: page
title: About
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

- [Getting Started Guide](/documentation/implementation/) - Step-by-step setup and examples
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

## History

The code base has some history already in it - let's clarify it a bit:

* The code base was established
  under [github.com/michaellavelle/spring-data-dynamodb)](https://github.com/michaellavelle/spring-data-dynamodb)
* It was forked and further maintained
  under [github.com/derjust/spring-data-dynamodb)](https://github.com/derjust/spring-data-dynamodb)
    * Available in Maven Central under [
      `com.github.derjust:spring-data-dynamodb`](http://central.maven.org/maven2/com/github/derjust/spring-data-dynamodb/)
* It was forked and even further maintained
  under [github.com/boostchicken/spring-data-dynamodb)](https://github.com/boostchicken/spring-data-dynamodb)
    * Available in Maven Central under [
      `io.github.boostchicken:spring-data-dynamodb`](https://repo1.maven.org/maven2/io/github/boostchicken/spring-data-dynamodb/)
* It was forked and even further maintained
  under [github.com/boostchicken/spring-data-dynamodb)](https://github.com/boostchicken/spring-data-dynamodb)
    * Available in Maven Central under [
      `io.github.boostchicken:spring-data-dynamodb`](https://repo1.maven.org/maven2/io/github/boostchicken/spring-data-dynamodb/)
* It was forked and even further maintained
  under [github.com/prasanna0586/spring-data-dynamodb)](https://github.com/prasanna0586/spring-data-dynamodb)
    * Available in Maven Central under [
      `io.github.prasanna0586:spring-data-dynamodb`](https://repo1.maven.org/maven2/io/github/prasanna0586/spring-data-dynamodb/)
* It was forked and even further maintained
  under [github.com/naderfares/spring-data-dynamodb)](https://github.com/naderfares/spring-data-dynamodb)
    * Available in Maven Central under [
      `io.github.naderfares:spring-data-dynamodb`](https://repo1.maven.org/maven2/io/github/naderfares/spring-data-dynamodb/)
