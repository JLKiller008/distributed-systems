# gRPC database server

This exercise implements the database service from exercise 3 with gRPC and
Protocol Buffers. The service interface is defined in
[`src/main/proto/database.proto`](src/main/proto/database.proto), and Maven
generates the protobuf messages and gRPC stubs during compilation.

Build the project:

```bash
mvn compile
```

Run the server and client in separate terminals:

```bash
mvn exec:java -Dexec.mainClass=DatabaseServer
mvn exec:java -Dexec.mainClass=DatabaseClient
```


To clean the project, run:

```bash
mvn clean
```

The client adds the six required records, reads indices 4103 and 4107, and
queries the database size. A missing record is reported as a gRPC `NOT_FOUND`
error; the expected database size after one client run is 6.
