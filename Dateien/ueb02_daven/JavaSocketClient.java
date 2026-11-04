import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Aufgabe 1a (+1c): HTTP-Client mit rohen Sockets.
 * Aufruf: java JavaSocketClient [host] [port] [pfad] [proxyHost proxyPort]
 * Mit Proxy: Verbindung geht zum Proxy, die Request-Zeile enthaelt die absolute URL.
 * Beachten: Port 80 ist für Http Requests
 */
public class JavaSocketClient {
    public JavaSocketClient(String host, int port, String path, String proxyHost, int proxyPort) throws Exception {
        boolean viaProxy = proxyHost != null;
        String connectHost = viaProxy ? proxyHost : host;
        int connectPort = viaProxy ? proxyPort : port;
        String requestTarget = viaProxy ? "http://" + host + ":" + port + path : path;

        try (Socket socket = new Socket(connectHost, connectPort)) {
            PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.ISO_8859_1));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            writer.print("GET " + requestTarget + " HTTP/1.1\r\n");
            writer.print("Host: " + host + (port == 80 ? "" : ":" + port) + "\r\n");
            writer.print("Connection: close\r\n");
            writer.print("\r\n"); // leere Zeile = Ende des Headers
            writer.flush();

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "fh-wedel.de";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 80;
        String path = args.length > 2 ? args[2] : "/index.html";
        String proxyHost = args.length > 4 ? args[3] : null;
        int proxyPort = args.length > 4 ? Integer.parseInt(args[4]) : 0;
        new JavaSocketClient(host, port, path, proxyHost, proxyPort);
    }
}
