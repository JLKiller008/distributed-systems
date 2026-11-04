import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Aufgabe 1b (+1c): HTTP-Client mit java.net.URL.
 * Aufruf: java JavaUrlClient [url] [proxyHost proxyPort]
 */
public class JavaUrlClient {
    public static void main(String[] args) throws Exception {
        URL url = URI.create(args.length > 0 ? args[0] : "http://localhost:4444/index.html").toURL();

        HttpURLConnection httpURLConnection;
        //Hier ein Beispiel mit verwendung eines Proxy Servers.
        if (args.length > 2) {
            // expliziter Proxy fuer diese eine Verbindung
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(args[1], Integer.parseInt(args[2])));
            httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
        } else {
            httpURLConnection = (HttpURLConnection) url.openConnection();
        }
        //Absoluter URL Pfad auch bei Proxy wichtig. Was erwarte ich als antwort/Was soll der Proxy mit meiner Übertragung machen
        httpURLConnection.setRequestMethod("GET");
        //Hatte Probleme bei Stud.fh-wedel.de Wegen php down und deswegen jetzt hier mit einem Timeout sonst loop
        httpURLConnection.setConnectTimeout(5000);
        httpURLConnection.setReadTimeout(5000);

        //Hier wir die Response angezeigt. -> Error bei falscher Webseite

        System.out.println(httpURLConnection.getResponseCode() + " " + httpURLConnection.getResponseMessage());
        //Code für bessere Message auslese aus dem Internet :)
        httpURLConnection.getHeaderFields().forEach((k, v) -> System.out.println((k == null ? "" : k + ": ") + String.join(", ", v)));
        System.out.println();

        var stream = httpURLConnection.getResponseCode() >= 400 ? httpURLConnection.getErrorStream() : httpURLConnection.getInputStream();
        if (stream != null) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
            }
        }
        httpURLConnection.disconnect();
    }
}
