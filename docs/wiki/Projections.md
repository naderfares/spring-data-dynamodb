Projections are supported by the [
`@Query` annotation](https://derjust.github.io/spring-data-dynamodb/apidocs/org/socialsignin/spring/data/dynamodb/repository/Query.html)
via
DynamoDB's [Projection Expressions](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.ProjectionExpressions.html)

To use a projection, the `@Query` annotation must be added to the Repository interface and the desired fields must be
provided (comma separated):

```java
import org.socialsignin.spring.data.dynamodb.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface UserRepository extends CrudRepository<User, String> {

	@Query(fields = "leaveDate")
	List<User> findByPostCode(String postCode);
```

All attributes that are not specified in the `fields` list will be `null` - this includes the hash/range key attributes!

**Note**:

* If projections are used on Global Secondary Indexes, the index must contain the desired fields in the first place (
  either by `SELECT`ed attributes or via `ALL`)
* This feature is available since version `5.0.3`
