import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringTokenizer;

/** Bearbeitet genau eine HTTP-Verbindung (ein Request, danach Connection: close). */
public class HttpRequestHandler implements Runnable {
    private final Socket socket;
    private final Path root;

    public HttpRequestHandler(Socket socket, String docRoot) {
        this.socket = socket;
        this.root = Path.of(docRoot).toAbsolutePath().normalize();
    }

    @Override
    public void run() {
        try (socket) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
            OutputStream out = socket.getOutputStream();

            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isBlank()) {
                return; // Verbindung ohne Request
            }
            System.out.println("Request: " + requestLine);

            // Header bis zur leeren Zeile (CRLF) lesen und verwerfen
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                System.out.println("  " + line);
            }

            StringTokenizer st = new StringTokenizer(requestLine);
            if (st.countTokens() != 3) {
                sendText(out, 400, "Bad Request", true);
                return;
            }
            String method = st.nextToken();
            String target = st.nextToken();

            boolean head = method.equals("HEAD");
            if (!method.equals("GET") && !head) {
                sendText(out, 405, "Method Not Allowed", true);
                return;
            }

            // Query/Fragment entfernen, URL-dekodieren
            int cut = target.indexOf('?');
            if (cut >= 0) target = target.substring(0, cut);
            target = URLDecoder.decode(target, StandardCharsets.UTF_8).replace('\\', '/');

            Path file = root.resolve("." + (target.startsWith("/") ? target : "/" + target)).normalize();
            if (!file.startsWith(root)) { // Path-Traversal (../) verhindern
                sendText(out, 403, "Forbidden", true);
                return;
            }
            if (Files.isDirectory(file)) {
                file = file.resolve("index.html");
            }
            if (!Files.isRegularFile(file)) {
                sendText(out, 404, "Not Found", true);
                return;
            }

            byte[] body = Files.readAllBytes(file);
            sendHeader(out, 200, "OK", contentType(file.getFileName().toString()), body.length);
            if (!head) out.write(body);
            out.flush();
        } catch (Exception e) {
            System.out.println("Error handling request: " + e.getMessage());
        }
    }

    private void sendText(OutputStream out, int code, String text, boolean body) throws IOException {
        byte[] bytes = (code + " " + text).getBytes(StandardCharsets.UTF_8);
        sendHeader(out, code, text, "text/plain; charset=UTF-8", bytes.length);
        if (body) out.write(bytes);
        out.flush();
    }

    private void sendHeader(OutputStream out, int code, String text, String type, long length) throws IOException {
        String header = "HTTP/1.1 " + code + " " + text + "\r\n"
                + "Content-Type: " + type + "\r\n"
                + "Content-Length: " + length + "\r\n"
                + "Connection: close\r\n"
                + "\r\n";
        out.write(header.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static String contentType(String name) {
        String n = name.toLowerCase();
        if (n.endsWith(".html") || n.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (n.endsWith(".txt") || n.endsWith(".md")) return "text/plain; charset=UTF-8";
        if (n.endsWith(".css")) return "text/css";
        if (n.endsWith(".js")) return "application/javascript";
        if (n.endsWith(".json")) return "application/json";
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        if (n.endsWith(".gif")) return "image/gif";
        if (n.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }
}
