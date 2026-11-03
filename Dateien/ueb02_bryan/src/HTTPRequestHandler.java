import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class HTTPRequestHandler implements Runnable {

    BufferedReader br;
    OutputStream output;
    Socket socket;

    public HTTPRequestHandler(Socket socket) throws IOException {

        //Inputstream wrapped twice to read full lines of symbols instead of singular bytes
        br = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        //Reply will be in raw bytes
        output = socket.getOutputStream();
        this.socket = socket;
    }

    public void run() {

        //A response is created as soon as a request is read but will only be sent after the input is fully read
        boolean responseFormed = false;
        byte[] responseBytes = "".getBytes();
        
        //Read first headerLine
        String headerLine = "";
        try {
            headerLine = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Continue reading input. Input is fully read if: Client closes connection
        //preemptively, the empty line is read, there is no content in the header. As readLine consumes 
        //the \r\n case 2 and 3 are identical here
        while (headerLine != null && !headerLine.equals("")) {

            System.out.println("Header: " + headerLine);

            if (!responseFormed) {

                //Baseline reply values
                String status = "HTTP/1.1 200 OK";
                // Defining how the reply shall be interpreted
                String headers = "Content-Type: text/html; charset=utf-8\r\nConnection: close";
                String emptyLine = "\r\n";
                String body = "";

                // Reading request word by word
                StringTokenizer s = new StringTokenizer(headerLine);
                String temp = "";
                if(s.hasMoreTokens()) {
                    temp = s.nextToken();
                } 

                //For now this server only accepts GET requests
                if (temp.equals("GET") && s.hasMoreTokens()) {

                        String target = s.nextToken();

                        // 3 mock situations
                        switch (target) {
                            case "/":
                                body = "There sure is a site here";

                                break;

                            case "/realFile":
                                body = "There most definitely is a file with that name";

                                break;

                            default:
                                status = "HTTP/1.1 404 Not found";
                                body = "Path doesnt exist";
                                break;
                        }

                } else {
                    status = "HTTP/1.1 400 Bad Request";
                    body = "Invalid Request";
                } 

                String responseString = status + "\r\n" + headers + "\r\n" + emptyLine + body + "\r\n";

                responseBytes = responseString.getBytes(StandardCharsets.UTF_8);

                responseFormed = true;

            }

            headerLine = "";
            try {
                headerLine = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            output.write(responseBytes);
            output.flush();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
