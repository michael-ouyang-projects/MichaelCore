package tw.framework.michaelcore.core;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
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
import tw.framework.michaelcore.mvc.MvcCore;
import tw.framework.michaelcore.mvc.annotation.Controller;
import tw.framework.michaelcore.mvc.annotation.Get;
import tw.framework.michaelcore.mvc.annotation.Post;

@Configuration
public class Core {

    private static final String applicationPath;

    static {
        readProperties();
        applicationPath = getApplicationPath();
    }

    public static void start() {
        initializeContainer();
        CoreContext.getBean(MvcCore.class.getName(), MvcCore.class).startServer();
    }

    private static void readProperties() {
        try {
            List<String> properties = Files.readAllLines(Paths.get("resources/application.properties"));
            properties.forEach(property -> {
                String[] keyValue = property.split("=");
                CoreContext.addProperties(keyValue[0], keyValue[1]);
            });
        } catch (IOException e) {
            System.err.println("Error while reading application.properties!");
        }
    }

    @SuppressWarnings("unchecked")
    private static void initializeContainer() {
        try {
            List<String> fqcns = getFqcns();

            // IoC
            fqcns.forEach(fqcn -> {
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
            for (String fqcn : fqcns) {
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
            fqcns.forEach(fqcn -> {
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
            fqcns.forEach(fqcn -> {
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
                                field.set(instance, CoreContext.getProperties(field.getName()));
                            }
                        }
                    }
                } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

            // Request Mapping
            fqcns.forEach(fqcn -> {
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
            fqcns.forEach(fqcn -> {
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

    private static String getApplicationPath() {
        String applicationPath = Core.class.getResource("/").getPath();
        boolean isWindowsSystem = Boolean.parseBoolean(CoreContext.getProperties("isWindowsSystem"));
        return isWindowsSystem ? applicationPath.substring(1) : applicationPath;
    }

    private static List<String> getFqcns() throws IOException {
        return Files.walk(Paths.get(applicationPath))
                .filter(Files::isRegularFile)
                .filter(file -> {
                    return notInDefaultPackage(file) && isClassFile(file);
                }).map(classFile -> {
                    return toFqcn(classFile);
                }).collect(Collectors.toList());
    }

    private static boolean notInDefaultPackage(Path file) {
        return file.getParent().toString().replaceAll("\\\\", "/").split(applicationPath).length == 2;
    }

    private static boolean isClassFile(Path file) {
        return file.getFileName().toString().endsWith(".class");
    }

    private static String toFqcn(Path classFile) {
        String packageName = classFile.getParent().toString().replaceAll("\\\\", "/").split(applicationPath)[1].replace("/", ".");
        String className = classFile.getFileName().toString().split("\\.class")[0];
        return String.format("%s.%s", packageName, className);
    }

}
