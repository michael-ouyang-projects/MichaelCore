package tw.framework.michaelcore.mvc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tw.framework.michaelcore.core.RequestProcessor;
import tw.framework.michaelcore.core.annotation.Configuration;
import tw.framework.michaelcore.core.annotation.ExecuteAfterContainerStartup;
import tw.framework.michaelcore.ioc.annotation.Bean;
import tw.framework.michaelcore.ioc.annotation.Value;

@Configuration
public class MvcCore {

    @Value
    public String listeningPort;

    @Value
    public String welcomePage;

    @Value
    public String loggingPage;

    @Bean
    public Map<String, Map<String, Method>> requestMapping() {
        Map<String, Map<String, Method>> requestMapping = new HashMap<>();
        requestMapping.put("GET", new HashMap<>());
        requestMapping.put("POST", new HashMap<>());
        requestMapping.put("PUT", new HashMap<>());
        requestMapping.put("DELETE", new HashMap<>());
        return requestMapping;
    }

    public void startServer() {
        ExecutorService executor = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(listeningPort))) {
            while (true) {
                executor.submit(new RequestProcessor(serverSocket.accept(), new File(loggingPage)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ExecuteAfterContainerStartup
    public void startup() {
        try {
            createNecessaryDirectories();
            createNecessaryFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createNecessaryDirectories() throws IOException {
        Path templatePath = Paths.get("resources/templates");
        if (directoryIsAbsent(templatePath)) {
            Files.createDirectories(templatePath);
        }
        Path wwwPath = Paths.get(new File(welcomePage).getParent());
        if (directoryIsAbsent(wwwPath)) {
            Files.createDirectories(wwwPath);
        }
        Path logPath = Paths.get(new File(loggingPage).getParent());
        if (directoryIsAbsent(logPath)) {
            Files.createDirectories(logPath);
        }
    }

    private boolean directoryIsAbsent(Path directory) {
        return !(Files.exists(directory) && Files.isDirectory(directory));
    }

    private void createNecessaryFiles() throws IOException {
        File welcomePage = new File(this.welcomePage);
        if (fileIsAbsent(welcomePage)) {
            createWelcomePage(welcomePage);
        }
        File loggingPage = new File(this.loggingPage);
        if (fileIsAbsent(loggingPage)) {
            loggingPage.createNewFile();
        }
    }

    private boolean fileIsAbsent(File file) {
        return !(file.exists() && file.isFile());
    }

    private void createWelcomePage(File welcomePage) {
        try (FileWriter writer = new FileWriter(welcomePage)) {
            writer.write("Web Server Implemented Using Java!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
