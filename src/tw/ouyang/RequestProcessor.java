package tw.ouyang;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class RequestProcessor implements Runnable {

    private Socket socket;

    public RequestProcessor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream())) {

            Request request = getClientRequest(reader);
            if (request.getRequestInfo() != null) {
                logRequest(request);
                responseToClient(request, outputStream);
            } else {
                System.err.println("ERROR");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Request getClientRequest(BufferedReader reader) throws IOException {
        StringBuffer requestInfo = new StringBuffer();
        String line = reader.readLine();
        while (line != null && line.length() > 0) {
            requestInfo.append(line + "\n");
            line = reader.readLine();
        }
        return new Request(requestInfo.toString());
    }

    private void logRequest(Request request) {
        try (FileWriter writer = new FileWriter("D:/var/log/httpd.log", true)) {
            writer.write(request.getRequestInfo() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void responseToClient(Request request, OutputStream outputStream) throws IOException {
        outputStream.write(createResponse(request).getBytes());
    }

    private String createResponse(Request request) {
        if (request.getRequestResourcePath().endsWith(".html")) {
            String response = "HTTP/1.1 200 OK"
                    + "Content-Length: %d"
                    + "Content-Type: text/html\r\n\r\n%s";
            String requestResource = getRequestResource(request);
            return String.format(response, requestResource.length(), requestResource);
        } else {
            String response = "HTTP/1.1 200 OK"
                    + "Content-Length: %d"
                    + "Content-Type: image/x-icon\r\n\r\n%s";
            String requestResource = getRequestResource(request);
            return String.format(response, requestResource.length(), requestResource);
        }
    }

    private String getRequestResource(Request request) {
        StringBuffer requestResource = new StringBuffer();
        try (BufferedReader reader = new BufferedReader(new FileReader("D:/var/www/" + request.getRequestResourcePath()))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                requestResource.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requestResource.toString();
    }

}
