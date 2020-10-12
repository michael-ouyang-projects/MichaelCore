package tw.framework.michaelcore.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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

import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.aop.annotation.AopInterface;
import tw.framework.michaelcore.core.annotation.Configuration;
import tw.framework.michaelcore.core.annotation.ExecuteAfterContainerStartup;
import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Bean;
import tw.framework.michaelcore.ioc.annotation.Service;
import tw.framework.michaelcore.ioc.annotation.Value;
import tw.framework.michaelcore.mvc.annotation.Controller;
import tw.framework.michaelcore.mvc.annotation.Get;
import tw.framework.michaelcore.mvc.annotation.Post;

@Configuration
public class Core {

    @Value
    public String listeningPort;

    @Value
    public String welcomePage;

    @Value
    public String loggingPage;

    public static void start() {
        readApplicationProperties();
        initializeContainer();
        CoreContext.getBean(Core.class.getName(), Core.class).startServer();
    }

    private static void readApplicationProperties() {
        try {
            List<String> properties = Files.readAllLines(Paths.get("resources/application.properties"));
            Map<String, String> propertyMapping = processProperties(properties);
            CoreContext.addBean("michaelcore.properties", propertyMapping);
        } catch (IOException e) {
            System.err.println("Error while reading application.properties.");
        }
    }

    private static Map<String, String> processProperties(List<String> properties) {
        Map<String, String> propertyMapping = null;
        if (properties.size() > 0) {
            propertyMapping = new HashMap<>();
            for (String property : properties) {
                String[] keyValue = property.split("=");
                propertyMapping.put(keyValue[0], keyValue[1]);
            }
        }
        return propertyMapping;
    }

    @SuppressWarnings("unchecked")
    private static void initializeContainer() {
        try {
            Map<String, String> propertiesMap = CoreContext.getBean("michaelcore.properties", Map.class);
            String applicationPath;
            if (Boolean.parseBoolean(propertiesMap.get("isWindowsSystem"))) {
                applicationPath = Core.class.getResource("/").getPath().substring(1);
            } else {
                applicationPath = Core.class.getResource("/").getPath();
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

            // IoC
            fqcnList.forEach(fqcn -> {
                try {
                    Class<?> clazz = Class.forName(fqcn);
                    if (clazz.isAnnotationPresent(Configuration.class)) {
                        Object instance = CoreContext.addBean(clazz.getName(), clazz.newInstance());
                        for (Method method : clazz.getMethods()) {
                            if (method.isAnnotationPresent(Bean.class)) {
                                CoreContext.addBean(method.getName(), method.invoke(instance));
                            }
                        }
                    }
                    if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class) || clazz.isAnnotationPresent(AopHandler.class)) {
                        if (!CoreContext.containsBean(clazz.getName())) {
                            Object instance = CoreContext.addBean(clazz.getName(), clazz.newInstance());
                            for (Class<?> implementedInterface : clazz.getInterfaces()) {
                                CoreContext.addBean(implementedInterface.getName(), instance);
                            }
                        }
                    }
                } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
                    e.printStackTrace();
                }
            });

            // AOP
            for (String fqcn : fqcnList) {
                try {
                    Class<?> clazz = Class.forName(fqcn);
                    if (clazz.isAnnotationPresent(AopInterface.class)) {
                        Class<?> aopIterface = clazz.getAnnotation(AopInterface.class).value();
                        if (clazz.isAnnotationPresent(AopHere.class)) {
                            InvocationHandler aopHandler = (InvocationHandler) CoreContext.getBean(clazz.getAnnotation(AopHere.class).value().getName());
                            Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {aopIterface }, aopHandler);
                            CoreContext.addAopProxyBean(clazz.getName(), proxy);
                            for (Class<?> implementedInterface : clazz.getInterfaces()) {
                                CoreContext.addAopProxyBean(implementedInterface.getName(), proxy);
                            }
                            continue;
                        }
                        for (Method method : clazz.getMethods()) {
                            if (method.isAnnotationPresent(AopHere.class)) {
                                InvocationHandler aopHandler = (InvocationHandler) CoreContext.getBean(method.getAnnotation(AopHere.class).value().getName());
                                Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {aopIterface }, aopHandler);
                                CoreContext.addAopProxyBean(clazz.getName(), proxy);
                                for (Class<?> implementedInterface : clazz.getInterfaces()) {
                                    CoreContext.addAopProxyBean(implementedInterface.getName(), proxy);
                                }
                                continue;
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            // @Autowired
            fqcnList.forEach(fqcn -> {
                try {
                    Class<?> clazz = Class.forName(fqcn);
                    if (clazz.isAnnotationPresent(Configuration.class) || clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class)
                            || clazz.isAnnotationPresent(AopHandler.class)) {
                        Object instance = CoreContext.getBean(clazz.getName());
                        if (Proxy.isProxyClass(instance.getClass())) {
                            instance = CoreContext.getBean(clazz.getName() + ".real");
                        }
                        for (Field field : clazz.getFields()) {
                            if (field.isAnnotationPresent(Autowired.class)) {
                                Object dependencyInstance = CoreContext.getBean(field.getType().getName());
                                field.set(instance, dependencyInstance);
                            }
                        }
                    }
                } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

            // application.properties to @Value
            fqcnList.forEach(fqcn -> {
                try {
                    Class<?> clazz = Class.forName(fqcn);
                    if (clazz.isAnnotationPresent(Configuration.class) || clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class)
                            || clazz.isAnnotationPresent(AopHandler.class)) {
                        Object instance = CoreContext.getBean(clazz.getName());
                        if (Proxy.isProxyClass(instance.getClass())) {
                            instance = CoreContext.getBean(clazz.getName() + ".real");
                        }
                        for (Field field : clazz.getFields()) {
                            if (field.isAnnotationPresent(Value.class)) {
                                field.set(instance, propertiesMap.get(field.getName()));
                            }
                        }
                    }
                } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

            // Request Mapping
            fqcnList.forEach(fqcn -> {
                try {
                    Map<String, Map<String, Method>> requestMapping = (Map<String, Map<String, Method>>) CoreContext.getBean("requestMapping");
                    Class<?> clazz = Class.forName(fqcn);
                    if (clazz.isAnnotationPresent(Controller.class)) {
                        for (Method method : clazz.getMethods()) {
                            if (method.isAnnotationPresent(Get.class)) {
                                requestMapping.get("GET").put(method.getAnnotation(Get.class).value(), method);
                            } else if (method.isAnnotationPresent(Post.class)) {
                                requestMapping.get("POST").put(method.getAnnotation(Post.class).value(), method);
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });

            // ExecuteAfterContainerStartup
            fqcnList.forEach(fqcn -> {
                try {
                    Class<?> clazz = Class.forName(fqcn);
                    if (clazz.isAnnotationPresent(Configuration.class)) {
                        for (Method method : clazz.getMethods()) {
                            if (method.isAnnotationPresent(ExecuteAfterContainerStartup.class)) {
                                method.invoke(CoreContext.getBean(clazz.getName()));
                            }
                        }
                    }
                } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startServer() {
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
