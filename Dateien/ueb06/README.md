# RMI database server

This exercise implements the database service using Java Remote Method Invocation
(RMI) as specified in `Aufgaben & PDF/ueb06/ueb06.pdf`.

The project requires Java 17 or newer. Run all commands from `Dateien/ueb06`:

```bash
cd Dateien/ueb06
mvn compile
```

Start the server in one terminal:

```bash
mvn exec:java -Dexec.mainClass=DatabaseServer
```

By default, the server exports `DataBaseImpl` via `UnicastRemoteObject.exportObject`
on an ephemeral port (`0`) and writes the serialized stub to `database.stub`.
You can optionally pass a custom stub file path and export port:

```bash
mvn exec:java -Dexec.mainClass=DatabaseServer -Dexec.args="database.stub 45124"
```

Run the client in a second terminal:

```bash
mvn exec:java -Dexec.mainClass=DatabaseClient
```

Or with a custom stub file path:

```bash
mvn exec:java -Dexec.mainClass=DatabaseClient -Dexec.args="database.stub"
```

Without Maven, you can compile and run directly with `javac` and `java`:

```bash
javac -d target/classes src/java/*.java
java -cp target/classes DatabaseServer
java -cp target/classes DatabaseClient
```

## Implemented operations

- `addRecord(int index, String record)`: stores a record in the server's `ConcurrentHashMap`.
- `getRecord(int index)`: returns the record string, or `null` if not found.
- `getSize()`: returns the current number of stored records.
- `getIndex(String record)`: returns the index for a given record value, or `-1` if not found.
- `getRecordObj(int index)`: returns a serializable `DBResult` object (`key` and `value`), or `null` if not found.
