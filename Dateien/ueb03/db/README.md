# Protocol Buffers database server

This solution uses Protocol Buffers messages over a direct TCP socket. Each
request and response is length-delimited with protobuf's
`parseDelimitedFrom`/`writeDelimitedTo` methods, so several RPC calls can use
one connection.

Generate and compile the Java sources from this directory:

```bash
protoc --java_out=. Database.proto
javac -cp protobuf-java-4.36.2.jar *.java
```

Run the server and client in separate terminals:

```bash
java -cp .:protobuf-java-4.36.2.jar DatabaseServer 45124
java -cp .:protobuf-java-4.36.2.jar DatabaseClient localhost 45124
```

The client adds the six required records, reads indices 4103 and 4107, and
queries the database size.

Direct TCP sends protobuf bytes with a small length prefix and no HTTP
headers, making it a simple, low-overhead protocol for this exercise. HTTP
would carry the protobuf bytes as an HTTP request/response body and adds
standard HTTP framing, headers, status codes, and proxy/tooling compatibility.
