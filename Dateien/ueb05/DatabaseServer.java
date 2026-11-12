import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DatabaseServer {
    private static final int DEFAULT_PORT = 45124;
    private final Map<Integer, String> records = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

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
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("port must be a number", exception);
        }
    }

    private void run(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("JSON-RPC database server listening on port %d%n",
                serverSocket.getLocalPort());
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                Thread handler = new Thread(() -> handleClient(socket),
                    "database-client-" + socket.getRemoteSocketAddress());
                handler.start();
            }
        } catch (IOException exception) {
            throw new RuntimeException("database server stopped unexpectedly", exception);
        }
    }

    private void handleClient(Socket socket) {
        try (Socket client = socket;
             BufferedReader input = new BufferedReader(new InputStreamReader(
                 client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter output = new PrintWriter(client.getOutputStream(), true,
                 StandardCharsets.UTF_8)) {
            String line;
            while ((line = input.readLine()) != null) {
                String response = process(line);
                System.out.println("Server -> client: " + response);
                output.println(response);
            }
        } catch (IOException exception) {
            System.err.printf("Could not process client %s: %s%n",
                socket.getRemoteSocketAddress(), exception.getMessage());
        }
    }

    private String process(String line) {
        JsonElement id = null;
        try {
            JsonElement parsed = JsonParser.parseString(line);
            if (!parsed.isJsonObject()) {
                return errorResponse(null, -32600, "request must be a JSON object");
            }
            JsonObject request = parsed.getAsJsonObject();
            id = request.get("id");
            if (!"2.0".equals(request.has("jsonrpc") ? request.get("jsonrpc").getAsString() : null)
                    || !request.has("method") || !request.get("method").isJsonPrimitive()
                    || !request.get("method").getAsJsonPrimitive().isString()) {
                return errorResponse(id, -32600, "invalid JSON-RPC request");
            }
            JsonElement params = request.get("params");
            if (params != null && !params.isJsonObject()) {
                return errorResponse(id, -32602, "params must be an object");
            }
            return execute(request.get("method").getAsString(),
                params == null ? new JsonObject() : params.getAsJsonObject(), id);
        } catch (JsonParseException | IllegalStateException | UnsupportedOperationException exception) {
            return errorResponse(id, -32700, "parse error: " + exception.getMessage());
        }
    }

    private String execute(String method, JsonObject params, JsonElement id) {
        return switch (method) {
            case "getRecord" -> getRecord(params, id);
            case "addRecord" -> addRecord(params, id);
            case "getSize" -> response(id, records.size());
            default -> errorResponse(id, -32601, "method not found");
        };
    }

    private String getRecord(JsonObject params, JsonElement id) {
        Integer key = integerParameter(params, "key");
        if (key == null) {
            return errorResponse(id, -32602, "key must be an integer");
        }
        String record = records.get(key);
        if (record == null) {
            return errorResponse(id, -32004, "no record found for key " + key);
        }
        return response(id, record);
    }

    private String addRecord(JsonObject params, JsonElement id) {
        Integer key = integerParameter(params, "key");
        JsonElement record = params.get("record");
        if (key == null || record == null || !record.isJsonPrimitive()
                || !record.getAsJsonPrimitive().isString()) {
            return errorResponse(id, -32602, "key must be an integer and record a string");
        }
        records.put(key, record.getAsString());
        return response(id, null);
    }

    private static Integer integerParameter(JsonObject params, String name) {
        JsonElement value = params.get(name);
        if (value == null || !value.isJsonPrimitive()
                || !value.getAsJsonPrimitive().isNumber()) {
            return null;
        }
        try {
            long number = value.getAsLong();
            if (number < Integer.MIN_VALUE || number > Integer.MAX_VALUE
                    || !value.getAsString().matches("-?(0|[1-9][0-9]*)")) {
                return null;
            }
            return (int) number;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String response(JsonElement id, Object result) {
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("result", gson.toJsonTree(result));
        response.add("id", id);
        return gson.toJson(response);
    }

    private String errorResponse(JsonElement id, int code, String message) {
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("error", error);
        response.add("id", id);
        return gson.toJson(response);
    }
}
