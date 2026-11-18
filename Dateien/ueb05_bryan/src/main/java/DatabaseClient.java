import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class DatabaseClient {

    /* Server specification */
    static String ownHost = "localhost";
    static String hostName = ownHost;
    static int port = 4444;
    static int idCounter = 0;

    public static void main(String[] args) throws IOException {
        new DatabaseClient().runClient();
    }

    public String buildGetRequest(int key) {
        idCounter++;
        return "{\"jsonrpc\": \"2.0\", \"method\": \"getRecord\", \"params\": "
                + "[" + key + "]"
                + ", \"id\": " + idCounter + "}";
    }

    public String buildPutRequest(int key, String value) {
        idCounter++;
        return "{\"jsonrpc\": \"2.0\", \"method\": \"addRecord\", \"params\": "
                + "[" + key + ", \"" + value + "\"]"
                + ", \"id\": " + idCounter + "}";
    }

    public String buildSizeRequest() {
        idCounter++;
        return "{\"jsonrpc\": \"2.0\", \"method\": \"getSize\", \"id\": " + idCounter + "}";
    }

    public String[] assignmentRequests() {
        String[] requests = new String[9];

        requests[0] = buildPutRequest(4101, "Appen");
        requests[1] = buildPutRequest(4102, "Ahrensburg");
        requests[2] = buildPutRequest(4103, "Wedel");
        requests[3] = buildPutRequest(4104, "Aumühle");
        requests[4] = buildPutRequest(4105, "Seevetal");
        requests[5] = buildPutRequest(4106, "Quickborn");

        requests[6] = buildGetRequest(4103);
        requests[7] = buildGetRequest(4107);

        requests[8] = buildSizeRequest();

        return requests;
    }

    public void runClient() throws IOException {

        Socket socket = new Socket(hostName, port);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        // Assignment specified requests
        String[] requests = assignmentRequests();

        for (String request : requests) {
            writer.println(request);
            writer.flush();

            String reply = reader.readLine();
            if (reply != null) {
                System.out.println(reply);
            }
        }

        writer.close();
        reader.close();
        socket.close();
    }

}
