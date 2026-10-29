import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class ProtoServer {
    private static final int DEFAULT_PORT = 45123;
    private static final Path DEFAULT_LOG_FILE = Path.of("proto-server.log");

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        Path logFile = args.length > 1 ? Path.of(args[1]) : DEFAULT_LOG_FILE;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("Proto server listening on port %d%n", serverSocket.getLocalPort());
            System.out.println("Writing log entries to " + logFile);

            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                Thread handler = new Thread(
                    () -> receiveMessages(socket, logFile),
                    "proto-client-" + socket.getRemoteSocketAddress());
                handler.start();
            }
        } catch (IOException e) {
            throw new RuntimeException("Proto server stopped unexpectedly", e);
        }
    }

    private static void receiveMessages(Socket socket, Path logFile) {
        try (Socket client = socket; InputStream input = client.getInputStream()) {
            LogMessageOuterClass.LogMessage message;
            while ((message = LogMessageOuterClass.LogMessage.parseDelimitedFrom(input)) != null) {
                appendLogEntry(logFile, message);
            }
        } catch (IOException | RuntimeException e) {
            System.err.printf(
                "Could not process client %s: %s%n",
                socket.getRemoteSocketAddress(),
                e.getMessage());
        }
    }

    private static synchronized void appendLogEntry(
            Path logFile, LogMessageOuterClass.LogMessage message) throws IOException {
        String timestamp = message.hasTimestamp()
            ? DateTimeFormatter.ISO_INSTANT.format(toInstant(message))
            : "-";
        String source = sanitize(message.getSource());
        String body = sanitize(message.getBody());
        String sourcePart = source.isEmpty() ? "" : " [" + source + "]";
        String entry = String.format(
            "%s %s%s %s%n",
            timestamp,
            message.getSeverity().name(),
            sourcePart,
            body);

        Path parent = logFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(
            logFile,
            entry,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.APPEND);
    }

    private static Instant toInstant(LogMessageOuterClass.LogMessage message) {
        return Instant.ofEpochSecond(
            message.getTimestamp().getSeconds(),
            message.getTimestamp().getNanos());
    }

    private static String sanitize(String value) {
        return value.replace("\r", "\\r").replace("\n", "\\n");
    }
}
