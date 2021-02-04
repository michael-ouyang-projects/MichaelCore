package tw.framework.michaelcore.ioc;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import tw.framework.michaelcore.ioc.annotation.ExecuteBeforeContextClose;
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
            initializeProperties(coreContext);
            initializeIoCForBean(coreContext);
            initializeAOP(coreContext);
            initializeAutowired(coreContext);
            executeStartupCode(coreContext);
            System.out.println("== MichaelCore Started Successfully ==");
        } catch (Exception e) {
            System.err.println("!! MichaelCore Started Error !!");
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

    private static void initializeProperties(CoreContext coreContext) throws Exception {
        for (Object bean : coreContext.getBeanFactory().values()) {
            if (!bean.getClass().equals(Constructor.class)) {
                insertPropertiesToBean(bean, bean.getClass());
            }
        }
    }

    static void insertPropertiesToBean(Object bean, Class<?> clazz) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Value.class)) {
                field.setAccessible(true);
                field.set(bean, CoreContext.getProperty(field.getName()));
            }
        }
    }

    private static void initializeIoCForBean(CoreContext coreContext) throws Exception {
        List<Object> configurationBeans = coreContext.getBeanFactory().values()
                .stream()
                .filter(bean -> {
                    return bean.getClass().isAnnotationPresent(Configuration.class);
                }).collect(Collectors.toList());
        for (Object configurationBean : configurationBeans) {
            processConfigurationBean(coreContext, configurationBean);
        }
    }

    private static void processConfigurationBean(CoreContext coreContext, Object configurationBean) throws Exception {
        for (Method method : configurationBean.getClass().getMethods()) {
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
        Map<String, Object> tmpBeanFactory = new HashMap<>();
        for (Entry<String, Object> entry : coreContext.getBeanFactory().entrySet()) {
            Object bean = entry.getValue();
            if (needToCreateProxy(bean.getClass())) {
                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(bean.getClass());
                if (bean.getClass().isAnnotationPresent(OrmRepository.class)) {
                    enhancer.setCallback((OrmAopHandler) coreContext.getBean(OrmAopHandler.class.getName()));
                } else {
                    enhancer.setCallback((MichaelCoreAopHandler) coreContext.getBean(MichaelCoreAopHandler.class.getName()));
                }
                tmpBeanFactory.put(entry.getKey(), enhancer.create());
            }
        }
        tmpBeanFactory.forEach(coreContext::addProxyBean);
    }

    private static boolean needToCreateProxy(Class<?> beanClazz) {
        return !beanClazz.equals(Constructor.class) &&
                !beanClazz.equals(Method.class) &&
                !beanClazz.isAnnotationPresent(Configuration.class) &&
                Components.isComponentClass(beanClazz) &&
                (aopOnClass(beanClazz) || aopOnMethod(beanClazz));
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
        for (String key : coreContext.getBeanFactory().keySet()) {
            Object bean = coreContext.getRealBean(key);
            if (!bean.getClass().equals(Constructor.class) &&
                    !bean.getClass().equals(Method.class) &&
                    Components.isComponentClass(bean.getClass())) {
                autowireDependencies(coreContext, bean);
            }
        }
    }

    static void autowireDependencies(CoreContext coreContext, Object realBean) throws Exception {
        for (Field field : realBean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                doAutowired(coreContext, field, realBean);
            }
        }
    }

    private static void doAutowired(CoreContext coreContext, Field field, Object realBean) throws Exception {
        Class<?> autowiredClazz = field.getAnnotation(Autowired.class).value();
        if (autowiredClazz.equals(Object.class)) {
            String autowiredName = field.getAnnotation(Autowired.class).name();
            if ("".equals(autowiredName)) {
                field.set(realBean, getBean(coreContext, field.getType()));
            } else {
                field.set(realBean, coreContext.getBean(autowiredName));
            }
        } else {
            field.set(realBean, getBean(coreContext, autowiredClazz));
        }
    }

    private static Object getBean(CoreContext coreContext, Class<?> clazz) {
        if (Components.isComponentClass(clazz)) {
            String beanName = clazz.getName();
            if (clazz.isAnnotationPresent(Component.class)) {
                beanName = getComponentName(clazz.getAnnotation(Component.class), clazz);
            }
            return coreContext.getBean(beanName);
        }
        return getThirdPartyBean(coreContext, clazz);
    }

    private static Object getThirdPartyBean(CoreContext coreContext, Class<?> clazz) {
        Object returningBean = null;
        for (Entry<String, Object> entry : coreContext.getBeanFactory().entrySet()) {
            Object bean = entry.getValue();
            if (bean.getClass().equals(clazz) || (bean.getClass().equals(Method.class) && ((Method) bean).getReturnType().equals(clazz))) {
                if (returningBean != null) {
                    throw new RuntimeException("More than one bean with type: " + clazz.getName());
                }
                returningBean = coreContext.getBean(entry.getKey());
            }
        }
        return returningBean;
    }

    private static void executeStartupCode(CoreContext coreContext) throws Exception {
        for (Object bean : coreContext.getBeanFactory().values()) {
            if (bean.getClass().isAnnotationPresent(Configuration.class)) {
                MultiValueTreeMap<Integer, Method> map = collectExecuteAfterContextCreateMethodsByAnnotation(bean);
                executeMethodsByOrder(map, bean);
            }
        }
    }

    private static MultiValueTreeMap<Integer, Method> collectExecuteAfterContextCreateMethodsByAnnotation(Object configurationBean) throws Exception {
        MultiValueTreeMap<Integer, Method> map = null;
        for (Method method : configurationBean.getClass().getMethods()) {
            if (method.isAnnotationPresent(ExecuteAfterContextCreate.class)) {
                if (map == null) {
                    map = new MultiValueTreeMap<>();
                }
                map.put(method.getAnnotation(ExecuteAfterContextCreate.class).order(), method);
            }
        }
        return map;
    }

    private static void executeMethodsByOrder(MultiValueTreeMap<Integer, Method> map, Object configurationBean) throws Exception {
        if (map != null) {
            for (Method method : map.getAllByOrder()) {
                method.invoke(configurationBean);
            }
        }
    }

    public static void stop(CoreContext coreContext) {
        try {
            executeShutdownCode(coreContext);
            coreContext.clearBeanFactory();
            System.out.println("== MichaelCore Stopped Successfully ==");
        } catch (Exception e) {
            System.out.println("!! MichaelCore Stopped Error !!");
            e.printStackTrace();
        }
    }

    static void executeShutdownCode(CoreContext coreContext) throws Exception {
        for (Object bean : coreContext.getBeanFactory().values()) {
            if (bean.getClass().isAnnotationPresent(Configuration.class)) {
                MultiValueTreeMap<Integer, Method> map = collectExecuteBeforeContextCloseMethodsByAnnotation(bean);
                executeMethodsByOrder(map, bean);
            }
        }
    }

    private static MultiValueTreeMap<Integer, Method> collectExecuteBeforeContextCloseMethodsByAnnotation(Object configurationBean) throws Exception {
        MultiValueTreeMap<Integer, Method> map = null;
        for (Method method : configurationBean.getClass().getMethods()) {
            if (method.isAnnotationPresent(ExecuteBeforeContextClose.class)) {
                if (map == null) {
                    map = new MultiValueTreeMap<>();
                }
                map.put(method.getAnnotation(ExecuteBeforeContextClose.class).order(), method);
            }
        }
        return map;
    }

}