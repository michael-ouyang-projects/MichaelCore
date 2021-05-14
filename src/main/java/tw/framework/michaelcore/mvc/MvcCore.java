package tw.framework.michaelcore.mvc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;

import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Bean;
import tw.framework.michaelcore.ioc.annotation.ExecuteAfterContextCreate;
import tw.framework.michaelcore.ioc.annotation.ExecuteBeforeContextDestroy;
import tw.framework.michaelcore.ioc.annotation.Value;
import tw.framework.michaelcore.ioc.annotation.components.Configuration;
import tw.framework.michaelcore.ioc.annotation.components.Controller;
import tw.framework.michaelcore.ioc.annotation.components.RestController;
import tw.framework.michaelcore.mvc.annotation.Delete;
import tw.framework.michaelcore.mvc.annotation.Get;
import tw.framework.michaelcore.mvc.annotation.Post;
import tw.framework.michaelcore.mvc.annotation.Put;

@Configuration
public class MvcCore {

    @Value("server.port")
    private String port;

    @Value("web.welcomePage")
    private String welcomePage;

    @Value("web.loggingPage")
    private String loggingPage;

    @Autowired
    private RequestProcessor requestProcessor;

    private boolean isSocketClosed;
    private ServerSocket serverSocket;
    private static Map<String, Map<String, Method>> requestMapping;

    @Bean
    public Gson createGson() {
        return new Gson();
    }

    @ExecuteBeforeContextDestroy
    public void clean() {
        try {
            isSocketClosed = true;
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ExecuteAfterContextCreate(order = 2)
    public void startServer() {
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(() -> {
            try {
                serverSocket = new ServerSocket(Integer.parseInt(port));
                while (true) {
                    executor.submit(new RequestTask(serverSocket.accept(), requestProcessor));
                }
            } catch (SocketException e) {
                if (!isSocketClosed) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @ExecuteAfterContextCreate(order = 1)
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
        requestMapping = createRequestMapping();
        for (Class<?> clazz : CoreContext.getClasses()) {
            if (isControllerOrRestController(clazz)) {
                mapUrlToMethod(clazz);
            }
        }
    }

    private Map<String, Map<String, Method>> createRequestMapping() {
        Map<String, Map<String, Method>> requestMapping = new HashMap<>();
        requestMapping.put("GET", new HashMap<>());
        requestMapping.put("POST", new HashMap<>());
        requestMapping.put("PUT", new HashMap<>());
        requestMapping.put("DELETE", new HashMap<>());
        return requestMapping;
    }

    private boolean isControllerOrRestController(Class<?> clazz) {
        return clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class);
    }

    private void mapUrlToMethod(Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Get.class)) {
                requestMapping.get("GET").put(method.getAnnotation(Get.class).value(), method);
            } else if (method.isAnnotationPresent(Post.class)) {
                requestMapping.get("POST").put(method.getAnnotation(Post.class).value(), method);
            } else if (method.isAnnotationPresent(Put.class)) {
                requestMapping.get("PUT").put(method.getAnnotation(Put.class).value(), method);
            } else if (method.isAnnotationPresent(Delete.class)) {
                requestMapping.get("DELETE").put(method.getAnnotation(Delete.class).value(), method);
            }
        }
    }

    private void createNecessaryDirectories() throws IOException {
        Path templatePath = Paths.get("src/main/resources/templates");
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
