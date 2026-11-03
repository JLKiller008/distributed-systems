package DatabaseClasses;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DatabaseServer {

    public static void main(String[] args) throws IOException {
        new DatabaseServer().startServer();
    }

    public void startServer() throws IOException {

        int port = 4444;
        Database db = new Database();

        // Starting the ServerSocket which accepts and organizes connections
        ServerSocket server_socket = new ServerSocket(port);
        System.out.println("Database server running on port "
                + server_socket.getLocalPort());

        // Continuously waiting and accepting new connection requests, starting a new
        // Thread for each
        while (true) {
            Socket socket = server_socket.accept();
            System.out.println("New connection accepted "
                    + socket.getInetAddress() + ":" + socket.getPort());
            DatabaseRequestHandler request = new DatabaseRequestHandler(socket, db);
            // Create a new thread to process the request.
            Thread thread = new Thread(request);
            // Start the thread.
            thread.start();
        }
    }

}
