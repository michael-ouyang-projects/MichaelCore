package tw.framework.michaelcore.core;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.cglib.proxy.Enhancer;
import tw.framework.michaelcore.aop.MichaelCoreAopHandler;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.async.annotation.Async;
import tw.framework.michaelcore.core.annotation.Configuration;
import tw.framework.michaelcore.core.annotation.ExecuteAfterContainerStartup;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.ioc.BeanScope;
import tw.framework.michaelcore.ioc.Components;
import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Bean;
import tw.framework.michaelcore.ioc.annotation.Component;
import tw.framework.michaelcore.ioc.annotation.Value;
import tw.framework.michaelcore.mvc.MvcCore;

public class Core {

    static {
        try {
            readPropertiesToContainer();
            readFqcnsToContainer();
        } catch (Exception e) {
            System.err.println("Core Initial Error!");
            e.printStackTrace();
        }
    }

    public static void start() {
        initializeCore();
        CoreContext.getBean(MvcCore.class).startServer();
    }

    private static void initializeCore() {
        try {
            initializeIoC();
            initializeValueProperties();
            initializeIoCForBean();
            initializeAOP();
            initializeAutowired();
            executeStartupCode();
        } catch (Exception e) {
            System.err.println("initializeCore() Error!");
            e.printStackTrace();
        }
    }

    private static void readPropertiesToContainer() throws IOException {
        List<String> properties = Files.readAllLines(Paths.get("resources/application.properties"));
        properties.forEach(property -> {
            if (property.trim().length() > 0) {
                String[] keyValue = property.split("=");
                CoreContext.addProperties(keyValue[0], keyValue[1]);
            }
        });
    }

    private static void readFqcnsToContainer() throws IOException {
        String applicationPath = getApplicationPath();
        List<String> fqcns = Files.walk(Paths.get(applicationPath))
                .filter(Files::isRegularFile)
                .filter(file -> {
                    return notInDefaultPackage(file, applicationPath) && isClassFile(file);
                }).map(classFile -> {
                    return toFqcn(classFile, applicationPath);
                }).collect(Collectors.toList());
        CoreContext.setFqcns(fqcns);
    }

    private static String getApplicationPath() {
        String applicationPath = Core.class.getResource("/").getPath();
        boolean isWindowsSystem = Boolean.parseBoolean(CoreContext.getProperties("isWindowsSystem"));
        return isWindowsSystem ? applicationPath.substring(1) : applicationPath;
    }

    private static boolean notInDefaultPackage(Path file, String applicationPath) {
        return file.getParent().toString().replace("\\", "/").split(applicationPath).length == 2;
    }

    private static boolean isClassFile(Path file) {
        return file.getFileName().toString().endsWith(".class");
    }

    private static String toFqcn(Path classFile, String applicationPath) {
        String packageName = classFile.getParent().toString().replace("\\", "/").split(applicationPath)[1].replace("/", ".");
        String className = classFile.getFileName().toString().split("\\.class")[0];
        return String.format("%s.%s", packageName, className);
    }

    private static Class<?> getClassByFqcn(String fqcn) throws Exception {
        return Class.forName(fqcn);
    }

    private static void initializeIoC() throws Exception {
        for (String fqcn : CoreContext.getFqcns()) {
            Class<?> clazz = getClassByFqcn(fqcn);
            if (isManagedBean(clazz)) {
                processIoC(clazz);
            }
        }
    }

    private static boolean isManagedBean(Class<?> clazz) {
        return isConfigurationClass(clazz) || Components.isComponentClass(clazz);
    }

    private static boolean isConfigurationClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Configuration.class);
    }

    private static void processIoC(Class<?> clazz) throws Exception {
        if (isComponentClass(clazz)) {
            Component component = clazz.getAnnotation(Component.class);
            if (component.scope().equals(BeanScope.PROTOTYPE)) {
                String componentName = getComponentName(component, clazz);
                addConstructorToContainer(componentName, clazz);
            } else if (component.scope().equals(BeanScope.SINGLETON)) {
                addBeanToContainer(clazz);
            }
        } else {
            addBeanToContainer(clazz);
        }
    }

    private static boolean isComponentClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class);
    }

    private static String getComponentName(Component component, Class<?> clazz) {
        String value = component.value();
        if ("".equals(value)) {
            value = clazz.getName();
        }
        return value;
    }

    private static void addBeanToContainer(Class<?> clazz) throws Exception {
        CoreContext.addBean(clazz.getName(), clazz.getDeclaredConstructor().newInstance());
    }

    private static void addConstructorToContainer(String componentName, Class<?> clazz) throws Exception {
        CoreContext.addBean(componentName, clazz.getDeclaredConstructor());
    }

    private static void initializeValueProperties() throws Exception {
        for (String fqcn : CoreContext.getFqcns()) {
            Class<?> clazz = getClassByFqcn(fqcn);
            if (isManagedBean(clazz)) {
                String componentName = clazz.getName();
                if (isComponentClass(clazz)) {
                    Component component = clazz.getAnnotation(Component.class);
                    componentName = getComponentName(component, clazz);
                }
                insertValue(clazz, CoreContext.getBean(componentName));
            }
        }
    }

    private static void insertValue(Class<?> clazz, Object bean) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Value.class)) {
                field.setAccessible(true);
                field.set(bean, CoreContext.getProperties(field.getName()));
            }
        }
    }

    private static void initializeIoCForBean() throws Exception {
        for (String fqcn : CoreContext.getFqcns()) {
            Class<?> clazz = getClassByFqcn(fqcn);
            if (isConfigurationClass(clazz)) {
                processConfigurationClass(clazz);
            }
        }
    }

    private static void processConfigurationClass(Class<?> configurationClazz) throws Exception {
        Object configurationBean = CoreContext.getBean(configurationClazz.getName());
        for (Method method : configurationClazz.getMethods()) {
            if (isBeanMethod(method)) {
                processBeanMethod(method.getAnnotation(Bean.class), method, configurationBean);
            }
        }
    }

    private static boolean isBeanMethod(Method method) {
        return method.isAnnotationPresent(Bean.class);
    }

    private static void processBeanMethod(Bean bean, Method method, Object configurationBean) throws Exception {
        String beanName = getBeanName(bean, method);
        if (bean.scope().equals(BeanScope.SINGLETON)) {
            addBeanToContainer(beanName, method, configurationBean);
        } else if (bean.scope().equals(BeanScope.PROTOTYPE)) {
            addMethodToContainer(beanName, method);
        }
    }

    private static String getBeanName(Bean bean, Method method) {
        String value = bean.value();
        if ("".equals(value)) {
            value = method.getReturnType().getName();
        }
        return value;
    }

    private static void addBeanToContainer(String beanName, Method method, Object configurationBean) throws Exception {
        CoreContext.addBean(beanName, method.invoke(configurationBean));
    }

    private static void addMethodToContainer(String beanName, Method method) throws Exception {
        CoreContext.addBean(beanName, method);
    }

    private static void initializeAOP() throws Exception {
        for (String fqcn : CoreContext.getFqcns()) {
            Class<?> clazz = getClassByFqcn(fqcn);
            if (needToCreateProxy(clazz)) {
                addProxyBeanToContainer(clazz, createProxy(clazz));
            }
        }
    }

    private static boolean needToCreateProxy(Class<?> clazz) {
        return Components.isComponentClass(clazz) && (aopOnClass(clazz) || aopOnMethod(clazz));
    }

    private static boolean aopOnClass(Class<?> clazz) {
        if (clazz.isAnnotationPresent(AopHere.class) || clazz.isAnnotationPresent(Transactional.class) || clazz.isAnnotationPresent(Async.class)) {
            return true;
        }
        return false;
    }

    private static boolean aopOnMethod(Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(AopHere.class) || method.isAnnotationPresent(Transactional.class) || method.isAnnotationPresent(Async.class)) {
                return true;
            }
        }
        return false;
    }

    private static Object createProxy(Class<?> clazz) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(getAopHandler());
        return enhancer.create();
    }

    private static MichaelCoreAopHandler getAopHandler() {
        return CoreContext.getBean(MichaelCoreAopHandler.class);
    }

    private static void addProxyBeanToContainer(Class<?> clazz, Object proxy) throws Exception {
        CoreContext.addProxyBean(clazz.getName(), proxy);
    }

    private static void initializeAutowired() throws Exception {
        for (String fqcn : CoreContext.getFqcns()) {
            Class<?> clazz = getClassByFqcn(fqcn);
            if (isManagedBean(clazz)) {
                if (isComponentClass(clazz)) {
                    Component component = clazz.getAnnotation(Component.class);
                    if (component.scope().equals(BeanScope.PROTOTYPE)) {
                        continue;
                    }
                }
                autowireDependency(clazz, CoreContext.getRealBean(clazz));
            }
        }
    }

    private static void autowireDependency(Class<?> clazz, Object realBean) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                Class<?> autowiredClazz = field.getAnnotation(Autowired.class).value();
                if (autowiredClazz.equals(Object.class)) {
                    field.set(realBean, CoreContext.getBean(field.getType()));
                } else {
                    field.set(realBean, CoreContext.getBean(autowiredClazz.getName()));
                }
            }
        }
    }

    private static void executeStartupCode() throws Exception {
        for (String fqcn : CoreContext.getFqcns()) {
            Class<?> clazz = getClassByFqcn(fqcn);
            if (isConfigurationClass(clazz)) {
                executeMethodWithStartupAnnotation(clazz, CoreContext.getBean(clazz));
            }
        }
    }

    private static void executeMethodWithStartupAnnotation(Class<?> clazz, Object configurationBean) throws Exception {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(ExecuteAfterContainerStartup.class)) {
                method.invoke(configurationBean);
            }
        }
    }

}
