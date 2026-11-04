package DatabaseClasses;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import DatabaseProto.DatabaseReply;
import DatabaseProto.DatabaseRequest;
import DatabaseProto.Operation;
import DatabaseProto.PutParameters;

public class DatabaseRequestHandler implements Runnable {

    InputStream in;
    OutputStream out;
    Socket socket;
    Database db;

    public DatabaseRequestHandler(Socket socket, Database db) throws IOException {

        in = socket.getInputStream();
        out = socket.getOutputStream();
        this.socket = socket;
        this.db = db;
    }

    public void closeConnection() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    public void run() {
        DatabaseRequest dbRequest;
        try {
            dbRequest = DatabaseRequest.parseDelimitedFrom(in);
            while (dbRequest != null) {

                Operation opType = dbRequest.getOperation();

                String responseText = "";
                DatabaseReply.Builder responseBuilder = DatabaseReply.newBuilder();

                switch (opType) {
                    case UNDEFINED:
                        responseText = "Undefined Operation Request";
                        break;

                    case GET:

                        if (!dbRequest.hasGetParameter()) {
                            responseText = "GET request unsuccessful: No Key specified";
                        } else {

                            String result = db.getRecord(dbRequest.getGetParameter());
                            if (result == null) {
                                responseText = "GET request unsuccessful: No value for given key";
                            } else {
                                responseText = "GET request successful";
                                responseBuilder.setGetResponseValue(result);
                            }
                        }
                        break;

                    case PUT:

                        if (!dbRequest.hasPutParameters()) {
                            responseText = "PUT request unsuccessful: No Parameters specified";
                        } else {

                            PutParameters params = dbRequest.getPutParameters();
                            db.addRecord(params.getKey(), params.getValue());
                            responseText = "PUT request successful";
                        }
                        break;

                    case SIZE:

                        responseText = "SIZE request successful";
                        responseBuilder.setSizeResponseValue(db.getSize());
                        break;

                    default:
                        responseText = "Undefined Operation Request";
                        break;
                }

                responseBuilder.setOperation(opType);
                responseBuilder.setResponseText(responseText);

                (responseBuilder.build()).writeDelimitedTo(out);
                out.flush();

                dbRequest = DatabaseRequest.parseDelimitedFrom(in);
            }
            closeConnection();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
