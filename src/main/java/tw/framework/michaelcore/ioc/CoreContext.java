package tw.framework.michaelcore.ioc;

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

	public static Object getBean(String name) {
		return beanFactory.get(name);
	}

	public static <T> T getBean(Class<T> clazz) {
		Object bean = beanFactory.get(clazz.getName());
		return clazz.cast(bean);
	}

	public static <T> T getBean(String name, Class<T> clazz) {
		Object bean = beanFactory.get(name);
		return clazz.cast(bean);
	}

	public static Object getRealBean(Class<?> clazz) {
		Object bean = beanFactory.get(clazz.getName());
		if (bean.getClass().getName().contains("$$EnhancerByCGLIB$$")) {
			bean = beanFactory.get(clazz.getName() + ".real");
		}
		return bean;
	}

	public static void addBean(String name, Object object) {
		if (object != null) {
			beanFactory.put(name, object);
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
