/**
 * Launches 2 Clients (1 each for IO and NET methods)
 */
public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Programm Started");

        System.out.println("Connecting via IO");
        RequestClientIO rClient = new RequestClientIO();
        rClient.runClient();

        System.out.println("Connecting via NET");
        RequestClientNET rClientNET = new RequestClientNET();
        rClientNET.runClient();

        System.out.println("Programm Ended");
    }
}

/**
 * 
 * Proxy Question:
 * Für NET: openConnection(Proxy proxy)
 * 
 * Für IO:
 * 1) Socket(Proxy proxy) (SOCKS Proxy)
 * 2) Request with Absolute form request like URL (HTTP Proxy)
 * 
 */