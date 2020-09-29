package tw.ouyang;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RequestProcessor implements Runnable {

    private Socket socket;

    public RequestProcessor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream outputStream = socket.getOutputStream()) {
            Request request = getClientRequest(reader);
            if (request != null) {
                logRequest(request);
                responseToClient(request, outputStream);
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
        return requestInfo.toString().length() > 0 ? new Request(requestInfo.toString()) : null;
    }

    private void logRequest(Request request) {
        try (FileWriter writer = new FileWriter("D:/var/log/httpd.log", true)) {
            writer.write(request.getRequestInfo() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void responseToClient(Request request, OutputStream outputStream) throws IOException {
        byte[] response = createResponse(request);
        if (response != null) {
            outputStream.write(response);
        }
    }

    private byte[] createResponse(Request request) {
        byte[] resource = getResource(request);
        if (resource != null) {
            byte[] info = getInfo(request, resource);
            return concatenateInfoAndResource(info, resource);
        }
        return null;
    }

    private byte[] getResource(Request request) {
        byte[] resource = null;
        try {
            resource = Files.readAllBytes(Paths.get("D:/var/www/", request.getResourcePath()));
        } catch (IOException e) {
            System.out.println("Wrong Path!");
        }
        return resource;
    }

    private byte[] getInfo(Request request, byte[] resource) {
        String infoFormat = "HTTP/1.1 200 OK"
                + "Content-Length: %d"
                + "Content-Type: %s\r\n\r\n";
        return String.format(infoFormat, resource.length, request.getContentType()).getBytes();
    }

    private byte[] concatenateInfoAndResource(byte[] info, byte[] resource) {
        int responseSize = info.length + resource.length;
        return ByteBuffer.allocate(responseSize).put(info).put(resource).array();
    }

}
