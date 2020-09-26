package tw.ouyang;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RequestProcessor implements Runnable {

    private Socket socket;

    public RequestProcessor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream())) {

            Request request = getClientRequest(reader);
            logRequest(request);
            responseToClient(request, writer);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Request getClientRequest(BufferedReader reader) throws IOException {
        String line = null;
        StringBuffer requestHeader = new StringBuffer();
        while ((line = reader.readLine()).length() > 0) {
            requestHeader.append(line + "\n");
        }
        return new Request(requestHeader.toString());
    }

    private void logRequest(Request request) {
        try (FileWriter writer = new FileWriter("D:/var/log/httpd.log", true)) {
            writer.write(request.getRequestHeader() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void responseToClient(Request request, PrintWriter writer) {
        String response = createResponse(request);
        writer.write(response);
    }

    private String createResponse(Request request) {
        String response = "HTTP/1.1 200 OK"
                + "Content-Length: %d"
                + "Content-Type: text/html\r\n\r\n%s";
        String requestResource = getRequestResource(request);
        return String.format(response, requestResource.length(), requestResource);
    }

    private String getRequestResource(Request request) {
        StringBuffer requestPage = new StringBuffer();
        try (BufferedReader filereader = new BufferedReader(new FileReader("D:/var/www/" + request.getRequestResourcePath()))) {
            String line = null;
            while ((line = filereader.readLine()) != null) {
                requestPage.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requestPage.toString();
    }

}
