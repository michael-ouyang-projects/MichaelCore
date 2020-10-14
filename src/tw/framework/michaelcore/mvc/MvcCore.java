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
import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Value;
import tw.framework.michaelcore.mvc.annotation.Controller;
import tw.framework.michaelcore.mvc.annotation.Get;
import tw.framework.michaelcore.mvc.annotation.Post;

@Configuration
public class MvcCore {

    @Value
    public String listeningPort;

    @Value
    public String welcomePage;

    @Value
    public String loggingPage;

    private static Map<String, Map<String, Method>> requestMapping = createRequestMapping();

    private static Map<String, Map<String, Method>> createRequestMapping() {
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
    public void initializeMvcCore() {
        try {
            initializeRequestMapping();
            createNecessaryDirectories();
            createNecessaryFiles();
        } catch (Exception e) {
            System.err.println("initializeMvcCore Error!");
            e.printStackTrace();
        }
    }

    private void initializeRequestMapping() throws Exception {
        for (String fqcn : CoreContext.getFqcns()) {
            Class<?> clazz = getClassByFqcn(fqcn);
            if (isControllerClass(clazz)) {
                mapUrlToMethod(clazz);
            }
        }
    }

    private static Class<?> getClassByFqcn(String fqcn) throws Exception {
        return Class.forName(fqcn);
    }

    private static boolean isControllerClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Controller.class);
    }

    private static void mapUrlToMethod(Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            if (isGetMethod(method)) {
                requestMapping.get("GET").put(method.getAnnotation(Get.class).value(), method);
            } else if (isPostMethod(method)) {
                requestMapping.get("POST").put(method.getAnnotation(Post.class).value(), method);
            }
        }
    }

    private static boolean isGetMethod(Method method) {
        return method.isAnnotationPresent(Get.class);
    }

    private static boolean isPostMethod(Method method) {
        return method.isAnnotationPresent(Post.class);
    }

    private void createNecessaryDirectories() throws IOException {
        Path templatePath = Paths.get("resources/templates");
        if (directoryNotExist(templatePath)) {
            Files.createDirectories(templatePath);
        }
        Path wwwPath = Paths.get(new File(welcomePage).getParent());
        if (directoryNotExist(wwwPath)) {
            Files.createDirectories(wwwPath);
        }
        Path logPath = Paths.get(new File(loggingPage).getParent());
        if (directoryNotExist(logPath)) {
            Files.createDirectories(logPath);
        }
    }

    private boolean directoryNotExist(Path directory) {
        return !(Files.exists(directory) && Files.isDirectory(directory));
    }

    private void createNecessaryFiles() throws IOException {
        File welcomePage = new File(this.welcomePage);
        if (fileNotExist(welcomePage)) {
            createWelcomePage(welcomePage);
        }
        File loggingPage = new File(this.loggingPage);
        if (fileNotExist(loggingPage)) {
            loggingPage.createNewFile();
        }
    }

    private boolean fileNotExist(File file) {
        return !(file.exists() && file.isFile());
    }

    private void createWelcomePage(File welcomePage) {
        try (FileWriter writer = new FileWriter(welcomePage)) {
            writer.write("Web Server Implemented Using Java!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Map<String, Method>> getRequestMapping() {
        return requestMapping;
    }

}
