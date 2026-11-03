import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URI;
import java.net.URLConnection;

/**
 * Simple client that tries to connect to a specified server, sending a
 * specified request using NET.URL
 */
public class RequestClientNET {

    // Building the URL out of all the elementary parts
    static String fhHost = "stud.fh-wedel.de";
    static String genericHost = "example.com";
    static String hostName = genericHost;
    static String path = "/";
    static int port = 80;
    static String stringURL = "http://" + hostName + ":" + port + path;

    public void runClient() throws IOException {

        URL url = URI.create(stringURL).toURL();

        // Connection instead of Sockets
        URLConnection connection = url.openConnection();

        // The HTTP request is automatically built from the URL, no writer needed
        // Basic Input Stream wrapped with InputStreamReader to read symbols instead of bytes
        // And wrapped with BufferedReader to read lines
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));

        //Headers have to be acquired separately as theyre not acquired through the connection input stream
        System.out.println("Header: '" + connection.getHeaderFields() + "'");

        String responseLine;
        while ((responseLine = reader.readLine()) != null) {
            System.out.println("Server: '" + responseLine + "'");
        }

        reader.close();
    }
}
