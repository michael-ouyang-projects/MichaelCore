package tw.framework.michaelcore.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreContext {

    private static List<Class<?>> fqcnClasses;
    private final static Map<String, String> properties = new HashMap<>();
    private final static Map<String, Object> beanFactory = new HashMap<>();
    private final static Map<String, Object> realBeans = new HashMap<>();

    public static List<Class<?>> getFqcnClasses() {
        return fqcnClasses;
    }

    static void setFqcnClasses(List<Class<?>> fqcns) {
        CoreContext.fqcnClasses = fqcns;
    }

    public static String getProperties(String key) {
        return properties.get(key);
    }

    static void addProperties(String key, String value) {
        if (value != null) {
            properties.put(key, value);
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        return clazz.cast(getBean(clazz.getName()));
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return clazz.cast(getBean(name));
    }

    @SuppressWarnings("unchecked")
    public static Object getBean(String name) {
        Object bean = beanFactory.get(name);
        try {
            if (isConstructor(bean)) {
                bean = getPrototypeComponentByConstructor((Constructor<Object>) bean, name);
            } else if (isMethod(bean)) {
                bean = getPrototypeBeanByMethod((Method) bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }

    private static boolean isConstructor(Object bean) {
        return bean.getClass().equals(Constructor.class);
    }

    private static boolean isMethod(Object bean) {
        return bean.getClass().equals(Method.class);
    }

    private static Object getPrototypeComponentByConstructor(Constructor<Object> constructor, String beanName) throws Exception {
        Object realComponent = constructor.newInstance();
        Class<?> componentClass = realComponent.getClass();
        dealWithDependencies(componentClass, realComponent);
        if (needToCreateProxy(componentClass)) {
            return getProxyAndAddRealBeanToContainer(componentClass, realComponent);
        }
        return realComponent;
    }

    private static void dealWithDependencies(Class<?> clazz, Object realBean) throws Exception {
        Core.insertValueToBean(clazz, realBean);
        Core.autowireDependencies(clazz, realBean);
    }

    private static boolean needToCreateProxy(Class<?> clazz) {
        return Core.aopOnClass(clazz) || Core.aopOnMethod(clazz);
    }

    public static Object getProxyAndAddRealBeanToContainer(Class<?> clazz, Object realBean) {
        Object proxy = Core.createProxy(clazz);
        realBeans.put(proxy.getClass().getName(), realBean);
        return proxy;
    }

    private static Object getPrototypeBeanByMethod(Method method) throws Exception {
        return method.invoke(beanFactory.get(method.getDeclaringClass().getName()));
    }

    public static Object getRealBeanByClass(Class<?> clazz) {
        Object bean = beanFactory.get(Core.getBeanName(clazz));
        return isProxy(bean) ? getRealBeanByProxy(bean) : bean;
    }

    private static boolean isProxy(Object bean) {
        return bean.getClass().getName().contains("$$EnhancerByCGLIB$$");
    }

    public static Object getRealBeanByProxy(Object proxy) {
        return realBeans.get(proxy.getClass().getName());
    }

    public static void addBean(String name, Object object) {
        if (object != null) {
            if (!containsBean(name)) {
                beanFactory.put(name, object);
            } else {
                throw new RuntimeException("Duplicate Bean Name: " + name);
            }
        }
    }

    public static void addProxyBean(String name, Object proxy) {
        Object realBean = beanFactory.put(name, proxy);
        realBeans.put(proxy.getClass().getName(), realBean);
    }

    public static boolean containsBean(String name) {
        return beanFactory.containsKey(name);
    }

}