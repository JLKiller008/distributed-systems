package Dateien.ueb02;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class Client {
    private static final String DEFAULT_URL = "http://localhost:45123/";
    private static final int TIMEOUT_MILLIS = 5_000;

    public static void main(String[] args) {
        String address = args.length == 0 ? DEFAULT_URL : args[0];

        try {
            System.out.println("Response using java.net.URL:");
            System.out.println(requestWithUrl(URI.create(address).toURL()));

            System.out.println("----------------------------------------");

            System.out.println("Response using java.net.http.HttpClient:");
            System.out.println(requestWithHttpClient(address));
        } catch (IOException e) {
            System.err.println("HTTP request failed: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("HTTP request was interrupted");
        }
    }

    /**
     * Opens a connection from a java.net.URL and sends a GET request.
     * 
     * @param url The URL to send the GET request to.
     * @return The response body as a String.
     * @throws IOException If an I/O error occurs during the request.
     */
    public static String requestWithUrl(URL url) throws IOException {
        // Potential HTTP Proxy => Pass it to the url.openConnection() method if needed.
        
        /*
        Proxy proxy = new Proxy(
            Proxy.Type.HTTP,
            new InetSocketAddress("localhost", 8080)
        );
        */

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(TIMEOUT_MILLIS);
        connection.setReadTimeout(TIMEOUT_MILLIS);

        try {
            int statusCode = connection.getResponseCode();
            InputStream responseStream = statusCode >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();
            String responseBody;
            if (responseStream == null) {
                responseBody = "";
            } else {
                try (responseStream) {
                    responseBody = new String(
                        responseStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            }

            ensureSuccessful(statusCode, responseBody);
            return responseBody;
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Accepts a URL as a normal String and uses the modern Java HTTP client.
     * 
     * @param address The URL to send the GET request to.
     * @return The response body as a String.
     * @throws IOException If an I/O error occurs during the request.
     * @throws InterruptedException If the request is interrupted.
     */
    public static String requestWithHttpClient(String address)
            throws IOException, InterruptedException {
        // Potential HTTP Proxy => add .proxy() to the HttpClient builder if needed.
        // .proxy(ProxySelector.of(new InetSocketAddress("localhost", 8080)))

        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofMillis(TIMEOUT_MILLIS))
            .build();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(address))
            .timeout(java.time.Duration.ofMillis(TIMEOUT_MILLIS))
            .GET()
            .build();

        HttpResponse<String> response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        ensureSuccessful(response.statusCode(), response.body());
        return response.body();
    }

    /**
     * Ensures that the HTTP response status code indicates success (2xx).
     * If not, throws an IOException with the status code and response body.
     * @param statusCode the HTTP response status code
     * @param responseBody the HTTP response body
     * @throws IOException if the status code is not in the 2xx range
     */
    private static void ensureSuccessful(int statusCode, String responseBody)
            throws IOException {
        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException(
                "Server returned HTTP " + statusCode + ": " + responseBody);
        }
    }
}
