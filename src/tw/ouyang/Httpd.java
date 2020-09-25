package tw.ouyang;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Httpd {

    public static void main(String[] args) {
        init(Paths.get("D:/var/www"), Paths.get("D:/var/log"));
        startHttpd(8080);
    }

    private static void init(Path wwwDirectory, Path logDirectory) {
        try {
            initWebData(wwwDirectory);
            initLogData(logDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startHttpd(int port) {
        ExecutorService executor = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                executor.submit(new RequestProcessor(serverSocket.accept()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initWebData(Path wwwDirectory) throws IOException {
        if (!(Files.exists(wwwDirectory) && Files.isDirectory(wwwDirectory))) {
            Files.createDirectories(wwwDirectory);
            createWelcomePage(new File(wwwDirectory.toString(), "index.html"));
        }
    }

    private static void createWelcomePage(File welcomePage) {
        if (!(welcomePage.exists() && welcomePage.isFile())) {
            try (FileWriter writer = new FileWriter(welcomePage)) {
                writer.write("<html><body><h1>Web Server Implemented Using Java!</h1></body></html>");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void initLogData(Path logDirectory) throws IOException {
        if (!(Files.exists(logDirectory) && Files.isDirectory(logDirectory))) {
            Files.createDirectories(logDirectory);
            createLogPage(new File(logDirectory.toString(), "httpd.log"));
        }
    }

    private static void createLogPage(File logPage) throws IOException {
        if (!(logPage.exists() && logPage.isFile())) {
            logPage.createNewFile();
        }
    }

}
