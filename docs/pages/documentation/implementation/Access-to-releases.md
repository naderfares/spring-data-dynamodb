---
layout: page
title: Access to releases
parent: Implementation
---

Regular releases are available
via [Maven Central](http://mvnrepository.com/artifact/com.github.derjust/spring-data-dynamodb) and should not require
any additional setup.

[`snapshot` releases](https://oss.sonatype.org/content/repositories/snapshots/com/github/derjust/spring-data-dynamodb/)
are available via the OSSRH snapshot repository.

Those `snapshot` releases are build straight from `master` whenever there is PR merged. Even though `master` should be
considered 'working' and is good for testing upcoming releases by no means any production-readiness can be assumed.

## Maven

### via `pom.xml`

The _quickest_ way is to add to the `pom.xml`
Add to `pom.xml`

```xml

<repositories>
    <repository>
        <id>snapshots-repo</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

### via profiles

The _recommended_ way is to add/extend a profile in `~/.m2/settings.xml`

```xml
<profiles>
  <profile>
     <id>allow-snapshots</id>
        <activation><activeByDefault>true</activeByDefault></activation>
     <repositories>
       <repository>
         <id>snapshots-repo</id>
         <url>https://oss.sonatype.org/content/repositories/snapshots</url>
         <releases><enabled>false</enabled></releases>
         <snapshots><enabled>true</enabled></snapshots>
       </repository>
     </repositories>
   </profile>
</profiles>
```

So it can be activated/deactivated via
Maven's (https://maven.apache.org/guides/introduction/introduction-to-profiles.html)[build profiles].

## Gradle

```gradle
maven {
  url 'https://oss.sonatype.org/content/repositories/snapshots'
}
```

## Additional resources

* [How to download SNAPSHOT version from maven SNAPSHOT repository?](https://stackoverflow.com/questions/7715321/how-to-download-snapshot-version-from-maven-snapshot-repository/7717234#7717234)
  on StackOverflow.com
