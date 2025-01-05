# Overview

> Amazon DynamoDB Accelerator (DAX) is a fully managed, highly available, in-memory cache for DynamoDB that delivers up
> to a 10x performance improvement – from milliseconds to microseconds – even at millions of requests per second. DAX does
> all the heavy lifting required to add in-memory acceleration to your DynamoDB tables, without requiring developers to
> manage cache invalidation, data population, or cluster management.

Source: [https://aws.amazon.com/dynamodb/dax/](https://aws.amazon.com/dynamodb/dax/)

# Explanation

`spring-data-dynamodb` does not have any state information about any entity.
Only meta data about entity classes are cached.

In fact most of the classes are Spring Bean singletons that never should have any (shared) state. It is fine to use DAX
from a `spring-data-dynamodb` perspective.

# Sample

## 1. Add DAX client JAR

Download the JAR though [Maven](https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk-dax/):

```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-dax</artifactId>
    <version>1.11.271</version>
</dependency>
```

or via Gradle

```
compile group: 'com.amazonaws', name: 'aws-java-sdk-dax', version: '1.11.271'
```

## 2. Configure DynamoDB to use DAX

```java
@Configuration
@EnableDynamoDBRepositories(basePackages = "com.acme.repositories")
public class DynamoDBConfig {

    @Value("${amazon.dynamodb.endpoint}")
    private String amazonDynamoDBEndpoint;

    @Value("${amazon.dynamodb.region}")
    private String amazonDynamoDBRegion;

    @Value("${amazon.aws.accesskey}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretkey}")
    private String amazonAWSSecretKey;

    @Value("${amazon.dax.endpoint}")
    private String daxEndpoint;

    @Bean
    public AmazonDynamoDB amazonDynamoDB(AWSCredentials amazonAWSCredentials) {
        AmazonDaxClientBuilder daxClientBuilder = AmazonDaxClientBuilder.standard();
        daxClientBuilder.withCredentials(new AWSStaticCredentialsProvider(amazonAWSCredentials));

        daxClientBuilder.withRegion(amazonDynamoDBRegion).withEndpointConfiguration(daxEndpoint);

        return daxClientBuilder.build();
    }

    @Bean
    public AWSCredentials amazonAWSCredentials() {
        // Or use an AWSCredentialsProvider/AWSCredentialsProviderChain
        return new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
    }
}
```

## 3. Update configuration

Compared to the basic sample two new configuration properties have been introduced and must be set:

* `amazon.dax.endpoint`: The endpoint of your DAX cluster. Use the DynamoDB console—choose your DAX cluster. The cluster
  endpoint is shown in the console
* `amazon.dynamodb.region`: The region of your DynamoDB DAX cluster

[Source](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DAX.client.run-application-java.html)
