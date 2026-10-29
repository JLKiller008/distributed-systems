# Quick Guide to Proto

This exercise requires installation of the [Protocol Buffers Compiler](https://grpc.io/docs/protoc-installation/). The compiler is used to generate Java classes from the `.proto` files.

To generate the Java classes, run the following command in the terminal:

```bash
protoc --java_out=. src/main/proto/*.proto
```

To compile the generated Java classes, a suitable java runtime is needed. Alternatively, a maven extension can be used. To get the suitable jre, use the follwing command:

```bash
curl -L -o protobuf-java-4.36.2.jar \
  https://repo1.maven.org/maven2/com/google/protobuf/protobuf-java/4.36.2/protobuf-java-4.36.2.jar
```

To now both compile + run the generated classes, use the following command:

```bash
javac -cp protobuf-java-4.36.2.jar \
  LogMessageOuterClass.java ProtoServer.java ProtoClient.java

java -cp .:protobuf-java-4.36.2.jar \
  ProtoServer 45213 logs/application.log
```

To run the client, now run:

```bash
java -cp .:protobuf-java-4.36.2.jar ProtoClient localhost 45213 DEBUG client "Debug message"
java -cp .:protobuf-java-4.36.2.jar ProtoClient localhost 45213 ERROR server "Something failed"
```
