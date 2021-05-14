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
import net.sf.cglib.proxy.InvocationHandler;
import tw.framework.michaelcore.aop.MichaelCoreAopHandler;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.async.annotation.Async;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Bean;
import tw.framework.michaelcore.ioc.annotation.ExecuteAfterContextCreate;
import tw.framework.michaelcore.ioc.annotation.ExecuteBeforeContextDestroy;
import tw.framework.michaelcore.ioc.annotation.Value;
import tw.framework.michaelcore.ioc.annotation.components.Component;
import tw.framework.michaelcore.ioc.annotation.components.Configuration;
import tw.framework.michaelcore.ioc.annotation.components.OrmRepository;
import tw.framework.michaelcore.ioc.enumeration.BeanScope;
import tw.framework.michaelcore.ioc.enumeration.Components;
import tw.framework.michaelcore.utils.MultiValueTreeMap;

public class Core {

    static {
        try {
            readApplicationPropertiesToContainer();
            scanClassesToContainer(isJUnitTest());
        } catch (Exception e) {
            System.err.println("!! Core Initial Error !!");
            e.printStackTrace();
        }
    }

    private static void readApplicationPropertiesToContainer() throws IOException {
        List<String> properties = Files.readAllLines(Paths.get("src/main/resources/application.properties"));
        properties.forEach(property -> {
            if (property.trim().length() > 0) {
                String[] keyValue = property.split("=");
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
        Path sourceCodePath = getSourceCodePath(isJUnitTest);
        CoreContext.setClasses(Files.walk(sourceCodePath)
        		.filter(Core::isClassFile)
                .map(classFile -> classFileToFqcn(classFile, sourceCodePath, isJUnitTest))
                .map(Core::fqcnToClass).collect(Collectors.toList()));
    }

    private static Path getSourceCodePath(boolean isJUnitTest) throws IOException {
        /* /D:/eclipse-workspace-git/MichaelCore/target/classes/ */
        String sourceCodePath = Core.class.getResource("/").getPath();
        sourceCodePath = System.getProperty("os.name").contains("Windows") ? sourceCodePath.substring(1) : sourceCodePath;
        if (isJUnitTest) {
            return Paths.get(sourceCodePath, "..").toRealPath();
        }
        return Paths.get(sourceCodePath);
    }

    private static boolean isClassFile(Path path) {
        return Files.isRegularFile(path) && path.getFileName().toString().endsWith(".class");
    }

    private static String classFileToFqcn(Path classFile, Path sourceCodePath, boolean isJUnitTest) {
        /* tw/framework/michaelcore/ioc/Core.class */
    	String classPath = classFile.toString().replace("\\", "/").split(sourceCodePath.toString().replace("\\", "/"))[1].substring(1);
    	if (isJUnitTest) {
            return classPath.substring(classPath.indexOf("/") + 1).split(".class")[0].replace("/", ".");
        }
        return classPath.split(".class")[0].replace("/", ".");
    }

    private static Class<?> fqcnToClass(String fqcn) {
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
            System.err.println("!! MichaelCore Failed To Start !!");
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
                insertPropertiesToBean(bean);
            }
        }
    }

    static void insertPropertiesToBean(Object bean) throws Exception {
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Value.class)) {
                field.setAccessible(true);
                field.set(bean, CoreContext.getProperty(field.getAnnotation(Value.class).value()));
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
        String beanName = "".equals(bean.value()) ? method.getReturnType().getName() : bean.value();
        if (bean.scope().equals(BeanScope.SINGLETON)) {
            coreContext.addBean(beanName, method.invoke(configurationBean));
        } else if (bean.scope().equals(BeanScope.PROTOTYPE)) {
            coreContext.addBean(beanName, method);
        }
    }

    private static void initializeAOP(CoreContext coreContext) throws Exception {
        Map<String, Object> tmpBeanFactory = new HashMap<>();
        for (Entry<String, Object> entry : coreContext.getBeanFactory().entrySet()) {
            Object bean = entry.getValue();
            if (needToCreateProxy(bean.getClass())) {
                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(bean.getClass());
                enhancer.setCallback((InvocationHandler) coreContext.getBean(MichaelCoreAopHandler.class.getName()));
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
                field.set(realBean, getBeanByType(coreContext, field.getType()));
            } else {
                field.set(realBean, coreContext.getBean(autowiredName));
            }
        } else {
            field.set(realBean, getBeanByType(coreContext, autowiredClazz));
        }
    }

    private static Object getBeanByType(CoreContext coreContext, Class<?> clazz) {
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
                    throw new RuntimeException(String.format("More than one bean with type: %s, please specify bean name during autowire.", clazz.getName()));
                }
                returningBean = coreContext.getBean(entry.getKey());
            }
        }
        return returningBean;
    }

    private static void executeStartupCode(CoreContext coreContext) throws Exception {
        for (Object bean : coreContext.getBeanFactory().values()) {
            if (bean.getClass().isAnnotationPresent(Configuration.class)) {
                MultiValueTreeMap<Integer, Method> map = collectExecuteAfterContextCreateMethods(bean);
                executeMethodsByOrder(map, bean);
            }
        }
    }

    private static MultiValueTreeMap<Integer, Method> collectExecuteAfterContextCreateMethods(Object configurationBean) throws Exception {
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
            System.out.println("!! MichaelCore Failed To Stop !!");
            e.printStackTrace();
        }
    }

    static void executeShutdownCode(CoreContext coreContext) throws Exception {
        for (Object bean : coreContext.getBeanFactory().values()) {
            if (bean.getClass().isAnnotationPresent(Configuration.class)) {
                MultiValueTreeMap<Integer, Method> map = collectExecuteBeforeContextDestroyMethods(bean);
                executeMethodsByOrder(map, bean);
            }
        }
    }

    private static MultiValueTreeMap<Integer, Method> collectExecuteBeforeContextDestroyMethods(Object configurationBean) throws Exception {
        MultiValueTreeMap<Integer, Method> map = null;
        for (Method method : configurationBean.getClass().getMethods()) {
            if (method.isAnnotationPresent(ExecuteBeforeContextDestroy.class)) {
                if (map == null) {
                    map = new MultiValueTreeMap<>();
                }
                map.put(method.getAnnotation(ExecuteBeforeContextDestroy.class).order(), method);
            }
        }
        return map;
    }

}