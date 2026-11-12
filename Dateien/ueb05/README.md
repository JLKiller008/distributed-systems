# JSON-RPC database server

This exercise implements the database service from exercise 3 with JSON-RPC
2.0 over a direct TCP socket. Messages are UTF-8 JSON objects, one request or
response per line, so several calls can use one connection.

The project requires Java 17 or newer and uses Google's Gson library through
Maven. Run all commands from this directory:

```bash
cd Dateien/ueb05
mvn compile
```

Start the server in one terminal:

```bash
mvn exec:java -Dexec.mainClass=DatabaseServer
```

The server listens on port `45124` by default. A different port can be
provided as an argument:

```bash
mvn exec:java -Dexec.mainClass=DatabaseServer -Dexec.args="45125"
```

Run the client in a second terminal:

```bash
mvn exec:java -Dexec.mainClass=DatabaseClient
```

The client defaults to `localhost:45124`. A different host and port can be
provided as arguments:

```bash
mvn exec:java -Dexec.mainClass=DatabaseClient -Dexec.args="localhost 45125"
```

To remove compiled classes and Maven build output, run:

```bash
mvn clean
```

JSON parsing and serialization are implemented with Gson, declared as a
dependency in `pom.xml`.

The client sends requests of the following form:

```json
{"jsonrpc":"2.0","method":"addRecord","params":{"key":4101,"record":"Appen"},"id":1}
{"jsonrpc":"2.0","method":"getRecord","params":{"key":4103},"id":2}
{"jsonrpc":"2.0","method":"getSize","params":{},"id":3}
```

Successful responses contain `result`, for example
`{"jsonrpc":"2.0","result":"Wedel","id":2}`. A missing record is returned as
an application error with code `-32004`; malformed requests use the standard
JSON-RPC error codes. After one client run, `getSize` reports 6.

Direct JSON-RPC over TCP has no HTTP headers, status line, or HTTP connection
semantics. It is therefore lightweight and requires an explicit message
framing rule (this implementation uses newline-delimited JSON). JSON-RPC over
HTTP carries the JSON-RPC message in an HTTP request/response body and gains
standard HTTP features such as status codes, headers, proxies, and browser or
web-server tooling, at the cost of additional framing overhead.
