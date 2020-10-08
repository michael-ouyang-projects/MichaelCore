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
import tw.framework.michaelcore.ioc.SingletonBeanFactory;
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
    public String webRoot;

    @Value
    public String templatePath;

    @Value
    public String welcomePage;

    @Value
    public String loggingPage;

    @Bean
    public Map<String, Map<String, Method>> requestMapping() {
        Map<String, Map<String, Method>> requestMapping = new HashMap<>();
        requestMapping.put("GET", new HashMap<>());
        requestMapping.put("POST", new HashMap<>());
        return requestMapping;
    }

    public static void start() {
        try {
            Map<String, String> propertiesMap = null;
            List<String> properties = Files.readAllLines(Paths.get("resources/application.properties"));
            if (properties.size() > 0) {
                propertiesMap = new HashMap<>();
                for (String property : properties) {
                    String[] keyValue = property.split("=");
                    propertiesMap.put(keyValue[0], keyValue[1]);
                }
            }
            SingletonBeanFactory.addBean("michaelcore.application.properties", propertiesMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initializeContainer();

        Core core = (Core) SingletonBeanFactory.getBean(Core.class.getName());
        core.startServer();
    }

    @SuppressWarnings("unchecked")
    private static void initializeContainer() {
        try {
            Map<String, String> propertiesMap = (Map<String, String>) SingletonBeanFactory.getBean("michaelcore.application.properties");
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
                        Object instance = SingletonBeanFactory.addBean(clazz.getName(), clazz.newInstance());
                        for (Method method : clazz.getMethods()) {
                            if (method.isAnnotationPresent(Bean.class)) {
                                SingletonBeanFactory.addBean(method.getName(), method.invoke(instance));
                            }
                        }
                    }
                    if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class) || clazz.isAnnotationPresent(AopHandler.class)) {
                        if (!SingletonBeanFactory.containsBean(clazz.getName())) {
                            Object instance = SingletonBeanFactory.addBean(clazz.getName(), clazz.newInstance());
                            for(Class<?> implementedInterface : clazz.getInterfaces()) {
                                SingletonBeanFactory.addBean(implementedInterface.getName(), instance);
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
                            InvocationHandler aopHandler = (InvocationHandler) SingletonBeanFactory.getBean(clazz.getAnnotation(AopHere.class).value().getName());
                            Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {aopIterface }, aopHandler);
                            SingletonBeanFactory.addAopProxyBean(clazz.getName(), proxy);
                            for(Class<?> implementedInterface : clazz.getInterfaces()) {
                                SingletonBeanFactory.addAopProxyBean(implementedInterface.getName(), proxy);
                            }
                            continue;
                        }
                        for (Method method : clazz.getMethods()) {
                            if (method.isAnnotationPresent(AopHere.class)) {
                                InvocationHandler aopHandler = (InvocationHandler) SingletonBeanFactory.getBean(method.getAnnotation(AopHere.class).value().getName());
                                Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {aopIterface }, aopHandler);
                                SingletonBeanFactory.addAopProxyBean(clazz.getName(), proxy);
                                for(Class<?> implementedInterface : clazz.getInterfaces()) {
                                    SingletonBeanFactory.addAopProxyBean(implementedInterface.getName(), proxy);
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
                        Object instance = SingletonBeanFactory.getBean(clazz.getName());
                        if (Proxy.isProxyClass(instance.getClass())) {
                            instance = SingletonBeanFactory.getBean(clazz.getName() + ".real");
                        }
                        for (Field field : clazz.getFields()) {
                            if (field.isAnnotationPresent(Autowired.class)) {
                                Object dependencyInstance = SingletonBeanFactory.getBean(field.getType().getName());
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
                        Object instance = SingletonBeanFactory.getBean(clazz.getName());
                        if (Proxy.isProxyClass(instance.getClass())) {
                            instance = SingletonBeanFactory.getBean(clazz.getName() + ".real");
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
                    Map<String, Map<String, Method>> requestMapping = (Map<String, Map<String, Method>>) SingletonBeanFactory.getBean("requestMapping");
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
                                method.invoke(SingletonBeanFactory.getBean(clazz.getName()));
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
                executor.submit(new RequestProcessor(serverSocket.accept(), webRoot, new File(loggingPage)));
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
        Path templatePath = Paths.get(this.templatePath);
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
