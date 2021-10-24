package tw.framework.michaelcore.ioc;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
import tw.framework.michaelcore.aop.MichaelCoreAopHandler;
import tw.framework.michaelcore.ioc.annotation.components.Component;

public class CoreContainer {

	private static Properties properties = new Properties();
	private static Set<Class<?>> componentClasses;

	private Map<String, Object> beanFactory = new HashMap<>();
	private Map<String, Object> realBeanFactory = new HashMap<>();

	static void loadProperties(InputStream inputStream) throws IOException {
		properties.load(inputStream);
	}

	public static Object getProperty(String key) {
		return properties.get(key);
	}

	static void setComponentClasses(Set<Class<?>> clazzes) {
		componentClasses = clazzes;
	}

	public static Set<Class<?>> getComponentClasses() {
		return componentClasses;
	}

	public CoreContainer() {
		beanFactory.put(CoreContainer.class.getName(), this);
	}

	public void addBean(String name, Object object) {
		if (!beanFactory.containsKey(name)) {
			beanFactory.put(name, object);
		} else {
			throw new RuntimeException("Duplicate Bean Name: " + name);
		}
	}

	Map<String, Object> getBeanFactory() {
		return beanFactory;
	}

	public void addProxyBean(String name, Object proxy) {
		Object realBean = beanFactory.put(name, proxy);
		realBeanFactory.put(proxy.getClass().getName(), realBean);
	}

	public Object getRealBean(String name) {
		Object bean = beanFactory.get(name);
		boolean isProxy = bean.getClass().getName().contains("$$EnhancerByCGLIB$$");
		return isProxy ? getRealBean(bean) : bean;
	}

	public Object getRealBean(Object proxy) {
		return realBeanFactory.get(proxy.getClass().getName());
	}

	public <T> T getBean(String name, Class<T> clazz) {
		return clazz.cast(getBean(name));
	}

	@SuppressWarnings("unchecked")
	public Object getBean(String name) {
		Object bean = beanFactory.get(name);
		if (bean == null) {
			return null;
		}
		try {
			if (bean.getClass().equals(Constructor.class)) {
				bean = getPrototypeComponentByConstructor((Constructor<Object>) bean);
			} else if (bean.getClass().equals(Method.class)) {
				Method method = (Method) bean;
				bean = method.invoke(beanFactory.get(method.getDeclaringClass().getName()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bean;
	}

	private Object getPrototypeComponentByConstructor(Constructor<Object> constructor) throws Exception {
		Object component = constructor.newInstance();
		Core.insertPropertiesToBean(component);
		Core.autowireDependencies(this, component);
		if (Core.aopOnClass(component.getClass()) || Core.aopOnMethod(component.getClass())) {
			return getProxyAndAddRealBeanToContainer(component);
		}
		return component;
	}

	private Object getProxyAndAddRealBeanToContainer(Object realBean) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(realBean.getClass());
		enhancer.setCallback((InvocationHandler) getBean(MichaelCoreAopHandler.class.getName()));
		Object proxy = enhancer.create();
		realBeanFactory.put(proxy.getClass().getName(), realBean);
		return proxy;
	}

	public void clearBeanFactory() {
		beanFactory.clear();
		realBeanFactory.clear();
	}

	public <T> T executeInnerMethodWithAop(Class<T> clazz) {
		String beanName = clazz.getName();
		if (clazz.isAnnotationPresent(Component.class)) {
			beanName = Core.getComponentName(clazz.getAnnotation(Component.class), clazz);
		}
		return getBean(beanName, clazz);
	}

}