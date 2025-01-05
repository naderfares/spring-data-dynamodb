```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dynamodb="http://docs.socialsignin.org/schema/data/dynamodb"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://docs.socialsignin.org/schema/data/dynamodb
                           http://derjust.github.io/spring-data-dynamodb/spring-dynamodb-1.0.xsd">

    <bean id="amazonDynamoDB" class="com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient">
        <constructor-arg ref="amazonAWSCredentials"/>
        <property name="endpoint" value="${amazon.dynamodb.endpoint}"/>
    </bean>

    <bean id="amazonAWSCredentials" class="com.amazonaws.auth.BasicAWSCredentials">
        <constructor-arg value="${amazon.aws.accesskey}"/>
        <constructor-arg value="${amazon.aws.secretkey}"/>
    </bean>

    <dynamodb:repositories base-package="com.acme.repositories" amazon-dynamodb-ref="amazonDynamoDB"/>

</beans>

```
