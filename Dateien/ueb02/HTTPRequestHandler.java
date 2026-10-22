package Dateien.ueb02;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class HTTPRequestHandler implements Runnable {
    private static final Path INDEX_FILE = Path.of("Dateien", "ueb02", "index.html");
    private final Socket socket;

    public HTTPRequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isBlank()) {
                sendResponse(writer, 400, "Bad Request", "Bad Request");
                return;
            }

            String[] requestParts = requestLine.split(" ", 3);
            if (requestParts.length != 3) {
                sendResponse(writer, 400, "Bad Request", "Bad Request");
                return;
            }

            String method = requestParts[0];
            String target = requestParts[1];
            if (!"GET".equals(method)) {
                sendResponse(writer, 405, "Method Not Allowed", "Method Not Allowed");
                return;
            }
            if (!"/".equals(target)) {
                sendResponse(writer, 404, "Not Found", "Not Found");
                return;
            }

            String indexHtml = Files.readString(INDEX_FILE, StandardCharsets.UTF_8);
            sendResponse(writer, 200, "OK", indexHtml, "text/html; charset=UTF-8");
        } catch (Exception e) {
            System.out.println("Error handling request: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void sendResponse(BufferedWriter writer, int statusCode, String statusText,
                              String body) throws IOException {
        sendResponse(writer, statusCode, statusText, body, "text/plain; charset=UTF-8");
    }

    private void sendResponse(BufferedWriter writer, int statusCode, String statusText,
                              String body, String contentType) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        writer.write("HTTP/1.1 " + statusCode + " " + statusText + "\r\n");
        writer.write("Content-Type: " + contentType + "\r\n");
        writer.write("Content-Length: " + bodyBytes.length + "\r\n");
        writer.write("Connection: close\r\n");
        writer.write("\r\n");
        writer.flush();
        socket.getOutputStream().write(bodyBytes);
        socket.getOutputStream().flush();
    }
}
