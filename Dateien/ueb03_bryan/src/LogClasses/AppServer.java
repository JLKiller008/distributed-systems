package LogClasses;

/**
 * Launches the server
 */
public class AppServer {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Server");
        OwnServer server = new OwnServer();
        server.startServer();
    }

}
