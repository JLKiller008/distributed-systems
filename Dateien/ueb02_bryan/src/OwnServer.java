import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server that accepts connections from clients and creates a HTTPRequestHandler
 * Thread for them
 */
public class OwnServer {

    public void startServer() throws IOException {

        int port = 4444;

        //Starting the ServerSocket which accepts and organizes connections
        ServerSocket server_socket = new ServerSocket(port);
        System.out.println("httpServer running on port "
                + server_socket.getLocalPort());

        //Continuously waiting and accepting new connection requests, starting a new Thread for each
        while (true) {
            Socket socket = server_socket.accept();
            System.out.println("New connection accepted "
                    + socket.getInetAddress() + ":" + socket.getPort());
            HTTPRequestHandler request = new HTTPRequestHandler(socket);
            // Create a new thread to process the request.
            Thread thread = new Thread(request);
            // Start the thread.
            thread.start();
        }
    }
}
