package tw.framework.michaelcore.core;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tw.framework.michaelcore.aop.MichaelCoreAopHandler;
import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.aop.annotation.AopInterface;
import tw.framework.michaelcore.core.annotation.Configuration;
import tw.framework.michaelcore.core.annotation.ExecuteAfterContainerStartup;
import tw.framework.michaelcore.ioc.Component;
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

    static {
        try {
            List<String> properties = Files.readAllLines(Paths.get("resources/application.properties"));
            properties.forEach(property -> {
                String[] keyValue = property.split("=");
                CoreContext.addProperties(keyValue[0], keyValue[1]);
            });
        } catch (Exception e) {
            System.err.println("Error while processing application.properties!");
        }
    }

    public static void start() {
        initializeMichaelCore();
        CoreContext.getBean(MvcCore.class.getName(), MvcCore.class).startServer();
    }

    private static void initializeMichaelCore() {
        try {
            List<String> fqcns = getFqcns();
            initializeIoC(fqcns);
            initializeAOP(fqcns);
            initializeAutowired(fqcns);
            initializeValueProperties(fqcns);
            initializeRequestMapping(fqcns);
            runStartupCode(fqcns);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> getFqcns() throws IOException {
        String applicationPath = getApplicationPath();
        return Files.walk(Paths.get(applicationPath))
                .filter(Files::isRegularFile)
                .filter(file -> {
                    return notInDefaultPackage(file, applicationPath) && isClassFile(file);
                }).map(classFile -> {
                    return toFqcn(classFile, applicationPath);
                }).collect(Collectors.toList());
    }

    private static String getApplicationPath() {
        String applicationPath = Core.class.getResource("/").getPath();
        boolean isWindowsSystem = Boolean.parseBoolean(CoreContext.getProperties("isWindowsSystem"));
        return isWindowsSystem ? applicationPath.substring(1) : applicationPath;
    }

    private static boolean notInDefaultPackage(Path file, String applicationPath) {
        return file.getParent().toString().replaceAll("\\\\", "/").split(applicationPath).length == 2;
    }

    private static boolean isClassFile(Path file) {
        return file.getFileName().toString().endsWith(".class");
    }

    private static String toFqcn(Path classFile, String applicationPath) {
        String packageName = classFile.getParent().toString().replaceAll("\\\\", "/").split(applicationPath)[1].replace("/", ".");
        String className = classFile.getFileName().toString().split("\\.class")[0];
        return String.format("%s.%s", packageName, className);
    }

    private static Class<?> getClassByFqcn(String fqcn) throws Exception {
        return Class.forName(fqcn);
    }

    private static void initializeIoC(List<String> fqcns) throws Exception {
        for (String fqcn : fqcns) {
            Class<?> clazz = getClassByFqcn(fqcn);
            if (isConfigurationClass(clazz)) {
                processConfigurationClass(clazz);
            } else if (Component.isComponentClass(clazz)) {
                processComponentClass(clazz);
            }
        }
    }

    private static boolean isConfigurationClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Configuration.class);
    }

    private static void processConfigurationClass(Class<?> clazz) throws Exception {
        Object instance = addBeanToContainer(clazz);
        for (Method method : clazz.getMethods()) {
            if (isBeanMethod(method)) {
                addBeanToContainer(method, instance);
            }
        }
    }

    private static void processComponentClass(Class<?> clazz) throws Exception {
        Object instance = addBeanToContainer(clazz);
        for (Class<?> implementedInterface : clazz.getInterfaces()) {
            CoreContext.addBean(implementedInterface.getName(), instance);
        }
    }

    private static Object addBeanToContainer(Class<?> clazz) throws Exception {
        return CoreContext.addBean(clazz.getName(), clazz.newInstance());
    }

    private static Object addBeanToContainer(Method method, Object instance) throws Exception {
        return CoreContext.addBean(method.getName(), method.invoke(instance));
    }

    private static boolean isBeanMethod(Method method) {
        return method.isAnnotationPresent(Bean.class);
    }

    private static void initializeAOP(List<String> fqcns) throws Exception {
        for (String fqcn : fqcns) {
            Class<?> clazz = getClassByFqcn(fqcn);
            if (needToCreateProxy(clazz)) {
                Object proxy = createProxy(clazz);
                CoreContext.addAopProxyBean(clazz.getName(), proxy);
                processInterfaces(clazz, proxy);
            }
        }
    }

    private static boolean needToCreateProxy(Class<?> clazz) {
        if (clazz.isAnnotationPresent(AopInterface.class)) {
            if (aopOnClass(clazz) || aopOnMethod(clazz)) {
                return true;
            }
        }
        return false;
    }

    private static boolean aopOnClass(Class<?> clazz) {
        if (clazz.isAnnotationPresent(AopHere.class)) {
            return true;
        }
        return false;
    }

    private static boolean aopOnMethod(Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(AopHere.class)) {
                return true;
            }
        }
        return false;
    }

    private static Object createProxy(Class<?> clazz) {
        Class<?> aopIterface = getAopInterface(clazz);
        InvocationHandler aopHandler = getAopHandler(clazz);
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {aopIterface }, aopHandler);
    }

    private static Class<?> getAopInterface(Class<?> clazz) {
        return clazz.getAnnotation(AopInterface.class).value();
    }

    private static InvocationHandler getAopHandler(Class<?> clazz) {
        return CoreContext.getBean(MichaelCoreAopHandler.class.getName(), InvocationHandler.class);
    }

    private static void processInterfaces(Class<?> clazz, Object proxy) {
        for (Class<?> interfazz : clazz.getInterfaces()) {
            CoreContext.addAopProxyBean(interfazz.getName(), proxy);
        }
    }

    private static void initializeAutowired(List<String> fqcns) throws Exception {
        for (String fqcn : fqcns) {
            Class<?> clazz = getClassByFqcn(fqcn);
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
        }
    }

    private static void initializeValueProperties(List<String> fqcns) throws Exception {
        for (String fqcn : fqcns) {
            Class<?> clazz = getClassByFqcn(fqcn);
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
        }
    }

    private static void initializeRequestMapping(List<String> fqcns) throws Exception {
        for (String fqcn : fqcns) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Method>> requestMapping = (Map<String, Map<String, Method>>) CoreContext.getBean("requestMapping");
            Class<?> clazz = getClassByFqcn(fqcn);
            if (clazz.isAnnotationPresent(Controller.class)) {
                for (Method method : clazz.getMethods()) {
                    if (method.isAnnotationPresent(Get.class)) {
                        requestMapping.get("GET").put(method.getAnnotation(Get.class).value(), method);
                    } else if (method.isAnnotationPresent(Post.class)) {
                        requestMapping.get("POST").put(method.getAnnotation(Post.class).value(), method);
                    }
                }
            }
        }
    }

    private static void runStartupCode(List<String> fqcns) throws Exception {
        for (String fqcn : fqcns) {
            Class<?> clazz = getClassByFqcn(fqcn);
            if (isConfigurationClass(clazz)) {
                for (Method method : clazz.getMethods()) {
                    if (method.isAnnotationPresent(ExecuteAfterContainerStartup.class)) {
                        method.invoke(CoreContext.getBean(clazz.getName()));
                    }
                }
            }
        }
    }

}
