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
        createNecessaryFile(Paths.get("D:/var/www"), Paths.get("D:/var/log"));
        startHttpd(80);
    }

    private static void createNecessaryFile(Path wwwDirectory, Path logDirectory) {
        try {
            createWebDirectoryAndWelcomePage(wwwDirectory, new File(wwwDirectory.toString(), "index.html"));
            createLogDirectoryAndLogPage(logDirectory, new File(logDirectory.toString(), "httpd.log"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createWebDirectoryAndWelcomePage(Path wwwDirectory, File welcomePage) throws IOException {
        if (needToCreateDirectory(wwwDirectory)) {
            Files.createDirectories(wwwDirectory);
        }
        if (needToCreateFile(welcomePage)) {
            try (FileWriter writer = new FileWriter(welcomePage)) {
                writer.write("<html><body>"
                        + "<link rel='icon' href='favicon.ico' type='image/icon type'>"
                        + "<h1>Web Server Implemented Using Java!</h1>"
                        + "<img src='favicon.ico'/>"
                        + "</body></html>");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void createLogDirectoryAndLogPage(Path logDirectory, File logPage) throws IOException {
        if (needToCreateDirectory(logDirectory)) {
            Files.createDirectories(logDirectory);
        }
        if (needToCreateFile(logPage)) {
            logPage.createNewFile();
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

    private static boolean needToCreateDirectory(Path directory) {
        return !(Files.exists(directory) && Files.isDirectory(directory));
    }

    private static boolean needToCreateFile(File file) {
        return !(file.exists() && file.isFile());
    }

}
