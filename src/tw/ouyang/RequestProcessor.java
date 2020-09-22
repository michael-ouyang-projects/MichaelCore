package tw.ouyang;

import java.io.BufferedReader;
import java.io.FileReader;
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

            String requestString = null;
            while ((requestString = reader.readLine()).length() > 0) {
                System.out.println(requestString);
            }
            System.out.println();

            String response = "HTTP/1.1 200 OK"
                    + "Content-Length: 788"
                    + "Content-Type: text/html\r\n\r\n";
            writer.write(response);
            try (BufferedReader filereader = new BufferedReader(new FileReader("D:/var/www/index.html"))) {
                String line = null;
                while ((line = filereader.readLine()) != null) {
                    writer.write(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
