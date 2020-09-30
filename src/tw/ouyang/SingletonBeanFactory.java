package tw.ouyang;

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

}
