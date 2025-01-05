By default, table names are statically defined via the entity class annotation:

```java
package com.acme.repository;

@DynamoDBTable(tableName = "someEntityTableName")
public class SomeEntity {
    ...
}
```

The table names can be altered via an altered `DynamoDBMapperConfig` bean:

> **⚠️ Using the correct builder for `DynamoDBMapperConfig` is important! ⚠️**
>
> [
`new DynamoDBMapperConfig.Builder()`](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/dynamodbv2/datamodeling/DynamoDBMapperConfig.Builder.html#Builder--)
> should be used to start with a [
`DEFAULT`](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/dynamodbv2/datamodeling/DynamoDBMapperConfig.html#DEFAULT)
> configuration and only overriding fields that should differ - like [
`TableNameOverride`](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/dynamodbv2/datamodeling/DynamoDBMapperConfig.Builder.html#withTableNameOverride-com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride-).
>
> Older versions of this sample code started with [
`DynamoDBMapperConfig.builder()`](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/dynamodbv2/datamodeling/DynamoDBMapperConfig.html#builder--)
> which sets all fields to `null` potentially causing `NullPointerException`s later on!

```java
@Configuration
@EnableDynamoDBRepositories(
    dynamoDBMapperConfigRef = "dynamoDBMapperConfig",  // This literal has to match the bean name - otherwise the default DynamoDBMapperConfig will be used
    basePackages = "com.acme.repository") // The package with the @DynamoDBTable entity classes
public class DynamoDBConfig {
    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig(TableNameOverride tableNameOverrider) {
        // Create empty DynamoDBMapperConfig builder
	DynamoDBMapperConfig.Builder builder = new DynamoDBMapperConfig.Builder();
	// Inject missing defaults from the deprecated method
	builder.withTypeConverterFactory(DynamoDBTypeConverterFactory.standard());
	builder.withTableNameResolver(DefaultTableNameResolver.INSTANCE);
        // Inject the table name overrider bean
	builder.withTableNameOverride(tableNameOverrider());
	return builder.build();
    }
    
    @Bean
    public TableNameOverride tableNameOverrider() {
        ...
    }
}
```

[
`TableNameOverride`](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/dynamodbv2/datamodeling/DynamoDBMapperConfig.TableNameOverride.html)
is a AWS SDK DynamoDB class and not exclusive to spring-data-dynamodb. There are `static` builder methods that expose
some default behavior that is sufficient for most scenarios:

Prefix each table with a literal:

```java
    @Bean
    public TableNameOverride tableNameOverrider() {
        String prefix = ... // Use @Value to inject values via Spring or use any logic to define the table prefix
        return TableNameOverride.withTableNamePrefix(prefix);
    }
```

or resolve each table name to the same one:

```java
    @Bean
    public TableNameOverride tableNameOverrider() {
        String singleTableName = ... // Use @Value to inject values via Spring or use any logic to define the table prefix
        return TableNameOverride.withTableNameReplacement(singleTableName);
    }
```
