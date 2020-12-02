package tw.framework.michaelcore.mvc;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RequestTask implements Runnable {

    private Socket socket;
    private RequestProcessor processor;

    public RequestTask(Socket socket, RequestProcessor processor) {
        this.socket = socket;
        this.processor = processor;
    }

    @Override
    public void run() {
        try (InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream()) {
            Request request = processor.getRequest(inputStream);
            if (request != null) {
                processor.writeLog(request);
                processor.responseToClient(request, outputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
