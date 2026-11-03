package LogClasses;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;
import LogProto.*;

public class LogMessageRequestHandler implements Runnable {

    InputStream in;
    OutputStream out;
    Socket socket;
    static String logPath = "log.txt";

    public LogMessageRequestHandler(Socket socket) throws IOException {

        in = socket.getInputStream();
        out = socket.getOutputStream();
        this.socket = socket;
    }

    public void closeConnection() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    static synchronized void writeLogEntry(LogMessage logMessage) throws IOException {
        PrintWriter logWriter = new PrintWriter(new FileWriter(logPath, true));
        String timestampText = ("Created at: " + Instant.ofEpochSecond(logMessage.getCreatedAt().getSeconds(),
                logMessage.getCreatedAt().getNanos()));
        String authorString = "";
        if (logMessage.hasAuthorDetails()) {
            authorString = "Author: " + logMessage.getAuthorDetails();
        } else {
            authorString = "Author: Unspecified";
        }
        String severityString = ("Severity: " + logMessage.getSeverity());
        String bodyString = ("Body: " + logMessage.getBody());

        String logText = timestampText + " | " + authorString + " | " + severityString + " | " + bodyString;

        logWriter.println(logText);
        logWriter.close();
    }

    public void run() {
        LogMessage logMessage;
        try {
            logMessage = LogMessage.parseDelimitedFrom(in);
            if (logMessage != null) {
                int size = logMessage.getSerializedSize();
                System.out.println("Message of length " + size + " received.");

                writeLogEntry(logMessage);
            }
            closeConnection();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
