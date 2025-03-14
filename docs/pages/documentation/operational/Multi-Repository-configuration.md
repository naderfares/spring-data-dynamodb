---
layout: page
parent: Operational
title: Multi Repository configuration
---


Multiple repository factories try to register all repository interfaces with them - which doesn't work very well.
Especially because spring-boot does a lot of auto-magic behind the curtain via the implicit existing
`@EnableJpaRepositories`.

The solution is to tell JPA and DynamoDB which repositories they are responsibility for (which then also drives which
entities are registered):

For Spring-Boot:

```java

@SpringBootApplication
@EnableJpaRepositories(
    includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {CustomerRepository.class})})
@EnableDynamoDBRepositories(includeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {DeviceValueRepository.class})})
public class Application {
```

Same for a configuration class:

```java
@Configuration
@EnableJpaRepositories(includeFilters = {
//or use basePackages or excludeFilters
	@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {CustomerRepository.class})
})
@EnableDynamoDBRepositories(includeFilters = {
//or use basePackages or excludeFilters
	@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {DeviceValueRepository.class})
})
public class AppConfig {
```

See also

* [Using multiple datasources with Spring Boot and Spring Data](https://medium.com/@joeclever/using-multiple-datasources-with-spring-boot-and-spring-data-6430b00c02e7)
* [Spring JPA – Multiple Databases](http://www.baeldung.com/spring-data-jpa-multiple-databases)
