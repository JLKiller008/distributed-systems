
package LogClasses;

import LogProto.LogMessage;
import com.google.protobuf.Timestamp;

import java.net.InetAddress;
import java.net.Socket;
import java.time.Instant;

/**
 * Log-Client: sendet LogMessage-Nachrichten (length-delimited) an den LogServer.
 * Aufruf: java -cp "out;lib/protobuf-java-4.36.2.jar" LogClasses.LogClient [host] [port] [severity text...]
 * Ohne severity/text wird je eine Beispielnachricht pro Severity gesendet.
 */
public class LogClient {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 4445;
        String source = "LogClient@" + InetAddress.getLocalHost().getHostName();

        try (Socket socket = new Socket(host, port)) {
            if (args.length > 3) {
                LogMessage.Severity severity = LogMessage.Severity.valueOf(args[2].toUpperCase());
                send(socket, source, severity, String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length)));
            } else {
                for (LogMessage.Severity s : LogMessage.Severity.values()) {
                    if (s == LogMessage.Severity.SEVERITY_UNSPECIFIED || s == LogMessage.Severity.UNRECOGNIZED) continue;
                    send(socket, source, s, "Beispielnachricht mit Severity " + s);
                }
            }
        }
        System.out.println("Log messages sent.");
    }

    private static void send(Socket socket, String source, LogMessage.Severity severity, String body) throws Exception {
        Instant now = Instant.now();
        LogMessage message = LogMessage.newBuilder()
                .setTimestamp(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setSource(source)
                .setSeverity(severity)
                .setBody(body)
                .build();
        message.writeDelimitedTo(socket.getOutputStream()); // Laenge (Varint) + Nachricht
        socket.getOutputStream().flush();
    }
}
