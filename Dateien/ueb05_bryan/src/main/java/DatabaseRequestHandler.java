import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.io.StringReader;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public class DatabaseRequestHandler implements Runnable {

    BufferedReader reader;
    PrintWriter writer;
    Socket socket;
    Database db;

    public DatabaseRequestHandler(Socket socket, Database db) throws IOException {

        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        this.socket = socket;
        this.db = db;
    }

    public void closeConnection() throws IOException {
        reader.close();
        writer.close();
        socket.close();
    }

    public void sendError(int errorCode, String errorMsg, String id) {
        String replyString = "{\"jsonrpc\": \"2.0\", \"error\": {\"code\": "
                + errorCode
                + ", \"message\": \"" + errorMsg + "\"}, \"id\": " + id + "}";

        writer.println(replyString);
        writer.flush();
    }

    public void sendResponseString(String result, String id) {
        String replyString = "{\"jsonrpc\": \"2.0\", \"result\": \"" + result + "\", \"id\": " + id + "}";

        writer.println(replyString);
        writer.flush();
    }

    public void sendResponseInt(int result, String id) {
        String replyString = "{\"jsonrpc\": \"2.0\", \"result\": " + result + ", \"id\": " + id + "}";

        writer.println(replyString);
        writer.flush();
    }

    public void sendResponseEmpty(String id) {
        String replyString = "{\"jsonrpc\": \"2.0\", \"result\": " + null + ", \"id\": " + id + "}";

        writer.println(replyString);
        writer.flush();
    }

    public void getRequest(JsonObject requestObj, String id, boolean shouldReply) {

        if (requestObj.containsKey("params")) {
            JsonArray params = requestObj.getJsonArray("params");
            if (params.size() == 1) {
                int key = params.getInt(0);
                String entry = db.getRecord(key);
                if (shouldReply) {
                    if (entry != null) {
                        sendResponseString(entry, id);
                    } else {
                        sendResponseEmpty(id);
                    }
                }

            } else if (shouldReply) {
                sendError(-32602, "Error: Invalid number of Parameters", id);
            }

        } else if (shouldReply) {
            sendError(-32600, "Error: Parameter field missing", id);
        }
    }

    public void addRequest(JsonObject requestObj, String id, boolean shouldReply) {

        if (requestObj.containsKey("params")) {
            JsonArray params = requestObj.getJsonArray("params");
            if (params.size() == 2) {
                int key = params.getInt(0);
                String value = params.getString(1);
                db.addRecord(key, value);

                if (shouldReply) {
                    sendResponseEmpty(id);
                }

            } else if (shouldReply) {
                sendError(-32602, "Error: Invalid number of Parameters", id);
            }

        } else if (shouldReply) {
            sendError(-32600, "Error: Parameter field missing", id);
        }
    }

    public void sizeRequest(JsonObject requestObj, String id, boolean shouldReply) {

        if (shouldReply) {
            sendResponseInt(db.getSize(), id);
        }
    }

    public void executeRequest(JsonObject requestObj) {

        if (requestObj == null) {
            sendError(-32700, "Error: Invalid Json", null);
            return;
        }

        boolean shouldReply = requestObj.containsKey("id");
        String id;

        if (shouldReply) {
            id = requestObj.get("id").toString();
        } else {
            id = null;
        }

        if (requestObj.containsKey("method")) {
            String method = requestObj.getString("method");
            switch (method) {
                case "getRecord":
                    getRequest(requestObj, id, shouldReply);
                    break;
                case "addRecord":
                    addRequest(requestObj, id, shouldReply);
                    break;
                case "getSize":
                    sizeRequest(requestObj, id, shouldReply);
                    break;
                default:
                    if (shouldReply) {
                        sendError(-32601, "Error: Method not found", id);
                    }
                    break;
            }
        } else if (shouldReply) {
            sendError(-32600, "Error: No Method field", id);
        }
    }

    public void run() {

        String request = null;
        try {
            while ((request = reader.readLine()) != null) {
                JsonReader jsonReader = Json.createReader(new StringReader(request));
                JsonObject obj = jsonReader.readObject();
                jsonReader.close();

                executeRequest(obj);
            }

            closeConnection();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
