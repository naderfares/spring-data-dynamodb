# Overview

Spring-Data provides hooks to add customized methods to a repository bean to allow for any kind of custom handling that
can not be achieved via the regular `find`
methods: [Custom implementations for Spring Data repositories](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.custom-implementations)

`spring-data-dynamodb` is not different to any other provider - thus here is an example:

# Example

## 1. Create the custom methods

First we need to declare the custom methods that needs to be implemented:

```java
public interface DeviceValueAdditionRepository {

    String fancyCustomMethod();
}
```

## 2. Add the custom methods to the repository

Next add the 'custom interface' to the regular interface (the one which is implemented by `spring-data-dynamodb` magic).
Everything that applies to regular DynamoDB repositories is still true & valid here.

```java
@EnableScan
public interface DeviceValueRepository 
       extends CrudRepository<DeviceValue, DeviceValueKey>,
               DeviceValueAdditionRepository {

    List<DeviceValue> findAll();
}
```

## 3. Implement the custom methods

As last step the custom methods needs to be implemented.
It is important to name the class implementing the custom interface the same as the interface *with a `Impl` suffix*

```java
// Most important this class has to be named like the interface with an 'Impl' suffix
// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.single-repository-behavior
public class DeviceValueRepositoryImpl implements DeviceValueAdditionRepository {

    // Inject everything you want as this is created like a normal bean
    @Autowired
    DynamoDBTemplate dynamoDBTemplate;

    @Override
    public String fancyCustomMethod() {
        // custom code here

        DeviceValue dv = dynamoDBTemplate.load(DeviceValue.class, "42");
        if (dv == null) {
            return "Not found";
        } else {
            return dv.getTag();
        }
    }
}
``` 

## 4. Use it

Afterwards the new, extended repository can be used as any other repository:

```java
@Autowired
private DeviceValueRepository repository;
	
public void callingMethod() {

    // Call a standard method from the repository
    repository.findAll();

    // Call the custom-implemented method 
    repository.fancyCustomMethod();
}
```

Spring takes care of blending all implementations properly together and exposing everything under the one repository
interface.
