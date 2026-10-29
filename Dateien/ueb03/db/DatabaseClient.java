import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 45124;
    private static final int CONNECT_TIMEOUT_MILLIS = 5_000;

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? parsePort(args[1]) : DEFAULT_PORT;
        try {
            run(host, port);
        } catch (IOException e) {
            System.err.println("Could not communicate with database server: " + e.getMessage());
        }
    }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("port must be between 1 and 65535");
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("port must be a number", e);
        }
    }

    private static void run(String host, int port) throws IOException {
        Map<Integer, String> records = new LinkedHashMap<>();
        records.put(4101, "Appen");
        records.put(4102, "Ahrensburg");
        records.put(4103, "Wedel");
        records.put(4104, "Aumühle");
        records.put(4105, "Seevetal");
        records.put(4106, "Quickborn");

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            for (Map.Entry<Integer, String> entry : records.entrySet()) {
                RPCResponse response = call(
                    RPCRequest.newBuilder()
                        .setOperation(RPCRequest.Operation.ADD_RECORD)
                        .setAddRecord(AddRecordRequest.newBuilder()
                            .setIndex(entry.getKey())
                            .setRecord(entry.getValue())
                            .build())
                        .build(),
                    input,
                    output);
                requireSuccess(response, "addRecord(" + entry.getKey() + ")");
            }

            printRecord(4103, call(getRequest(4103), input, output));
            printRecord(4107, call(getRequest(4107), input, output));

            RPCResponse sizeResponse = call(
                RPCRequest.newBuilder()
                    .setOperation(RPCRequest.Operation.GET_SIZE)
                    .build(),
                input,
                output);
            requireSuccess(sizeResponse, "getSize");
            System.out.println("getSize(): " + sizeResponse.getSize());
        }
    }

    private static RPCRequest getRequest(int index) {
        return RPCRequest.newBuilder()
            .setOperation(RPCRequest.Operation.GET_RECORD)
            .setGetRecord(GetRecordRequest.newBuilder().setIndex(index).build())
            .build();
    }

    private static RPCResponse call(
            RPCRequest request, InputStream input, OutputStream output) throws IOException {
        request.writeDelimitedTo(output);
        output.flush();
        RPCResponse response = RPCResponse.parseDelimitedFrom(input);
        if (response == null) {
            throw new IOException("database server closed the connection");
        }
        return response;
    }

    private static void printRecord(int index, RPCResponse response) {
        if (response.getSuccess()) {
            System.out.printf("getRecord(%d): %s%n", index, response.getRecord());
        } else {
            System.out.printf("getRecord(%d): %s%n", index, response.getError());
        }
    }

    private static void requireSuccess(RPCResponse response, String operation) throws IOException {
        if (!response.getSuccess()) {
            throw new IOException(operation + " failed: " + response.getError());
        }
    }
}
