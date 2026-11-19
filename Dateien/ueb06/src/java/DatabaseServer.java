import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CountDownLatch;

public final class DatabaseServer {
    private static final String DEFAULT_STUB_FILE = "database.stub";
    private static final int DEFAULT_PORT = 0;

    public static void main(String[] args) throws IOException, InterruptedException {
        String stubFile = args.length > 0 ? args[0] : DEFAULT_STUB_FILE;
        int port = args.length > 1 ? parsePort(args[1]) : DEFAULT_PORT;

        DataBaseImpl database = new DataBaseImpl();
        Remote stub = UnicastRemoteObject.exportObject(database, port);
        writeStubToFile(stubFile, stub);
        System.out.printf("RMI database server exported (stub written to %s)%n", stubFile);

        CountDownLatch shutdownLatch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                UnicastRemoteObject.unexportObject(database, true);
            } catch (NoSuchObjectException ignored) {
                // Already unexported.
            } finally {
                shutdownLatch.countDown();
            }
        }, "rmi-server-shutdown"));

        shutdownLatch.await();
    }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value);
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("port must be between 0 and 65535");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("port must be a number", exception);
        }
    }

    private static void writeStubToFile(String fileName, Remote stub)
            throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(stub);
        out.close();
    }
}
