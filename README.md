# quarkus-jib-jms

to reproduce execute:
```
mvn clean verify -DskipITs=false -Dquarkus.container-image.build=true 
```

during the execution of the IT we can see:
```
2026-03-03 19:36:12,708 INFO  [io.quarkus.test.common.DefaultDockerContainerLauncher] (main) Executing "docker run --name quarkus-integration-test-hRqOG -i --rm -p 8081:8081 -p 8444:8444 --net=2a0eecdbfa1f410f6a286d53a89a55a70cf4b0ddd4d3b09c648b8b87db02f67a --env QUARKUS_LOG_CATEGORY__IO_QUARKUS__LEVEL=INFO --env QUARKUS_HTTP_PORT=8081 --env QUARKUS_HTTP_SSL_PORT=8444 --env TEST_URL=http://localhost:${quarkus.http.test-port:8081} --env QUARKUS_PROFILE=prod --env QUARKUS_CONTAINER_IMAGE_BUILD=true --env QUARKUS_TEST_CONTAINER_NETWORK=2a0eecdbfa1f410f6a286d53a89a55a70cf4b0ddd4d3b09c648b8b87db02f67a --env QUARKUS_IRONJACAMAR_RA_CONFIG_CONNECTION_PARAMETERS=host=localhost;port=50176;protocols=CORE sevel/quarkus-jib-jms:1.0.0-SNAPSHOT"
```

and in the `quarkus.log` we see: 
```
2026-03-03 18:36:15,146 DEBUG [org.apache.activemq.artemis.core.client.impl.ClientSessionFactoryImpl] (executor-thread-2) Connector towards NettyConnector [host=localhost, port=50176, httpEnabled=false, httpUpgradeEnabled=false, useServlet=false, servletPath=/messaging/ActiveMQServlet, sslEnabled=false, useNio=true] failed
2026-03-03 18:36:15,147 DEBUG [org.apache.activemq.artemis.core.client.impl.ClientSessionFactoryImpl] (executor-thread-2) no connection been made, returning null 
```

this can't work, because the container is trying to connect to `localhost:50176` but the ActiveMQ Artemis is running on the host machine, in a different container. 

if instead you execute the following, then it works:
```
mvn clean verify -DskipITs=false -Dquarkus.container-image.build=false 
```