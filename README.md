---
layout: page
title: About
permalink: /about/
---

[![Build Status](https://travis-ci.org/prasanna0586/spring-data-dynamodb.svg?branch=develop)](https://travis-ci.org/prasanna0586/spring-data-dynamodb)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.prasanna0586/spring-data-dynamodb/badge.svg)](https://search.maven.org/search?q=g:io.github.prasanna0586)

# Spring  Data DynamoDB #

The primary goal of the [Spring® Data](https://projects.spring.io/spring-data/) project is to make it easier to build
Spring-powered applications that use data access technologies.

This module deals with enhanced support for a data access layer built
on [AWS DynamoDB](https://aws.amazon.com/dynamodb/).

Technical infos can be found on the [project page](https://naderfares.github.io/spring-data-dynamodb/).


## Quick Start ##

Download the JAR though [Maven Central](https://mvnrepository.com/artifact/io.github.naderfares/spring-data-dynamodb)

Add the following to your `pom.xml`:
```xml
<dependency>
  <groupId>io.github.naderfares</groupId>
  <artifactId>spring-data-dynamodb</artifactId>
  <version>6.0.9</version>
</dependency>
```

or via Gradle

```gradle 
compile group: 'io.github.naderfares', name: 'spring-data-dynamodb', version: '6.0.9'
```

## Version & Spring Framework compatibility ##

The major and minor number of this library refers to the compatible Spring framework version. The build number is used
as specified by SEMVER.

API changes will follow SEMVER and loosely the Spring Framework releases.

| `spring-data-dynamodb` version | Spring Boot compatibility | Spring Framework compatibility | Spring Data compatibility     |
|--------------------------------|---------------------------|--------------------------------|-------------------------------|
| 1.0.x                          |                           | \>= 3.1 && \< 4.2              |                               |
| 4.2.x                          | \>= 1.3.0 && < 1.4.0      | \>= 4.2 && \< 4.3              | Gosling-SR1                   |
| 4.3.x                          | \>= 1.4.0 && < 2.0        | \>= 4.3 && \< 5.0              | Gosling-SR1                   |
| 4.4.x                          | \>= 1.4.0 && < 2.0        | \>= 4.3 && \< 5.0              | Hopper-SR2                    |
| 4.5.x                          | \>= 1.4.0 && < 2.0        | \>= 4.3 && \< 5.0              | Ingalls                       |
| 5.0.x                          | \>= 2.0 && < 2.1          | \>= 5.0 && \< 5.1              | Kay-SR1                       |
| 5.1.x                          | == 2.1                    | \>= 5.1                        | Lovelace-SR1                  |
| 5.2.x                          | \>= 2.2                   | \>= 5.2                        | Moore-RELEASE, Nuemann-RELASE |
| 6.0.3                          | \>= 3.2.5                 | \>=6.1.6                       | 2023.1.5                      |
| 6.0.4                          | \>= 3.5.6                 | \>=6.2.11                      | 2025.0.4                      |

`spring-data-dynamodb` depends directly on `spring-data` as also `spring-tx`.

`compile` and `runtime` dependencies are kept to a minimum to allow easy integration, for example into
Spring-Boot projects.

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
