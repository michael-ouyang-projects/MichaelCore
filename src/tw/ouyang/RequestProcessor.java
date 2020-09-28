package tw.ouyang;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

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
        outputStream.write(createResponse(request));
    }

    private byte[] createResponse(Request request) {
        String info = "HTTP/1.1 200 OK"
                + "Content-Length: %d"
                + "Content-Type: %s\r\n\r\n";
        byte[] resource = getResource(request);
        byte[] responseInfo = String.format(info, resource.length, request.getContentType()).getBytes();
        int responseSize = responseInfo.length + resource.length;
        return ByteBuffer.allocate(responseSize).put(responseInfo).put(resource).array();
    }

    private byte[] getResource(Request request) {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream("D:/var/www/" + request.getResourcePath()))) {
            int step = 0;
            byte[] dataBlock = new byte[1024];
            while ((step = inputStream.read(dataBlock)) > 0) {
                dataStream.write(Arrays.copyOf(dataBlock, step));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataStream.toByteArray();
    }

}
