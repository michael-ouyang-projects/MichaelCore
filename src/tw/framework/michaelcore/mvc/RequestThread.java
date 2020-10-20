package tw.framework.michaelcore.mvc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class RequestThread implements Runnable {

    private Socket socket;
    private RequestProcessor processor;

    public RequestThread(Socket socket, RequestProcessor processor) {
        this.socket = socket;
        this.processor = processor;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream outputStream = socket.getOutputStream()) {
            RequestInfo request = processor.getClientRequest(reader);
            if (request != null) {
                processor.logRequest(request);
                processor.responseToClient(request, outputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
