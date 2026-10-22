package Dateien.ueb02;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class Server {
    private final static int PORT = 45123;

    public static void main(String[] args) {
        try (ServerSocket welcomeSocket = new ServerSocket(PORT)) {
            System.out.println(String.format("HTTP-Server running on port: %d", welcomeSocket.getLocalPort()));
            
            while (!Thread.currentThread().isInterrupted()) {
                String ip = null;
                int port = -1;
                try {
                    Socket socket = welcomeSocket.accept();
                    SocketAddress socketAddress = socket.getRemoteSocketAddress();
                    ip = socketAddress.toString().split(":")[0];
                    port = Integer.parseInt(socketAddress.toString().split(":")[1]);

                    System.out.println(String.format("Connection established with client with IP: %s and Port: %d", ip, port));

                    HTTPRequestHandler requestHandler = new HTTPRequestHandler(socket);

                    Thread requestHandlerThread = new Thread(requestHandler);
                    requestHandlerThread.start();
                } catch (Exception e) {
                    System.out.println(String.format("Error: Connection to client with IP: %s and Port: %d has encountered an error and is now closed", ip, port));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("ServerSocket crashed. See reason: " + e.getMessage(), e);
        } finally {
            System.out.println("HTTP-Server stopped running");
        }
    }
}
