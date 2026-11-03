package DatabaseClasses;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import DatabaseProto.DatabaseReply;
import DatabaseProto.DatabaseRequest;
import DatabaseProto.Operation;
import DatabaseProto.PutParameters;

public class DatabaseClient {

    /* Server specification */
    static String ownHost = "localhost";
    static String hostName = ownHost;
    static int port = 4444;

    public static void main(String[] args) throws IOException {
        new DatabaseClient().runClient();
    }

    public DatabaseRequest buildPutRequest(int key, String value) {
        return DatabaseRequest.newBuilder()
                .setOperation(Operation.PUT)
                .setPutParameters(
                        PutParameters.newBuilder().setKey(key).setValue(value).build())
                .build();
    }

    public DatabaseRequest buildGetRequest(int key) {
        return DatabaseRequest.newBuilder()
                .setOperation(Operation.GET)
                .setGetParameter(key)
                .build();
    }

    public DatabaseRequest buildSizeRequest() {
        return DatabaseRequest.newBuilder()
                .setOperation(Operation.SIZE)
                .build();
    }

    public DatabaseRequest[] assignmentRequests() {
        DatabaseRequest[] requests = new DatabaseRequest[9];

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

    public String replyToString(DatabaseReply reply) {
        String start = "Database Reply:\n";
        String replyText = reply.getResponseText() + "\n";
        String additionalText = "";
        switch (reply.getOperation()) {
            case Operation.UNDEFINED:
                additionalText = "Called Operation: Undefined\n";
                break;

            case Operation.GET:
                if (reply.hasGetResponseValue()) {
                    String returnValue = reply.getGetResponseValue();
                    additionalText = "Called Operation: GET\nReturn Value: " + returnValue + "\n";
                } else {
                    additionalText = "Called Operation: GET\nReturn Value: No Value Given\n";
                }
                break;

            case Operation.PUT:
                additionalText = "Called Operation: PUT\n";
                break;

            case Operation.SIZE:
                if (reply.hasSizeResponseValue()) {
                    int returnSize = reply.getSizeResponseValue();
                    additionalText = "Called Operation: SIZE\nReturn Value: " + returnSize + "\n";
                } else {
                    additionalText = "Called Operation: SIZE\nReturn Value: No Value Given\n";
                }
                break;
            default:
                break;
        }

        return start + replyText + additionalText + "End of Response\n\n";

    }

    public void runClient() throws IOException {

        Socket socket = new Socket(hostName, port);

        OutputStream out = socket.getOutputStream();

        InputStream in = socket.getInputStream();

        // Assignment specified requests
        DatabaseRequest[] requests = assignmentRequests();

        for (DatabaseRequest request : requests) {
            request.writeDelimitedTo(out);
            out.flush();

            DatabaseReply reply = DatabaseReply.parseDelimitedFrom(in);
            if (reply != null) {
                System.out.println(replyToString(reply));
            }
        }

        out.close();
        in.close();
        socket.close();
    }

}
