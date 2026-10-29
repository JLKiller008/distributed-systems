import com.google.protobuf.Timestamp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.Locale;

public class ProtoClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 45123;
    private static final int CONNECT_TIMEOUT_MILLIS = 5_000;

    public static void main(String[] args) {
        try {
            LogMessageOuterClass.LogMessage message = createMessage(args);
            sendMessage(message, args);
            System.out.println("Log message sent.");
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid argument: " + e.getMessage());
            System.err.println("Usage: ProtoClient [host] [port] [severity] [source] [message...]");
        } catch (IOException e) {
            System.err.println("Could not send log message: " + e.getMessage());
        }
    }

    private static LogMessageOuterClass.LogMessage createMessage(String[] args) {
        String severityName = args.length > 2 ? args[2] : "INFO";
        LogMessageOuterClass.LogMessage.Severity severity;
        try {
            severity = LogMessageOuterClass.LogMessage.Severity.valueOf(
                severityName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            severity = LogMessageOuterClass.LogMessage.Severity.SEVERITY_UNSPECIFIED;
        }

        String source = args.length > 3 ? args[3] : "";
        String body = args.length > 4 ? String.join(" ", java.util.Arrays.copyOfRange(args, 4, args.length))
                                      : "Test log message";

        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
            .setSeconds(now.getEpochSecond())
            .setNanos(now.getNano())
            .build();

        return LogMessageOuterClass.LogMessage.newBuilder()
            .setTimestamp(timestamp)
            .setSource(source)
            .setSeverity(severity)
            .setBody(body)
            .build();
    }

    private static void sendMessage(
            LogMessageOuterClass.LogMessage message, String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);
            message.writeDelimitedTo(socket.getOutputStream());
            socket.getOutputStream().flush();
        }
    }
}
