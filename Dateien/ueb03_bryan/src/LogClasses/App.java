package LogClasses;

/**
 * Launches the log client
 */
public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Programm Started");

        System.out.println("Connecting via IO");
        RequestClientIO rClient = new RequestClientIO();
        rClient.runClient();

        System.out.println("Programm Ended");
    }
}
