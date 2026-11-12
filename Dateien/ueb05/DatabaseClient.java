import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DatabaseClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 45124;
    private static final int CONNECT_TIMEOUT_MILLIS = 5_000;
    private static final Gson GSON = new Gson();

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? parsePort(args[1]) : DEFAULT_PORT;
        try {
            run(host, port);
        } catch (IOException exception) {
            System.err.println("Could not communicate with database server: "
                + exception.getMessage());
        }
    }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("port must be between 1 and 65535");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("port must be a number", exception);
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
            try (BufferedReader input = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter output = new PrintWriter(socket.getOutputStream(), true,
                     StandardCharsets.UTF_8)) {
                int id = 1;
                for (Map.Entry<Integer, String> entry : records.entrySet()) {
                    Map<String, Object> params = new LinkedHashMap<>();
                    params.put("key", entry.getKey());
                    params.put("record", entry.getValue());
                    requireSuccess(call("addRecord", params, id++, input, output),
                        "addRecord(" + entry.getKey() + ")");
                }

                printRecord(4103, call("getRecord", Map.of("key", 4103),
                    id++, input, output));
                printRecord(4107, call("getRecord", Map.of("key", 4107),
                    id++, input, output));

                JsonObject size = call("getSize", Map.of(), id, input, output);
                requireSuccess(size, "getSize");
                System.out.println("getSize(): " + size.get("result").getAsInt());
            }
        }
    }

    private static JsonObject call(String method, Map<String, ?> params, int id,
            BufferedReader input, PrintWriter output) throws IOException {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("method", method);
        request.put("params", params);
        request.put("id", id);
        String json = GSON.toJson(request);
        System.out.println("Client -> server: " + json);
        output.println(json);
        String line = input.readLine();
        if (line == null) {
            throw new IOException("database server closed the connection");
        }
        JsonElement response = JsonParser.parseString(line);
        if (!response.isJsonObject()) {
            throw new IOException("database server returned an invalid response");
        }
        return response.getAsJsonObject();
    }

    private static void printRecord(int key, JsonObject response) {
        if (response.has("result")) {
            System.out.printf("getRecord(%d): %s%n", key, response.get("result").getAsString());
        } else {
            System.out.printf("getRecord(%d): %s%n", key, errorMessage(response));
        }
    }

    private static void requireSuccess(JsonObject response, String operation)
            throws IOException {
        if (response.has("error")) {
            throw new IOException(operation + " failed: " + errorMessage(response));
        }
    }

    private static String errorMessage(JsonObject response) {
        JsonElement error = response.get("error");
        if (error != null && error.isJsonObject() && error.getAsJsonObject().has("message")) {
            return error.getAsJsonObject().get("message").getAsString();
        }
        return "unknown error";
    }
}
