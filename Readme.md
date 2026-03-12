# multi-ext-app

This is a sample application to test the performance of the AOT cache feature.

this is based on an application with multiple extensions we have in the company, and we run it with 2 profiles (simulated by dev and basedev here).

as I am running on windows, it was easier to use docker compose and rely on `mvn verify` to figure out the correct launch options.

in our real application, each of our 9 extensions depend on quarkus extensions. 
to simplify the test, I created 9 empty extensions that only depend on `quarkus-arc` and added them to the application, plus I added all quarkus extensions directly into the app, since I believe that the way we depend on those core extensions does not change the performance of the app startup with or without AOT.
granted, this looks a bit awkward here.  

To reproduce execute the following commands:
```
docker compose -f app\compose-devservices.yaml up
mvn verify -DskipITs=false -Dquarkus.compose.devservices.reuse-project-for-tests=true
```

from the logs capture the command:
``` 
Executing "java -Dquarkus.http.port=8081 ... -jar ...\app\target\quarkus-app\quarkus-run.jar"
```

in this command replace `-Dquarkus.profile=prod` by `-Dquarkus.profile=dev,basedev`  
and add options `-XX:AOTCacheOutput=app.aot -Xlog:aot`
execute the resulting command. you should see:
```
2026-03-12 09:58:38,915 INFO  [io.quarkus] (main) multi-ext-app 1.0.0-SNAPSHOT on JVM (powered by Quarkus 3.32.3) started in 2.946s. Listening on: http://0.0.0.0:8081
2026-03-12 09:58:38,916 INFO  [io.quarkus] (main) Profiles basedev,dev activated.
```

call `curl localhost:8081/hello/apiKey`
you should see: `Optional[12345]`

stop the jvm. the aot file should be created.
```
AOTCache creation is complete: app.aot 103350272 bytes
Removed temporary AOT configuration file app.aot.config
```

now replace the aot options with `-XX:AOTCache=app.aot -Xlog:aot` and restart.
You should see:
```
[0.112s][info][aot] Using AOT-linked classes: true (static archive: has aot-linked classes)
...
2026-03-12 10:04:29,797 INFO  [io.quarkus] (main) multi-ext-app 1.0.0-SNAPSHOT on JVM (powered by Quarkus 3.32.3) started in 1.761s. Listening on: http://0.0.0.0:8081
```

the startup time is much better than the 2.946s without AOT. but still pretty high considering the expectations (according to discussions we should be able to get close to 800 ms).

if I put a logging breakpoint on `YamlConfigSourceLoader` in `loadConfigSource(URL, oridinal)`
```
==>jar:file:///.../multi-ext-app/app/target/quarkus-app/app/multi-ext-app-1.0.0-SNAPSHOT.jar!/application.yaml
==>jar:file:///.../multi-ext-app/app/target/quarkus-app/app/multi-ext-app-1.0.0-SNAPSHOT.jar!/application.yaml
==>jar:file:/.../multi-ext-app/app/target/quarkus-app/app/multi-ext-app-1.0.0-SNAPSHOT.jar!/application-dev.yaml
==>jar:file:/.../multi-ext-app/app/target/quarkus-app/app/multi-ext-app-1.0.0-SNAPSHOT.jar!/application-dev.yml
==>jar:file:/.../multi-ext-app/app/target/quarkus-app/app/multi-ext-app-1.0.0-SNAPSHOT.jar!/application-basedev.yaml
==>jar:file:/.../multi-ext-app/app/target/quarkus-app/app/multi-ext-app-1.0.0-SNAPSHOT.jar!/application-basedev.yml
==>jar:file:/.../multi-ext-app/app/target/quarkus-app/app/multi-ext-app-1.0.0-SNAPSHOT.jar!/application-dev.yaml
==>jar:file:/.../multi-ext-app/app/target/quarkus-app/app/multi-ext-app-1.0.0-SNAPSHOT.jar!/application-dev.yml
...
```

this explains that `loadConfigSource` appears multiple times in the flamegraph.

asynch-profiler is not supported on windows. I did some profiling with JProfiler, but the agent is not compatible with the AOT cache, so the only thing I could is to profile without AOT, or use the AOT cache without profiling.
yet the displayed startup time when running AOT should be enough to demonstrate that there might room for improvement.

