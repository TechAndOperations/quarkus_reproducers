# quarkus-jib-jms

to reproduce execute:
```
mvn clean verify -DskipITs=false -Dquarkus.container-image.build=true 
```

the issue is related to launching the IT jvm in a container with properties set as env variables: 
```
2026-02-27 15:00:14,685 INFO  [io.quarkus.test.common.DefaultDockerContainerLauncher] (main) Executing "docker run --name quarkus-integration-test-LlUdA -i --rm -p 8081:8081 -p 8444:8444 --net=16159872531d10ea59eca67ab98a571d3d5df7dbf62bc93491b7b28f12e0804d --env QUARKUS_LOG_CATEGORY__IO_QUARKUS__LEVEL=INFO --env QUARKUS_HTTP_PORT=8081 --env QUARKUS_HTTP_SSL_PORT=8444 --env TEST_URL=http://localhost:${quarkus.http.test-port:8081} --env QUARKUS_PROFILE=prod --env QUARKUS_CONTAINER_IMAGE_BUILD=true --env QUARKUS_TEST_CONTAINER_NETWORK=16159872531d10ea59eca67ab98a571d3d5df7dbf62bc93491b7b28f12e0804d --env QUARKUS_IRONJACAMAR_RA_CONFIG_CONNECTION_PARAMETERS=host=localhost;port=59543;protocols=CORE sevel/quarkus-jib-jms:1.0.0-SNAPSHOT" 
```

specifically the env variable `QUARKUS_IRONJACAMAR_RA_CONFIG_CONNECTION_PARAMETERS` translates into property `connection.parameters` and not `connection-parameters`
look at `ArtemisResourceAdapterFactory`:
```
    public ActiveMQResourceAdapter createResourceAdapter(String id, Map<String, String> config) {
        ActiveMQResourceAdapter adapter = new ActiveMQResourceAdapter();
        String connectionParameters = config.get("connection-parameters");
```
you can see we are looking for `connection-parameters` and not `connection.parameters` which is what we get from env variable.


if instead you execute the following, then it works:
```
mvn clean verify -DskipITs=false -Dquarkus.container-image.build=false 
```