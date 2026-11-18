import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * XML-RPC-Client (Aufgabe 5) auf Basis des Socket-Clients aus ueb02.
 * Aufruf: java XmlRpcClient [host] [port] [http|direct]
 */
public class XmlRpcClient {
    private final String host;
    private final int port;
    private final boolean http;

    XmlRpcClient(String host, int port, boolean http) {
        this.host = host;
        this.port = port;
        this.http = http;
    }

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 4447;
        boolean http = args.length < 3 || args[2].equals("http");
        XmlRpcClient client = new XmlRpcClient(host, port, http);

        Map<Integer, String> data = new LinkedHashMap<>();
        data.put(4101, "Appen");
        data.put(4102, "Ahrensburg");
        data.put(4103, "Wedel");
        data.put(4104, "Aumühle");
        data.put(4105, "Seevetal");
        data.put(4106, "Quickborn");

        for (Map.Entry<Integer, String> e : data.entrySet()) {
            System.out.println("addRecord(" + e.getKey() + ", " + e.getValue() + ") -> " + client.call("addRecord", e.getKey(), e.getValue()));
        }
        for (int key : new int[]{4103, 4107}) {
            System.out.println("getRecord(" + key + ") -> " + client.call("getRecord", key));
        }
        System.out.println("getSize() -> " + client.call("getSize") + " (erwartet: " + data.size() + ")");
    }

    /** Sendet einen XML-RPC-Aufruf und gibt das Ergebnis bzw. "FAULT ..." zurueck. */
    String call(String method, Object... params) throws Exception {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?>\n<methodCall>\n  <methodName>" + method + "</methodName>\n  <params>\n");
        for (Object p : params) {
            String typed = p instanceof Integer ? "<int>" + p + "</int>" : "<string>" + XmlRpcServer.escape((String) p) + "</string>";
            xml.append("    <param><value>").append(typed).append("</value></param>\n");
        }
        xml.append("  </params>\n</methodCall>\n");
        byte[] body = xml.toString().getBytes(StandardCharsets.UTF_8);

        try (Socket socket = new Socket(host, port)) {
            if (http) {
                socket.getOutputStream().write(("POST /RPC2 HTTP/1.1\r\nHost: " + host + ":" + port + "\r\n"
                        + "User-Agent: DS-XmlRpcClient/1.0\r\nContent-Type: text/xml\r\n"
                        + "Content-Length: " + body.length + "\r\nConnection: close\r\n\r\n")
                        .getBytes(StandardCharsets.ISO_8859_1));
            }
            socket.getOutputStream().write(body);
            socket.getOutputStream().flush();

            byte[] raw = socket.getInputStream().readAllBytes(); // Server schliesst nach der Antwort
            if (http) { // HTTP-Header abtrennen (Leerzeile CRLF CRLF)
                String s = new String(raw, StandardCharsets.ISO_8859_1);
                raw = s.substring(s.indexOf("\r\n\r\n") + 4).getBytes(StandardCharsets.ISO_8859_1);
            }
            return parseResponse(raw);
        }
    }

    private static String parseResponse(byte[] xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document doc = f.newDocumentBuilder().parse(new ByteArrayInputStream(xml));

        if (doc.getElementsByTagName("fault").getLength() > 0) {
            String code = doc.getElementsByTagName("int").item(0).getTextContent();
            String text = doc.getElementsByTagName("string").item(0).getTextContent();
            return "FAULT " + code + ": " + text;
        }
        Element value = (Element) doc.getElementsByTagName("value").item(0);
        for (Node n = value.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element) return n.getTextContent();
        }
        return value.getTextContent();
    }
}
