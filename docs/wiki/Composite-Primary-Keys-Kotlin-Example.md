# Overview

This example demonstrates how to model DynamoDB HASH/RANGE partition keys using a SpringData-style composite primary key
and Kotlin data classes. I built this example from sample code submitted by [@rheber](https://github.com/rheber) as part
of issue #240: [Table with hash and range key in Kotlin](https://github.com/derjust/spring-data-dynamodb/issues/240).

It also borrows largely from an existing wiki article which demonstrates the said mapping in
Java: [Use Hash Range keys](https://github.com/derjust/spring-data-dynamodb/wiki/Use-Hash-Range-keys).

## Example

### Composite Key Class

```kotlin
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import java.io.Serializable

/**
 * Composite primary key for the [FoobarEntry] domain.
 */
@DynamoDBDocument
data class FoobarEntryId(

    @field:DynamoDBHashKey
    var foobarCode: String? = null,

    @field:DynamoDBRangeKey
    var foobarKey: String? = null

) : Serializable
```

### Domain Class

```kotlin
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import org.springframework.data.annotation.Id

@DynamoDBTable(tableName = "Foobars")
data class FoobarEntry(
    @field:Id
    @DynamoDBIgnore
    val id: FoobarEntryId = FoobarEntryId()
) {

    @DynamoDBHashKey(attributeName = "foobarCode")
    fun getFoobarCode() = id.foobarCode

    fun setFoobarCode(foobarCode: String) {
        id.foobarCode = foobarCode
    }

    @DynamoDBRangeKey(attributeName = "foobarKey")
    fun getFoobarKey() = id.foobarKey

    fun setFoobarKey(foobarKey: String) {
        id.foobarKey = foobarKey
    }

    @get:DynamoDBAttribute
    var foobarValue: String? = null
}
```

### Repository Interface

```kotlin
import org.springframework.data.repository.CrudRepository

interface FoobarRepository : CrudRepository<FoobarEntry, FoobarEntryId> {
    fun findAllByFoobarCode(foobarCode: String): Iterable<FoobarEntry>
}
```

I tested the example above using:

* Kotlin: 1.3.21
* Spring Boot: 2.1.3.RELEASE
* Spring Data DynamoDB Version: 5.0.4 (2.0)
* Spring Data Version: 2.0.8.RELEASE
* AWS SDK Version: 1.11.478
* Java Version: 1.8.0_201
* Platform: Linux 4.15.0-46-generic

Additionally, I created a JUnit 5 integration test that leverages the
excellent [testcontainers](https://github.com/testcontainers/testcontainers-java) library to launch
a [localstack](https://github.com/localstack/localstack) Docker container to emulate AWS DynamoDB in local mode. I am
providing the said test for illustration purposes only as it _does not run_ on its own.

### Integration Test

```kotlin
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FoobarRepositoryTest : BaseDynamoDbIntegrationTest() {
    @Autowired
    private lateinit var foobarRepository: FoobarRepository

    private val foobarEntryId = FoobarEntryId(foobarCode = "1", foobarKey = "key")

    @BeforeAll
    fun setupTestData() {
        foobarRepository.save(FoobarEntry(foobarEntryId).apply {
            foobarValue = "value"
        })
    }

    @AfterAll
    fun cleanTestData() {
        foobarRepository.deleteById(foobarEntryId)
    }

    @Test
    fun `Test findAllByFoobarCode() query finder works as expected`() {
        assertThat(foobarRepository.findAllByFoobarCode("1")).hasOnlyOneElementSatisfying { entry ->
            assertThat(entry.id.foobarCode).isEqualTo("1")
            assertThat(entry.id.foobarKey).isEqualTo("key")
            assertThat(entry.getFoobarCode()).isEqualTo("1")
            assertThat(entry.getFoobarKey()).isEqualTo("key")
            assertThat(entry.foobarValue).isEqualTo("value")
        }
    }
}
```
