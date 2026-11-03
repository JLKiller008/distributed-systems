package LogClasses;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import LogProto.*;

import com.google.protobuf.Timestamp;

/**
 * Simple client that tries to connect to a specified server, sending a
 * specified proto Object
 */
public class RequestClientIO {

     /* Server specification */
     static String ownHost = "localhost";
     static String hostName = ownHost;
     static int port = 4444;

     public LogMessage buildLogMessage() {

          long millis = System.currentTimeMillis();
          Timestamp timestamp = Timestamp.newBuilder().setSeconds(millis / 1000)
                    .setNanos((int) ((millis % 1000) * 1000000)).build();

          return LogMessage.newBuilder()
                    .setCreatedAt(timestamp)
                    .setAuthorDetails("Bryan in Moorrege")
                    .setSeverity(SeverityLevel.FATAL)
                    .setBody("Hallo!")
                    .build();

     }

     public void runClient() throws IOException {

          Socket socket = new Socket(hostName, port);

          OutputStream out = socket.getOutputStream();

          LogMessage logMessage = buildLogMessage();

          logMessage.writeDelimitedTo(out);
          out.flush();

          out.close();
          socket.close();
     }
}
