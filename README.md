# reproducer-graphql

This is a reproducer for 2 issues when using OIDC and GraphQL Client.

The first issue is related to using the default `oidc-client` configuration.
The second issue is related to the generated token provided not being registered for reflection.

Those issues have been introduced in Quarkus 3.31. 
And the issues have been tested to remain in 3.33 up to 3.37.
See https://github.com/quarkusio/quarkus/pull/51743 and https://github.com/quarkusio/quarkus/issues/47875

## Prerequisite

Configure JDK 21 and Start keycloak with:
```shell script
docker compose -f compose-devservices.yml up
```

## Issue with default `oidc-client` configuration

The following use cases reproduce the issue with various versions of quarkus and set of properties 
called `option1` and `option2` activated at build time using profiles.

### option 1 used to work up to 3.30
```
mvn clean package -Dquarkus.profile=option1 -f pom.3.30.xml
java -jar target/quarkus-app/quarkus-run.jar
curl localhost:18080/hello/graphql
=> OK
```

### option 1 does not work anymore on 3.31
```
mvn clean package -Dquarkus.profile=option1 -f pom.3.31.xml
java -jar target/quarkus-app/quarkus-run.jar
=> Caused by: java.lang.NullPointerException: Cannot invoke "String.startsWith(String)" because "name" is null
```

### required to move to option 2 to get it to work on 3.31
```
mvn clean package -Dquarkus.profile=option2 -f pom.3.31.xml
java -jar target/quarkus-app/quarkus-run.jar
curl localhost:18080/hello/graphql
=> OK
```

### still failing on 3.33 with option 1
```
mvn clean package -Dquarkus.profile=option1 -f pom.3.33.xml
java -jar target/quarkus-app/quarkus-run.jar
=> Caused by: java.lang.NullPointerException: Cannot invoke "String.startsWith(String)" because "name" is null
```

### still failing on 3.37 with option 1
```
mvn clean package -Dquarkus.profile=option1 -f pom.3.37.xml
java -jar target/quarkus-app/quarkus-run.jar
=> Caused by: java.lang.NullPointerException: Cannot invoke "String.startsWith(String)" because "name" is null
```

## Issue with native not registering the generated class for reflection

### native used to work up until 3.30
```
mvn clean package -Dquarkus.profile=option1 -f pom.3.30.xml -Dnative
docker build -f src/main/docker/Dockerfile.native-micro -t quarkus/reproducer-graphql_3.30 .
docker run -i --rm -e KCHOST=host.docker.internal -p 18080:18080 quarkus/reproducer-graphql_3.30
curl localhost:18080/hello/graphql
=> OK
```

### does not work in 3.31, even with option 2
```
mvn clean package -Dquarkus.profile=option2 -f pom.3.31.xml -Dnative
docker build -f src/main/docker/Dockerfile.native-micro -t quarkus/reproducer-graphql_3.31 .
docker run -i --rm -e KCHOST=host.docker.internal -p 18080:18080 quarkus/reproducer-graphql_3.31
=> Caused by: java.lang.ClassNotFoundException: io.quarkus.oidc.client.graphql.runtime.AbstractGraphQLTokenProvider_foo_bar
```

### still failing in 3.33
```
mvn clean package -Dquarkus.profile=option2 -f pom.3.33.xml -Dnative
docker build -f src/main/docker/Dockerfile.native-micro -t quarkus/reproducer-graphql_3.33 .
docker run -i --rm -e KCHOST=host.docker.internal -p 18080:18080 quarkus/reproducer-graphql_3.33
=> Caused by: java.lang.ClassNotFoundException: io.quarkus.oidc.client.graphql.runtime.AbstractGraphQLTokenProvider_foo_bar
```

### still failing in 3.37
```
mvn clean package -Dquarkus.profile=option2 -f pom.3.37.xml -Dnative
docker build -f src/main/docker/Dockerfile.native-micro -t quarkus/reproducer-graphql_3.37 .
docker run -i --rm -e KCHOST=host.docker.internal -p 18080:18080 quarkus/reproducer-graphql_3.37
=> Caused by: java.lang.ClassNotFoundException: io.quarkus.oidc.client.graphql.runtime.AbstractGraphQLTokenProvider_foo_bar
```
