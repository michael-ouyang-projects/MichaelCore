package tw.ouyang;

import java.io.File;
import java.io.FileWriter;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Httpd {

    public static void main(String[] args) {
        Path wwwDirectory = Paths.get("D:/var/www");
        Path logDirectory = Paths.get("D:/var/log");
        try {
            if (!(Files.exists(wwwDirectory) && Files.isDirectory(wwwDirectory))) {
                Files.createDirectories(wwwDirectory);
                File welcomePage = new File(wwwDirectory.toString(), "index.html");
                if (!(welcomePage.exists() && welcomePage.isFile())) {
                    try (FileWriter writer = new FileWriter(welcomePage)) {
                        writer.write("<html><body><h1>Web Server Implemented Using Java!</h1></body></html>");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!(Files.exists(logDirectory) && Files.isDirectory(logDirectory))) {
                Files.createDirectories(logDirectory);
                File logPage = new File(logDirectory.toString(), "httpd.log");
                if (!(logPage.exists() && logPage.isFile())) {
                    logPage.createNewFile();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ExecutorService executor = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                executor.submit(new RequestProcessor(serverSocket.accept()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
