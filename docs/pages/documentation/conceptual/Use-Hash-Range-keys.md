---
layout: page
parent: Conceptual
title: Use Hash-Range keys
---

# Overview

Using a `Range` and `Hash` as a combined key requires a little bit more code to leverage AWS SDK for DynamoDB and
Spring-Data:

In the entity have a field annotated with `@Id` referencing a custom "key" class - and exposes the attributes as direct
getters/setter with the `@DynamoDBHashKey` / `@DynamoDBRangeKey` annotation including the column configuration.

The background for this mumbojumbo is that Spring-Data requires to have a dedicated entity representing the key. As the
`Repository.find()` method takes exactly one argument - `T` from the specific derived repository.
Therefore the two fields, which constitute the key, have to be wrapped up in a single entity - the "key class" depicted
above.
This also applies to `delete` operations: If a combined key is used, also the `Repository.deleteById()` method must be
used.

# Example

## Domain class

```java
@DynamoDBTable
public class Playlist {
	@Id
	private PlaylistId playlistId;

	// Any other field and their getter/setter + @DynamoDBAttribute annotation

	@DynamoDBHashKey(attributeName = "UserName")
	public String getUserName() {
		return playlistId != null ? playlistId.getUserName() : null;
	}

	public void setUserName(String userName) {
		if (playlistId == null) {
			playlistId = new PlaylistId();
		}
		playlistId.setUserName(userName);
	}

	@DynamoDBRangeKey(attributeName = "PlaylistName")
	public String getPlaylistName() {
		return playlistId != null ? playlistId.getPlaylistName() : null;
	}

	public void setPlaylistName(String playlistName) {
		if (playlistId == null) {
			playlistId = new PlaylistId();
		}
		playlistId.setPlaylistName(playlistName);
	}    
```

## Key class

The key class itself has only the hash and range member fields including the `@DynamoDBHashKey` / `@DynamoDBRangeKey`
annotation *again* - this time without table column configuration.

```java
public class PlaylistId implements Serializable {
  private static final long serialVersionUID = 1L;

  private String userName;
  private String playlistName;

  @DynamoDBHashKey
  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  @DynamoDBRangeKey
  public String getPlaylistName() {
    return playlistName;
  }

  public void setPlaylistName(String playlistName) {
    this.playlistName = playlistName;
  }
}
```

## Repository interface

* This also means that the generic for the Repository have to be updated to the new "key" class.

```java
public interface PlaylistRepository extends CrudRepository<Playlist, PlaylistId> {
}
```

An example can be found
in [this test case](https://github.com/derjust/spring-data-dynamodb/blob/master/src/test/java/org/socialsignin/spring/data/dynamodb/domain/sample/HashRangeKeyIT.java)
with the entity
class [Playlist](https://github.com/derjust/spring-data-dynamodb/blob/master/src/test/java/org/socialsignin/spring/data/dynamodb/domain/sample/Playlist.java) /
ID
class [PlaylistId](https://github.com/derjust/spring-data-dynamodb/blob/master/src/test/java/org/socialsignin/spring/data/dynamodb/domain/sample/PlaylistId.java).

## Common errors

### ```DynamoDBMappingException: not supported; requires @DynamoDBTyped or @DynamoDBTypeConverted```

Chances are that the `@Id` field has `get`ters/`set`ters: `DynamoDBMapper` now can't figure out how to serialize the
`Key` class.
The `@Id` field does not need `get`ters/`set`ters - please remove them: Spring-Data uses the annotated field directly -
and `DynamoDBMapper` (should) care only for the `@DynamoDBHashKey`/`@DynamoDBRangeKey` properties

### No field annotated with interface `org.springframework.data.annotation.Id` found!

This error message is the bottom line if something is not properly annotated.

### org.socialsignin.spring.data.dynamodb.exception.BatchDeleteException: Processing of entities failed!; nested exception is com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException: The provided key element does not match the schema

This exception is thrown if a custom repository method is added in the style of
`deleteByHashAndRangePropery(hashProperty, rangeProperty)`. To delete an entity via its combined key, the (built-in)
`deleteById(keyClass)` method must be called.

### java.lang.NullPointerException at org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityMetadataSupport.getPropertyNameForAccessorMethod

In the entity class, you may not have fields for the hash and range key fields.

### Check list

* You need a specialized class for your primary key. It needs
    * to implement `Serializable` (only before Spring 5.0/Spring-Data 2.0!) and
    * will have the fields, getters, and setters for your hash key field and range key field.
* In your entity class, instead of the hash key field and range key field, you have a field that is an instance of the
  specialized key class mentioned above. You must annotate that field with Spring's `@Id` annotation, but **do not
  provide a `get`ter or `set`ter**.
* Also in your entity class, do provide a getter and setter for your hash key field and range key field, even though
  your entity doesn't have those fields. You pass those through to the corresponding method on your primary key field.
* Make sure to lazy-create your primary key field in the hash key and range key setters in the entity.
* The hash key and range key getters in both your entity class and primary key class have to be annotated with
  `@DynamoDBHashKey` and `@DynamoDBRangeKey`, respectively. Yes, this is redundant.
* Make sure that your repo uses the primary key class for the `ID` class, otherwise you'll get another weird error about
  something not being an instance of the right class.

Do all of the above and the hash-key/range-ky combo will work properly.
