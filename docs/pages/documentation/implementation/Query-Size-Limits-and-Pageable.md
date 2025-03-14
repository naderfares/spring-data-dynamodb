---
layout: page
parent: Implementation
title: Query Size Limits and Pageable
---


The AWS SDK tries to load all results of a query into memory as one single list.

To activate lazy loading, the following `DynamoDBMapperConfig` has to be used:

```java
DynamoDBMapperConfig.Builder builder = new DynamoDBMapperConfig.Builder();
builder.

setPaginationLoadingStrategy(PaginationLoadingStrategy.ITERATION_ONLY);
```

This causes the result to be available as a lazy-loaded list - as long as no methods are called that affect the whole
list.
Those methods (like `size()`) will transparently retrieve the full result set!

Having the above `ITERATION_ONLY` `PaginationLoadingStrategy` in place allows to use the [
`Pageable`](https://docs.spring.io/spring-data/data-commons/docs/current/reference/html/#repositories.special-parameters)
parameter on `find` methods.
