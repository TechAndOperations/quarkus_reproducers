Unit tests succeed
```
$ mvn clean package
...
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.195 s -- in org.acme.GreetingResourceTest
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

IT succeed also in JVM mode
```
$ mvn clean verify -DskipITs=false 
...
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.604 s -- in org.acme.GreetingResourceIT
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

But this fails in native:
```
$ mvn clean verify -Pnative
...
[ERROR]   GreetingResourceIT>GreetingResourceTest.testProp:27 1 expectation failed.
Response body doesn't match expectation.
Expected: is "bar"
  Actual: xx 
```