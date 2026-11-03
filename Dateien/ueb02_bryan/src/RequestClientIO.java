import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Simple client that tries to connect to a specified server, sending a
 * specified HTTP request using IO
 */
public class RequestClientIO {

     /* Server specification */
     static String fhHost = "stud.fh-wedel.de";
     static String genericHost = "example.com";
     static String ownHost = "localhost";
     static String hostName = genericHost;
     static int port = 80;

     /* Request */
     static String getRequest = "GET / HTTP/1.1\r\n";
     static String hostHeader = "Host: " + hostName + "\r\n";
     static String closeHeader = "Connection: close\r\n";
     static String emptyLine = "\r\n";
     static String requestText = getRequest + hostHeader + closeHeader + emptyLine;

     public void runClient() throws IOException {

          Socket socket = new Socket(hostName, port);

          /* Basic Output Stream wrapped with OutputStreamWriter to write symbols instead of bytes */
          PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream()));

          /* Basic Input Stream wrapped with InputStreamReader to read symbols instead of bytes */
          /* And wrapped with BufferedReader to read lines */
          BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

          // Writing request and flushing so that the message isnt stuck in a buffer
          writer.print(requestText);
          writer.flush();

          // Reading the response line for line until the connection is closed (null)
          String responseLine;
          while ((responseLine = reader.readLine()) != null) {
               System.out.println("Server: '" + responseLine + "'");
          }

          writer.close();
          reader.close();
          socket.close();
     }
}
