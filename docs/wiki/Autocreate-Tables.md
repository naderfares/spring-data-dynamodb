(This feature is available since version `5.1.0`)

The required tables for operation can be created during application startup.
This feature is inspired by Hibernate's autocreate feature and provides the following functionality via the
configuration properties with the defaults below:

```
spring.data.dynamodb.entity2ddl.auto = none
spring.data.dynamodb.entity2ddl.gsiProjectionType = ALL
spring.data.dynamodb.entity2ddl.readCapacity = 10
spring.data.dynamodb.entity2ddl.writeCapacity = 1
```

# Available Modes

## none

No action will be performed.

## create-only

Database creation will be generated on ApplicationContext startup.
Please note [Creation table defaults](#Creation) details.

## drop

Database dropping will be generated on ApplicationContext shutdown.

## create

Database dropping will be generated followed by database creation on ApplicationContext startup.

Please note [Creation table defaults](#Creation) details.

## create-drop

Drop the schema and recreate it on ApplicationContext startup.
Additionally, drop the schema on ApplicationContext shutdown.

Please note [Creation table defaults](#Creation) details.

## validate

Validate the database schema structure on ApplicationContext startup.
The capacity is not checked at all.

# Creation

If the table creation is required, the following defaults are applied:

* All required GSIs' use `spring.data.dynamodb.entity2ddl.gsiProjectionType` as the projection
* All required GIS's use `spring.data.dynamodb.entity2ddl.readCapacity` /
  `spring.data.dynamodb.entity2ddl.writeCapacity` for the capacity
