package tw.ouyang;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import tw.ouyang.annotation.Bean;
import tw.ouyang.annotation.Configuration;
import tw.ouyang.annotation.Controller;
import tw.ouyang.annotation.Get;
import tw.ouyang.annotation.Post;

@Configuration
public class WebServer {

    private boolean isWindowsSystem = true;
    private int listeningPort = 8080;
    private String webRoot = "resources";
    private Path templatePath = Paths.get(webRoot, "templates");
    private Path wwwPath = Paths.get(webRoot, "www");
    private Path logPath = Paths.get(webRoot, "log");
    private File welcomePage = new File(wwwPath.toString(), "welcome.html");
    private File loggingPage = new File(logPath.toString(), "httpd.log");

    public static void main(String[] args) {
        new WebServer().start();
    }

    public void start() {
        createNecessaryDirectories();
        createNecessaryFiles();
        initializeContainer();
        startServer();
    }

    private void startServer() {
        ExecutorService executor = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(listeningPort)) {
            while (true) {
                executor.submit(new RequestProcessor(serverSocket.accept(), webRoot, loggingPage));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeContainer() {
        try {
            String applicationPath;
            if (isWindowsSystem) {
                applicationPath = this.getClass().getResource("/").getPath().substring(1);
            } else {
                applicationPath = this.getClass().getResource("/").getPath();
            }
            List<String> fqcnList = Files.walk(Paths.get(applicationPath))
                    .filter(Files::isRegularFile)
                    .filter(classFile -> {
                        if (classFile.getParent().toString().replaceAll("\\\\", "/").split(applicationPath).length == 2) {
                            return classFile.getFileName().toString().endsWith(".class");
                        } else {
                            return false;
                        }
                    }).map(classFile -> {
                        String packagePath = classFile.getParent().toString().replaceAll("\\\\", "/").split(applicationPath)[1].replace("/", ".");
                        String className = classFile.getFileName().toString().split("\\.")[0];
                        return String.format("%s.%s", packagePath, className);
                    }).collect(Collectors.toList());

            fqcnList.forEach(fqcn -> {
                try {
                    Class<?> loopClass = Class.forName(fqcn);
                    if (loopClass.isAnnotationPresent(Configuration.class)) {
                        for (Method method : loopClass.getMethods()) {
                            if (method.isAnnotationPresent(Bean.class)) {
                                SingletonBeanFactory.addBean(method.getName(), method.invoke(this));
                            }
                        }
                    }
                } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });

            fqcnList.forEach(fqcn -> {
                try {
                    Map<String, Map<String, Method>> requestMapping = (Map<String, Map<String, Method>>) SingletonBeanFactory.getBean("requestMapping");
                    Class<?> loopClass = Class.forName(fqcn);
                    if (loopClass.isAnnotationPresent(Controller.class)) {
                        SingletonBeanFactory.addBean(loopClass.getName(), loopClass.newInstance());
                        for (Method method : loopClass.getMethods()) {
                            if (method.isAnnotationPresent(Get.class)) {
                                requestMapping.get("GET").put(method.getAnnotation(Get.class).value(), method);
                            } else if (method.isAnnotationPresent(Post.class)) {
                                requestMapping.get("POST").put(method.getAnnotation(Post.class).value(), method);
                            }
                        }
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createNecessaryDirectories() {
        try {
            if (directoryIsAbsent(templatePath)) {
                Files.createDirectories(templatePath);
            }
            if (directoryIsAbsent(wwwPath)) {
                Files.createDirectories(wwwPath);
            }
            if (directoryIsAbsent(logPath)) {
                Files.createDirectories(logPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean directoryIsAbsent(Path directory) {
        return !(Files.exists(directory) && Files.isDirectory(directory));
    }

    private void createNecessaryFiles() {
        try {
            if (fileIsAbsent(welcomePage)) {
                createWelcomePage(welcomePage);
            }
            if (fileIsAbsent(loggingPage)) {
                loggingPage.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    @Bean
    public Map<String, Map<String, Method>> requestMapping() {
        Map<String, Map<String, Method>> requestMapping = new HashMap<>();
        requestMapping.put("GET", new HashMap<>());
        requestMapping.put("POST", new HashMap<>());
        return requestMapping;
    }

}
