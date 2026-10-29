import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseServer {
    private static final int DEFAULT_PORT = 45124;
    private final Map<Integer, String> records = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int port = args.length > 0 ? parsePort(args[0]) : DEFAULT_PORT;
        new DatabaseServer().run(port);
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

    private void run(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("Database server listening on port %d%n", serverSocket.getLocalPort());
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                Thread handler = new Thread(
                    () -> handleClient(socket),
                    "database-client-" + socket.getRemoteSocketAddress());
                handler.start();
            }
        } catch (IOException e) {
            throw new RuntimeException("Database server stopped unexpectedly", e);
        }
    }

    private void handleClient(Socket socket) {
        try (Socket client = socket;
             InputStream input = client.getInputStream();
             OutputStream output = client.getOutputStream()) {
            RPCRequest request;
            while ((request = RPCRequest.parseDelimitedFrom(input)) != null) {
                execute(request).writeDelimitedTo(output);
                output.flush();
            }
        } catch (IOException | RuntimeException e) {
            System.err.printf(
                "Could not process client %s: %s%n",
                socket.getRemoteSocketAddress(),
                e.getMessage());
        }
    }

    private RPCResponse execute(RPCRequest request) {
        return switch (request.getOperation()) {
            case GET_RECORD -> getRecord(request);
            case ADD_RECORD -> addRecord(request);
            case GET_SIZE -> RPCResponse.newBuilder()
                .setSuccess(true)
                .setSize(records.size())
                .build();
            case OPERATION_UNSPECIFIED, UNRECOGNIZED ->
                failure("request operation is missing or unknown");
        };
    }

    private RPCResponse getRecord(RPCRequest request) {
        if (!request.hasGetRecord()) {
            return failure("getRecord request is missing");
        }
        String record = records.get(request.getGetRecord().getIndex());
        if (record == null) {
            return failure("no record found for index " + request.getGetRecord().getIndex());
        }
        return RPCResponse.newBuilder()
            .setSuccess(true)
            .setRecord(record)
            .build();
    }

    private RPCResponse addRecord(RPCRequest request) {
        if (!request.hasAddRecord()) {
            return failure("addRecord request is missing");
        }
        AddRecordRequest addRequest = request.getAddRecord();
        records.put(addRequest.getIndex(), addRequest.getRecord());
        return RPCResponse.newBuilder()
            .setSuccess(true)
            .build();
    }

    private static RPCResponse failure(String error) {
        return RPCResponse.newBuilder()
            .setSuccess(false)
            .setError(error)
            .build();
    }
}
