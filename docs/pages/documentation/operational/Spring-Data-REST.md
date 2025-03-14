---
layout: page
parent: Operational
title: Spring Data REST
---

## Introduction

`spring-data-dynamodb` is compatible with [Spring Data REST](https://projects.spring.io/spring-data-rest/).
It uses a [
`PersistentEntityResourceAssembler`](https://docs.spring.io/spring-data/rest/docs/current/api/org/springframework/data/rest/webmvc/PersistentEntityResourceAssembler.html)
that requires the [
`DynamoDBMappingContext`](https://github.com/derjust/spring-data-dynamodb/blob/master/src/main/java/org/socialsignin/spring/data/dynamodb/mapping/DynamoDBMappingContext.java)
to be exposed as a Spring Bean.

## Usage

To use [Spring Data REST](https://projects.spring.io/spring-data-rest/), the additional Bean must be registered.
If such a bean is already available in the `ApplicationContext` it still has to registered via the `mappingContextRef`!:

```java
@Configuration
@EnableDynamoDBRepositories(mappingContextRef = "dynamoDBMappingContext")
public class DynamoDBConfig {

	/* ... other beans like AmazonDynamoDB ... */

        @Bean
        public DynamoDBMappingContext dynamoDBMappingContext() {
               return new DynamoDBMappingContext();
        }
}
```
