package tw.framework.michaelcore.ioc;

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
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.data.orm.OrmAopHandler;
import tw.framework.michaelcore.data.orm.annotation.OrmRepository;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Bean;
import tw.framework.michaelcore.ioc.annotation.Component;
import tw.framework.michaelcore.ioc.annotation.Configuration;
import tw.framework.michaelcore.ioc.annotation.ExecuteAfterContextCreate;
import tw.framework.michaelcore.ioc.annotation.Value;
import tw.framework.michaelcore.ioc.enumeration.BeanScope;
import tw.framework.michaelcore.ioc.enumeration.Components;
import tw.framework.michaelcore.utils.MultiValueTreeMap;

public class Core {

    static {
        try {
            readPropertiesToContainer();
            scanClassesToContainer(isJUnitTest());
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

    private static void scanClassesToContainer(boolean isJUnitTest) throws IOException {
        Path sourceCodeDirectoryPath = getSourceCodeDirectoryPath(isJUnitTest);
        CoreContext.setClasses(Files.walk(sourceCodeDirectoryPath)
                .filter(Core::isClassFile)
                .map(classFilePath -> {
                    return classFilePathToFqcn(classFilePath, sourceCodeDirectoryPath, isJUnitTest);
                }).map(Core::getClassByFqcn).collect(Collectors.toList()));
    }

    private static Path getSourceCodeDirectoryPath(boolean isJUnitTest) throws IOException {
        /* /D:/eclipse-workspace-git/MichaelCore/target/classes/ */
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

    private static String classFilePathToFqcn(Path classFilePath, Path sourceCodeDirectoryPath, boolean isJUnitTest) {
        /* tw/framework/michaelcore/ioc/Core.class */
        String portionClassPath = classFilePath.toString().replace("\\", "/").split(sourceCodeDirectoryPath.toString().replace("\\", "/"))[1].substring(1);
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

    public static CoreContext start() {
        CoreContext coreContext = new CoreContext();
        try {
            initializeIoC(coreContext);
            initializeValueProperties(coreContext);
            initializeIoCForBean(coreContext);
            initializeAOP(coreContext);
            initializeAutowired(coreContext);
            executeStartupCode(coreContext);
            System.out.println("MichaelCore Start!");
        } catch (Exception e) {
            System.err.println("initializeCore() Error!");
            e.printStackTrace();
        }
        return coreContext;
    }

    private static void initializeIoC(CoreContext coreContext) throws Exception {
        for (Class<?> clazz : CoreContext.getClasses()) {
            if (Components.isComponentClass(clazz)) {
                processIoC(coreContext, clazz);
            }
        }
    }

    private static void processIoC(CoreContext coreContext, Class<?> clazz) throws Exception {
        if (clazz.isAnnotationPresent(Component.class)) {
            Component component = clazz.getAnnotation(Component.class);
            String componentName = getComponentName(component, clazz);
            if (component.scope().equals(BeanScope.SINGLETON)) {
                coreContext.addBean(componentName, clazz.getDeclaredConstructor().newInstance());
            } else if (component.scope().equals(BeanScope.PROTOTYPE)) {
                coreContext.addBean(componentName, clazz.getDeclaredConstructor());
            }
        } else {
            coreContext.addBean(clazz.getName(), clazz.getDeclaredConstructor().newInstance());
        }
    }

    static String getComponentName(Component component, Class<?> clazz) {
        return "".equals(component.value()) ? clazz.getName() : component.value();
    }

    private static void initializeValueProperties(CoreContext coreContext) throws Exception {
        for (Class<?> clazz : CoreContext.getClasses()) {
            if (Components.isComponentClass(clazz) && isSingletonBean(clazz)) {
                insertValueToBean(clazz, coreContext.getBean(getBeanName(clazz)));
            }
        }
    }

    private static boolean isSingletonBean(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            return clazz.getAnnotation(Component.class).scope().equals(BeanScope.SINGLETON);
        }
        return true;
    }

    static String getBeanName(Class<?> clazz) {
        String beanName = clazz.getName();
        if (clazz.isAnnotationPresent(Component.class)) {
            beanName = getComponentName(clazz.getAnnotation(Component.class), clazz);
        }
        return beanName;
    }

    static void insertValueToBean(Class<?> clazz, Object bean) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Value.class)) {
                field.setAccessible(true);
                field.set(bean, CoreContext.getProperty(field.getName()));
            }
        }
    }

    private static void initializeIoCForBean(CoreContext coreContext) throws Exception {
        for (Class<?> clazz : CoreContext.getClasses()) {
            if (clazz.isAnnotationPresent(Configuration.class)) {
                processConfigurationClass(coreContext, clazz);
            }
        }
    }

    private static void processConfigurationClass(CoreContext coreContext, Class<?> configurationClazz) throws Exception {
        Object configurationBean = coreContext.getBean(configurationClazz.getName());
        for (Method method : configurationClazz.getMethods()) {
            if (method.isAnnotationPresent(Bean.class)) {
                processBeanMethod(coreContext, method, configurationBean);
            }
        }
    }

    private static void processBeanMethod(CoreContext coreContext, Method method, Object configurationBean) throws Exception {
        Bean bean = method.getAnnotation(Bean.class);
        String beanName = getBeanName(bean, method);
        if (bean.scope().equals(BeanScope.SINGLETON)) {
            coreContext.addBean(beanName, method.invoke(configurationBean));
        } else if (bean.scope().equals(BeanScope.PROTOTYPE)) {
            coreContext.addBean(beanName, method);
        }
    }

    private static String getBeanName(Bean bean, Method method) {
        return "".equals(bean.value()) ? method.getReturnType().getName() : bean.value();
    }

    private static void initializeAOP(CoreContext coreContext) throws Exception {
        for (Class<?> clazz : CoreContext.getClasses()) {
            if (needToCreateProxy(clazz)) {
                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(clazz);
                if (clazz.isAnnotationPresent(OrmRepository.class)) {
                    enhancer.setCallback((OrmAopHandler) coreContext.getBean(OrmAopHandler.class.getName()));
                } else {
                    enhancer.setCallback((MichaelCoreAopHandler) coreContext.getBean(MichaelCoreAopHandler.class.getName()));
                }
                coreContext.addProxyBean(getBeanName(clazz), enhancer.create());
            }
        }
    }

    private static boolean needToCreateProxy(Class<?> clazz) {
        return Components.isComponentClass(clazz) && isSingletonBean(clazz) && (aopOnClass(clazz) || aopOnMethod(clazz));
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

    private static void initializeAutowired(CoreContext coreContext) throws Exception {
        for (Class<?> clazz : CoreContext.getClasses()) {
            if (Components.isComponentClass(clazz) && isSingletonBean(clazz)) {
                autowireDependencies(coreContext, clazz, coreContext.getRealBean(clazz));
            }
        }
    }

    static void autowireDependencies(CoreContext coreContext, Class<?> clazz, Object realBean) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                doAutowired(coreContext, field, realBean);
            }
        }
    }

    private static void doAutowired(CoreContext coreContext, Field field, Object realBean) throws Exception {
        Class<?> autowiredClazz = field.getAnnotation(Autowired.class).value();
        if (autowiredClazz.equals(Object.class)) {
            String beanName = field.getAnnotation(Autowired.class).name();
            if ("".equals(beanName)) {
                field.set(realBean, coreContext.getBean(getBeanName(field.getType())));
            } else {
                field.set(realBean, coreContext.getBean(beanName));
            }
        } else {
            field.set(realBean, coreContext.getBean(getBeanName(autowiredClazz)));
        }
    }

    private static void executeStartupCode(CoreContext coreContext) throws Exception {
        for (Class<?> clazz : CoreContext.getClasses()) {
            if (clazz.isAnnotationPresent(Configuration.class)) {
                MultiValueTreeMap<Integer, Method> map = collectExecuteAfterContextCreateMethods(clazz);
                executeExecuteAfterContextCreateMethodsByOrder(map, coreContext.getBean(clazz.getName()));
            }
        }
    }

    private static MultiValueTreeMap<Integer, Method> collectExecuteAfterContextCreateMethods(Class<?> configurationClazz) throws Exception {
        MultiValueTreeMap<Integer, Method> map = null;
        for (Method method : configurationClazz.getMethods()) {
            if (method.isAnnotationPresent(ExecuteAfterContextCreate.class)) {
                if (map == null) {
                    map = new MultiValueTreeMap<>();
                }
                map.put(method.getAnnotation(ExecuteAfterContextCreate.class).order(), method);
            }
        }
        return map;
    }

    private static void executeExecuteAfterContextCreateMethodsByOrder(MultiValueTreeMap<Integer, Method> map, Object configurationBean) throws Exception {
        if (map != null) {
            for (Method method : map.getAllByOrder()) {
                method.invoke(configurationBean);
            }
        }
    }

}