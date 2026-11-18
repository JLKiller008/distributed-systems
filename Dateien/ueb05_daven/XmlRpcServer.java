import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * XML-RPC-Server (Aufgabe 5) auf Basis des Thread-pro-Verbindung-Servers aus ueb02 und der Datenbank aus ueb03.
 * Erkennt automatisch, ob die Verbindung
 *   - XML-RPC ueber HTTP ("POST /RPC2 HTTP/1.1 ...", Standard laut Spezifikation) oder
 *   - direktes XML-RPC (nur das methodCall-XML ueber den Socket)
 * spricht. Aufruf: java XmlRpcServer [port]
 */
public class XmlRpcServer {
    private static final Database db = new Database();

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 4447;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("XML-RPC server listening on port " + serverSocket.getLocalPort());
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
                new Thread(() -> handle(socket)).start();
            }
        }
    }

    private static void handle(Socket socket) {
        try (socket) {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            byte[] firstLine = readLine(in);
            if (firstLine == null) return;
            boolean http = new String(firstLine, StandardCharsets.ISO_8859_1).startsWith("POST ");

            byte[] requestXml;
            if (http) {
                int contentLength = 0;
                String line;
                while (!(line = new String(readLine(in), StandardCharsets.ISO_8859_1)).isEmpty()) {
                    if (line.toLowerCase().startsWith("content-length:")) {
                        contentLength = Integer.parseInt(line.substring(15).trim());
                    }
                }
                requestXml = in.readNBytes(contentLength); // Content-Length gibt das Nachrichtenende vor
            } else {
                // direkt: so lange lesen, bis </methodCall> kommt (Ende der Nachricht)
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                buf.write(firstLine);
                buf.write('\n');
                while (!buf.toString(StandardCharsets.UTF_8).contains("</methodCall>")) {
                    int b = in.read();
                    if (b < 0) break;
                    buf.write(b);
                }
                requestXml = buf.toByteArray();
            }
            System.out.println("Request:  " + new String(requestXml, StandardCharsets.UTF_8).trim());

            String responseXml = execute(requestXml);
            byte[] body = responseXml.getBytes(StandardCharsets.UTF_8);
            System.out.println("Response: " + responseXml);

            if (http) { // laut Spezifikation immer "200 OK", auch bei fault
                out.write(("HTTP/1.1 200 OK\r\nConnection: close\r\nContent-Type: text/xml\r\n"
                        + "Content-Length: " + body.length + "\r\nServer: DS-XmlRpc/1.0\r\n\r\n")
                        .getBytes(StandardCharsets.ISO_8859_1));
            }
            out.write(body);
            out.flush();
        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    /** Parst den methodCall mit JAXP, ruft die Datenbank auf und baut die methodResponse. */
    private static String execute(byte[] requestXml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // kein XXE
            Document doc = f.newDocumentBuilder().parse(new ByteArrayInputStream(requestXml));

            Element call = doc.getDocumentElement();
            if (!call.getTagName().equals("methodCall")) return fault(-32600, "root element must be methodCall");
            String method = child(call, "methodName").getTextContent().trim();

            List<Object> params = new ArrayList<>();
            Element paramsEl = child(call, "params");
            if (paramsEl != null) {
                NodeList list = paramsEl.getElementsByTagName("param");
                for (int i = 0; i < list.getLength(); i++) {
                    params.add(parseValue(child((Element) list.item(i), "value")));
                }
            }

            switch (method) {
                case "getRecord": {
                    if (params.size() != 1 || !(params.get(0) instanceof Integer)) return fault(3, "getRecord(int key) expected");
                    String record = db.getRecord((Integer) params.get(0));
                    if (record == null) return fault(1, "no record for key " + params.get(0));
                    return response("<string>" + escape(record) + "</string>");
                }
                case "addRecord": {
                    if (params.size() != 2 || !(params.get(0) instanceof Integer) || !(params.get(1) instanceof String))
                        return fault(3, "addRecord(int key, string value) expected");
                    db.addRecord((Integer) params.get(0), (String) params.get(1));
                    return response("<boolean>1</boolean>");
                }
                case "getSize":
                    return response("<int>" + db.getSize() + "</int>");
                default:
                    return fault(2, "unknown method " + method);
            }
        } catch (Exception e) {
            return fault(-32700, "parse error: " + e.getMessage());
        }
    }

    /** value -> Integer (i4/int) oder String (string bzw. ohne Typangabe). */
    private static Object parseValue(Element value) {
        for (Node n = value.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element) {
                Element e = (Element) n;
                switch (e.getTagName()) {
                    case "i4": case "int": return Integer.valueOf(e.getTextContent().trim());
                    case "string": return e.getTextContent();
                    default: throw new IllegalArgumentException("unsupported type " + e.getTagName());
                }
            }
        }
        return value.getTextContent(); // ohne Typ = string
    }

    private static Element child(Element parent, String name) {
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element && ((Element) n).getTagName().equals(name)) return (Element) n;
        }
        return null;
    }

    private static String response(String typedValue) {
        return "<?xml version=\"1.0\"?><methodResponse><params><param><value>" + typedValue
                + "</value></param></params></methodResponse>";
    }

    private static String fault(int code, String message) {
        return "<?xml version=\"1.0\"?><methodResponse><fault><value><struct>"
                + "<member><name>faultCode</name><value><int>" + code + "</int></value></member>"
                + "<member><name>faultString</name><value><string>" + escape(message) + "</string></value></member>"
                + "</struct></value></fault></methodResponse>";
    }

    static String escape(String s) { // laut Spezifikation muessen nur < und & maskiert werden
        return s.replace("&", "&amp;").replace("<", "&lt;");
    }

    /** Liest bis zum Zeilenende (ohne CR LF); null bei Verbindungsende. */
    static byte[] readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) >= 0 && b != '\n') {
            if (b != '\r') buf.write(b);
        }
        return b < 0 && buf.size() == 0 ? null : buf.toByteArray();
    }
}
