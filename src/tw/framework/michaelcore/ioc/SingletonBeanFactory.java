package tw.framework.michaelcore.ioc;

import java.util.HashMap;
import java.util.Map;

public class SingletonBeanFactory {

    private final static Map<String, Object> beanFactory = new HashMap<>();

    public static Object addBean(String name, Object object) {
        beanFactory.put(name, object);
        return getBean(name);
    }

    public static Object getBean(String name) {
        return beanFactory.get(name);
    }

    public static boolean containsBean(String name) {
        return beanFactory.containsKey(name);
    }

}
