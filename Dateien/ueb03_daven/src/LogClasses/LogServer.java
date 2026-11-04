package LogClasses;

import LogProto.LogMessage;
import com.google.protobuf.Timestamp;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Log-Server: nimmt length-delimited LogMessage-Protobuf-Nachrichten entgegen und schreibt
 * pro Nachricht eine Zeile in die Logdatei. Pro Verbindung ein Thread.
 * Aufruf: java -cp "out;lib/protobuf-java-4.36.2.jar" LogClasses.LogServer [port] [logdatei]
 */
public class LogServer {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 4445;
        Path logFile = Path.of(args.length > 1 ? args[1] : "server.log");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("LogServer listening on port " + serverSocket.getLocalPort() + ", log file " + logFile);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
                new Thread(() -> receive(socket, logFile)).start();
            }
        }
    }

    private static void receive(Socket socket, Path logFile) {
        try (socket; InputStream in = socket.getInputStream()) {
            LogMessage message;
            // parseDelimitedFrom liefert null, sobald der Client die Verbindung schliesst
            while ((message = LogMessage.parseDelimitedFrom(in)) != null) {
                append(logFile, message);
            }
        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    /** Eine Zeile pro Eintrag: "<ISO-Zeit> <SEVERITY> [source] body". Synchronisiert wegen mehrerer Threads. */
    private static synchronized void append(Path logFile, LogMessage m) throws Exception {
        Timestamp t = m.getTimestamp();
        String time = m.hasTimestamp()
                ? DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(t.getSeconds(), t.getNanos()))
                : "-";
        String source = m.hasSource() && !m.getSource().isEmpty() ? " [" + oneLine(m.getSource()) + "]" : "";
        String line = time + " " + m.getSeverity().name() + source + " " + oneLine(m.getBody()) + System.lineSeparator();
        Files.writeString(logFile, line, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        System.out.print("logged: " + line);
    }

    private static String oneLine(String s) {
        return s.replace("\r", "\\r").replace("\n", "\\n");
    }
}
