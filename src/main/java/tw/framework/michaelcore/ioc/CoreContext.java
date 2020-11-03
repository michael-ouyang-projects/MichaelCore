package tw.framework.michaelcore.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreContext {

    private static List<String> fqcns;
    private final static Map<String, String> properties = new HashMap<>();
    private final static Map<String, Object> beanFactory = new HashMap<>();

    public static List<String> getFqcns() {
        return fqcns;
    }

    public static void setFqcns(List<String> fqcns) {
        CoreContext.fqcns = fqcns;
    }

    public static String getProperties(String key) {
        return properties.get(key);
    }

    public static void addProperties(String key, String value) {
        if (value != null) {
            properties.put(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    public static Object getBean(String name) {
        Object bean = beanFactory.get(name);
        if (isConstructor(bean)) {
            bean = createPrototypeComponentByConstructor((Constructor<Object>) bean);
        } else if (isMethod(bean)) {
            bean = createPrototypeBeanByMethod((Method) bean);
        }
        return bean;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        Object bean = beanFactory.get(clazz.getName());
        if (isConstructor(bean)) {
            bean = createPrototypeComponentByConstructor((Constructor<Object>) bean);
        } else if (isMethod(bean)) {
            bean = createPrototypeBeanByMethod((Method) bean);
        }
        return clazz.cast(bean);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name, Class<T> clazz) {
        Object bean = beanFactory.get(name);
        if (isConstructor(bean)) {
            bean = createPrototypeComponentByConstructor((Constructor<Object>) bean);
        } else if (isMethod(bean)) {
            bean = createPrototypeBeanByMethod((Method) bean);
        }
        return clazz.cast(bean);
    }

    public static Object getRealBean(Class<?> clazz) {
        Object bean = beanFactory.get(clazz.getName());
        if (isProxy(bean)) {
            bean = beanFactory.get(clazz.getName() + ".real");
        }
        return bean;
    }

    private static boolean isConstructor(Object bean) {
        return bean.getClass().equals(Constructor.class);
    }

    private static boolean isMethod(Object bean) {
        return bean.getClass().equals(Method.class);
    }

    private static boolean isProxy(Object bean) {
        return bean.getClass().getName().contains("$$EnhancerByCGLIB$$");
    }

    private static Object createPrototypeComponentByConstructor(Constructor<Object> constructor) {
        Object bean = null;
        try {
            bean = constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }

    private static Object createPrototypeBeanByMethod(Method method) {
        Object bean = null;
        Object configurationClazz = beanFactory.get(method.getDeclaringClass().getName());
        try {
            bean = method.invoke(configurationClazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
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

    public static void addProxyBean(String name, Object object) {
        if (object != null) {
            Object realBean = beanFactory.put(name, object);
            beanFactory.put(name + ".real", realBean);
        }
    }

    public static boolean containsBean(String name) {
        return beanFactory.containsKey(name);
    }

}
