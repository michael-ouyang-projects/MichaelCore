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

            Request request = getRequestInfo(reader);
            logRequestInfo(request);

            String response = "HTTP/1.1 200 OK"
                    + "Content-Length: %d"
                    + "Content-Type: text/html\r\n\r\n%s";
            StringBuffer webPageData = new StringBuffer();
            try (BufferedReader filereader = new BufferedReader(new FileReader("D:/var/www/" + request.getRequestPath()))) {
                String line = null;
                while ((line = filereader.readLine()) != null) {
                    webPageData.append(line);
                }
                writer.write(String.format(response, webPageData.length(), webPageData));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Request getRequestInfo(BufferedReader reader) throws IOException {
        String line = null;
        StringBuffer requestInfo = new StringBuffer();
        while ((line = reader.readLine()).length() > 0) {
            requestInfo.append(line + "\n");
        }
        return new Request(requestInfo.toString());
    }

    private void logRequestInfo(Request request) {
        try (FileWriter writer = new FileWriter("D:/var/log/httpd.log", true)) {
            writer.write(request.getRequestHeader() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
