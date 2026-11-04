import java.net.ServerSocket;
import java.net.Socket;

/**
 * Aufgabe 2a: einfacher HTTP-Server. Pro eingehender Verbindung wird ein neuer Thread gestartet.
 * Aufruf: java JavaServer [port] [wurzelverzeichnis]
 */
public class JavaServer {
    private final int port;
    private final String docRoot;

    public JavaServer(int port, String docRoot) {
        this.port = port;
        this.docRoot = docRoot;
    }

    public void start() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("httpServer running on port " + serverSocket.getLocalPort()
                    + ", serving '" + docRoot + "'");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
                new Thread(new HttpRequestHandler(socket, docRoot)).start();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 4444;
        //Im Docroot nur eine generierte html Datei drinne zum auslese bei anfrage. Andere Dateiarten nicht unterstützt
        String docRoot = args.length > 1 ? args[1] : "www";
        new JavaServer(port, docRoot).start();
    }
}
