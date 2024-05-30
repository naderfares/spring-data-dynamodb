# 5.2.6

## New Features

# 5.2.5

## New Features
1. [Allow single object as parameter to query a set/list](https://github.com/boostchicken/spring-data-dynamodb/issues/33)

# 5.2.4

## Housekeeping
1. Built against Spring Data 2.3.0, not changes needed.

## Bug fixes
1. [Missing LSI Indexes Projection Settings](https://github.com/boostchicken/spring-data-dynamodb/issues/19)
2. [Failed to register dynamoDBMapperRef](https://github.com/boostchicken/spring-data-dynamodb/issues/25)
3. [Limit and Consistent Reads only works on GSIs](https://github.com/boostchicken/spring-data-dynamodb/issues/23)

## New Features
1. [Ability to apply filter expressions to a Query](https://github.com/boostchicken/spring-data-dynamodb/issues/27)

With static parameters
```	
@Query(fields = "leaveDate", limit = 1, filterExpression = "contains(#field, :value)",
			expressionMappingNames = {@ExpressionAttribute(key = "#field", value = "name")},
			expressionMappingValues = {@ExpressionAttribute(key=":value", value = "projection")})
	List<User> findByPostCode(String postCode);
```

With dynamic parameters from methods
```
	@Query(fields = "leaveDate", limit = 1, filterExpression = "contains(#field, :value)",
			expressionMappingNames = {@ExpressionAttribute(key = "#field", value = "name")},
			expressionMappingValues = {@ExpressionAttribute(key=":value", parameterName = "projection")})
	List<User> findByPostCode(@Param("postCode") String postCode, @Param("projection") String projection);
```
2. [Support for Nested Repositories](https://github.com/boostchicken/spring-data-dynamodb/pull/24)
```
@EnableDynamoDBRepositories(basePackages = "io.github.naderfares.spring.data.dynamodb.domain.sample", considerNestedRepositories = true)
```