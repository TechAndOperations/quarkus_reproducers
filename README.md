# xml-component-test

When we try to unmarchall a xml document with a namespace from a file in a QuarkusTest and in a QuarkusComponentTest (same test), we observe a different result between quarkus 3.28.5 (last working) and quarkus 3.29.4+

> Java version: 21

How to run the reproducer :

### build 3.28.5

```bash
mvn clean install
```

The tests should pass.

### build 3.29.4

Edit the pom.xml and change the property `quarkus.platform.version` to `3.29.4`, rebuild and run the tests again:

```bash
mvn clean install
```

The quarkus component test should fail proving the regression

### build 3.32.3

Edit the pom.xml and change the property `quarkus.platform.version` to `3.32.3`, rebuild and run the tests again:

```bash
mvn clean install
```

The quarkus component test should fail again proving the regression is still present in 3.32.3