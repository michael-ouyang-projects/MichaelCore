package tw.framework.michaelcore.ioc;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import tw.framework.michaelcore.aop.MichaelCoreAopHandler;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.async.annotation.Async;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.data.orm.OrmAopHandler;
import tw.framework.michaelcore.data.orm.annotation.OrmRepository;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Bean;
import tw.framework.michaelcore.ioc.annotation.Component;
import tw.framework.michaelcore.ioc.annotation.Configuration;
import tw.framework.michaelcore.ioc.annotation.ExecuteAfterContextStartup;
import tw.framework.michaelcore.ioc.annotation.Value;
import tw.framework.michaelcore.ioc.enumeration.BeanScope;
import tw.framework.michaelcore.ioc.enumeration.Components;
import tw.framework.michaelcore.utils.MultiValueTreeMap;

public class Core {

    static {
        try {
            readPropertiesToContainer();
            readComponentsToContainer(isJUnitTest());
        } catch (Exception e) {
            System.err.println("Core Initial Error!");
            e.printStackTrace();
        }
    }

    private static void readPropertiesToContainer() throws IOException {
        List<String> propertyStrings = Files.readAllLines(Paths.get("resources/application.properties"));
        propertyStrings.forEach(propertyString -> {
            if (propertyString.trim().length() > 0) {
                String[] keyValue = propertyString.split("=");
                CoreContext.addProperty(keyValue[0], keyValue[1]);
            }
        });
    }

    private static boolean isJUnitTest() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.junit")) {
                return true;
            }
        }
        return false;
    }

    private static void readComponentsToContainer(boolean isJUnitTest) throws IOException {
        Path sourceCodeDirectoryPath = getSourceCodeDirectoryPath(isJUnitTest);
        CoreContext.setComponentClasses(Files.walk(sourceCodeDirectoryPath)
                .filter(Core::isClassFile)
                .map(classPath -> {
                    return classPathToFqcn(classPath, sourceCodeDirectoryPath, isJUnitTest);
                }).map(Core::getClassByFqcn).collect(Collectors.toList()));
    }

    private static Path getSourceCodeDirectoryPath(boolean isJUnitTest) throws IOException {
        String sourceCodeDirectoryPath = Core.class.getResource("/").getPath();
        sourceCodeDirectoryPath = System.getProperty("os.name").contains("Windows") ? sourceCodeDirectoryPath.substring(1) : sourceCodeDirectoryPath;
        if (isJUnitTest) {
            return Paths.get(sourceCodeDirectoryPath, "..").toRealPath();
        }
        return Paths.get(sourceCodeDirectoryPath);
    }

    private static boolean isClassFile(Path path) {
        return Files.isRegularFile(path) && path.getFileName().toString().endsWith(".class");
    }

    private static String classPathToFqcn(Path classPath, Path sourceCodeDirectoryPath, boolean isJUnitTest) {
        String portionClassPath = classPath.toString().replace("\\", "/").split(sourceCodeDirectoryPath.toString().replace("\\", "/"))[1].substring(1);
        if (isJUnitTest) {
            return portionClassPath.substring(portionClassPath.indexOf("/") + 1).split(".class")[0].replace("/", ".");
        }
        return portionClassPath.split(".class")[0].replace("/", ".");
    }

    private static Class<?> getClassByFqcn(String fqcn) {
        try {
            return Class.forName(fqcn);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void start() {
        try {
            initializeIoC();
            initializeValueProperties();
            initializeIoCForBean();
            initializeAOP();
            initializeAutowired();
            executeStartupCode();
            System.out.println("MichaelCore Start!");
        } catch (Exception e) {
            System.err.println("initializeCore() Error!");
            e.printStackTrace();
        }
    }

    private static void initializeIoC() throws Exception {
        for (Class<?> clazz : CoreContext.getComponentClasses()) {
            if (isManagedBeanClass(clazz)) {
                processIoC(clazz);
            }
        }
    }

    private static boolean isManagedBeanClass(Class<?> clazz) {
        return isConfigurationClass(clazz) || Components.isComponentsClass(clazz);
    }

    private static boolean isConfigurationClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Configuration.class);
    }

    private static void processIoC(Class<?> clazz) throws Exception {
        if (isComponentClass(clazz)) {
            Component component = clazz.getAnnotation(Component.class);
            processComponentClass(component, getComponentBeanName(component, clazz), clazz);
        } else {
            addBeanToContainer(clazz);
        }
    }

    private static boolean isComponentClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class);
    }

    private static String getComponentBeanName(Component component, Class<?> clazz) {
        return "".equals(component.value()) ? clazz.getName() : component.value();
    }

    private static void processComponentClass(Component component, String componentName, Class<?> clazz) throws Exception {
        if (component.scope().equals(BeanScope.SINGLETON)) {
            addBeanToContainer(componentName, clazz);
        } else if (component.scope().equals(BeanScope.PROTOTYPE)) {
            addConstructorToContainer(componentName, clazz);
        }
    }

    private static void addBeanToContainer(String beanName, Class<?> clazz) throws Exception {
        CoreContext.addBean(beanName, clazz.getDeclaredConstructor().newInstance());
    }

    private static void addConstructorToContainer(String beanName, Class<?> clazz) throws Exception {
        CoreContext.addBean(beanName, clazz.getDeclaredConstructor());
    }

    private static void addBeanToContainer(Class<?> clazz) throws Exception {
        CoreContext.addBean(clazz.getName(), clazz.getDeclaredConstructor().newInstance());
    }

    private static void initializeValueProperties() throws Exception {
        for (Class<?> clazz : CoreContext.getComponentClasses()) {
            if (isManagedBeanClass(clazz) && isSingletonBean(clazz)) {
                insertValueToBean(clazz, CoreContext.getBean(getBeanName(clazz)));
            }
        }
    }

    private static boolean isSingletonBean(Class<?> clazz) {
        if (isComponentClass(clazz)) {
            return clazz.getAnnotation(Component.class).scope().equals(BeanScope.SINGLETON);
        }
        return true;
    }

    public static String getBeanName(Class<?> clazz) {
        String beanName = clazz.getName();
        if (isComponentClass(clazz)) {
            beanName = getComponentBeanName(clazz.getAnnotation(Component.class), clazz);
        }
        return beanName;
    }

    static void insertValueToBean(Class<?> clazz, Object bean) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Value.class)) {
                field.setAccessible(true);
                field.set(bean, CoreContext.getProperties(field.getName()));
            }
        }
    }

    private static void initializeIoCForBean() throws Exception {
        for (Class<?> clazz : CoreContext.getComponentClasses()) {
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
        return "".equals(bean.value()) ? method.getReturnType().getName() : bean.value();
    }

    private static void addBeanToContainer(String beanName, Method method, Object configurationBean) throws Exception {
        CoreContext.addBean(beanName, method.invoke(configurationBean));
    }

    private static void addMethodToContainer(String beanName, Method method) throws Exception {
        CoreContext.addBean(beanName, method);
    }

    private static void initializeAOP() throws Exception {
        for (Class<?> clazz : CoreContext.getComponentClasses()) {
            if (needToCreateProxy(clazz)) {
                if (clazz.isAnnotationPresent(OrmRepository.class)) {
                    addProxyBeanToContainer(clazz, createProxy(clazz, OrmAopHandler.class));
                } else {
                    addProxyBeanToContainer(clazz, createProxy(clazz, MichaelCoreAopHandler.class));
                }
            }
        }
    }

    private static boolean needToCreateProxy(Class<?> clazz) {
        return Components.isComponentsClass(clazz) && isSingletonBean(clazz) && (aopOnClass(clazz) || aopOnMethod(clazz));
    }

    static boolean aopOnClass(Class<?> clazz) {
        if (clazz.isAnnotationPresent(AopHere.class) || clazz.isAnnotationPresent(Transactional.class) || clazz.isAnnotationPresent(Async.class) || clazz.isAnnotationPresent(OrmRepository.class)) {
            return true;
        }
        return false;
    }

    static boolean aopOnMethod(Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(AopHere.class) || method.isAnnotationPresent(Transactional.class) || method.isAnnotationPresent(Async.class)) {
                return true;
            }
        }
        return false;
    }

    static Object createProxy(Class<?> clazz, Class<? extends Callback> aopHandlerClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(CoreContext.getBean(aopHandlerClass));
        return enhancer.create();
    }

    private static void addProxyBeanToContainer(Class<?> clazz, Object proxy) throws Exception {
        CoreContext.addProxyBean(getBeanName(clazz), proxy);
    }

    private static void initializeAutowired() throws Exception {
        for (Class<?> clazz : CoreContext.getComponentClasses()) {
            if (isManagedBeanClass(clazz) && isSingletonBean(clazz)) {
                autowireDependencies(clazz, CoreContext.getRealBeanByClass(clazz));
            }
        }
    }

    static void autowireDependencies(Class<?> clazz, Object realBean) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                doAutowired(field, realBean);
            }
        }
    }

    private static void doAutowired(Field field, Object realBean) throws Exception {
        Class<?> autowiredClazz = field.getAnnotation(Autowired.class).value();
        if (autowiredClazz.equals(Object.class)) {
            String beanName = field.getAnnotation(Autowired.class).name();
            if ("".equals(beanName)) {
                field.set(realBean, CoreContext.getBean(getBeanName(field.getType())));
            } else {
                field.set(realBean, CoreContext.getBean(beanName));
            }
        } else {
            field.set(realBean, CoreContext.getBean(getBeanName(autowiredClazz)));
        }
    }

    private static void executeStartupCode() throws Exception {
        for (Class<?> clazz : CoreContext.getComponentClasses()) {
            if (isConfigurationClass(clazz)) {
                MultiValueTreeMap<Integer, Method> map = collectMethodsWithStartupAnnotation(clazz);
                executeMethodsWithStartupAnnotationByOrder(map, CoreContext.getBean(clazz));
            }
        }
    }

    private static MultiValueTreeMap<Integer, Method> collectMethodsWithStartupAnnotation(Class<?> clazz) throws Exception {
        MultiValueTreeMap<Integer, Method> map = null;
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(ExecuteAfterContextStartup.class)) {
                if (map == null) {
                    map = new MultiValueTreeMap<>();
                }
                map.put(method.getAnnotation(ExecuteAfterContextStartup.class).order(), method);
            }
        }
        return map;
    }

    private static void executeMethodsWithStartupAnnotationByOrder(MultiValueTreeMap<Integer, Method> map, Object configurationBean) throws Exception {
        if (map != null) {
            for (Method method : map.getAllByOrder()) {
                method.invoke(configurationBean);
            }
        }
    }

    public static void clean() {
        CoreContext.clean();
    }

}