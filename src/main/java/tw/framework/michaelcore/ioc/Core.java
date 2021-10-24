package tw.framework.michaelcore.ioc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

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
			loadProperties();
			componentsScan();
		} catch (Exception e) {
			System.err.println("== Failed to initialize MichaelCore ==");
			e.printStackTrace();
		}
	}

	private static void loadProperties() throws IOException {
		try (InputStream input = new FileInputStream("src/main/resources/application.properties")) {
			CoreContainer.loadProperties(input);
		}
	}

	private static void componentsScan() throws IOException {
		Set<Class<?>> classes = ClassPath.from(ClassLoader.getSystemClassLoader()).getAllClasses().stream()
				.filter(classInfo -> classInfo.getPackageName().startsWith("tw.framework.michaelcore")
						|| classInfo.getPackageName().startsWith("demo"))
				.map(ClassInfo::load)
				.filter(Components::isComponentClass)
				.collect(Collectors.toSet());
		CoreContainer.setComponentClasses(classes);
		System.out.println(CoreContainer.getComponentClasses().size());
	}

	public static CoreContainer start() {
		CoreContainer container = new CoreContainer();
		try {
			initializeIoC(container);
			initializeProperties(container);
			initializeIoCForBean(container);
			initializeAOP(container);
			initializeAutowired(container);
			executeStartupCode(container);
			System.out.println("== MichaelCore initialized Successfully ==");
		} catch (Exception e) {
			System.err.println("== Failed to initialize MichaelCore ==");
			e.printStackTrace();
		}
		return container;
	}

	private static void initializeIoC(CoreContainer coreContext) throws Exception {
		for (Class<?> clazz : CoreContainer.getComponentClasses()) {
			processIoC(coreContext, clazz);
		}
	}

	private static void processIoC(CoreContainer coreContext, Class<?> clazz) throws Exception {
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

	private static void initializeProperties(CoreContainer coreContext) throws Exception {
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
				field.set(bean, CoreContainer.getProperty(field.getAnnotation(Value.class).value()));
			}
		}
	}

	private static void initializeIoCForBean(CoreContainer coreContext) throws Exception {
		List<Object> configurationBeans = coreContext.getBeanFactory().values().stream().filter(bean -> {
			return bean.getClass().isAnnotationPresent(Configuration.class);
		}).collect(Collectors.toList());
		for (Object configurationBean : configurationBeans) {
			processConfigurationBean(coreContext, configurationBean);
		}
	}

	private static void processConfigurationBean(CoreContainer coreContext, Object configurationBean) throws Exception {
		for (Method method : configurationBean.getClass().getMethods()) {
			if (method.isAnnotationPresent(Bean.class)) {
				processBeanMethod(coreContext, method, configurationBean);
			}
		}
	}

	private static void processBeanMethod(CoreContainer coreContext, Method method, Object configurationBean)
			throws Exception {
		Bean bean = method.getAnnotation(Bean.class);
		String beanName = "".equals(bean.value()) ? method.getReturnType().getName() : bean.value();
		if (bean.scope().equals(BeanScope.SINGLETON)) {
			coreContext.addBean(beanName, method.invoke(configurationBean));
		} else if (bean.scope().equals(BeanScope.PROTOTYPE)) {
			coreContext.addBean(beanName, method);
		}
	}

	private static void initializeAOP(CoreContainer coreContext) throws Exception {
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
		return !beanClazz.equals(Constructor.class) && !beanClazz.equals(Method.class)
				&& !beanClazz.isAnnotationPresent(Configuration.class) && Components.isComponentClass(beanClazz)
				&& (aopOnClass(beanClazz) || aopOnMethod(beanClazz));
	}

	static boolean aopOnClass(Class<?> clazz) {
		if (clazz.isAnnotationPresent(AopHere.class) || clazz.isAnnotationPresent(Transactional.class)
				|| clazz.isAnnotationPresent(Async.class) || clazz.isAnnotationPresent(OrmRepository.class)) {
			return true;
		}
		return false;
	}

	static boolean aopOnMethod(Class<?> clazz) {
		for (Method method : clazz.getMethods()) {
			if (method.isAnnotationPresent(AopHere.class) || method.isAnnotationPresent(Transactional.class)
					|| method.isAnnotationPresent(Async.class)) {
				return true;
			}
		}
		return false;
	}

	private static void initializeAutowired(CoreContainer coreContext) throws Exception {
		for (String key : coreContext.getBeanFactory().keySet()) {
			Object bean = coreContext.getRealBean(key);
			if (!bean.getClass().equals(Constructor.class) && !bean.getClass().equals(Method.class)
					&& Components.isComponentClass(bean.getClass())) {
				autowireDependencies(coreContext, bean);
			}
		}
	}

	static void autowireDependencies(CoreContainer coreContext, Object realBean) throws Exception {
		for (Field field : realBean.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Autowired.class)) {
				field.setAccessible(true);
				doAutowired(coreContext, field, realBean);
			}
		}
	}

	private static void doAutowired(CoreContainer coreContext, Field field, Object realBean) throws Exception {
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

	private static Object getBeanByType(CoreContainer coreContext, Class<?> clazz) {
		if (Components.isComponentClass(clazz)) {
			String beanName = clazz.getName();
			if (clazz.isAnnotationPresent(Component.class)) {
				beanName = getComponentName(clazz.getAnnotation(Component.class), clazz);
			}
			return coreContext.getBean(beanName);
		}
		return getThirdPartyBean(coreContext, clazz);
	}

	private static Object getThirdPartyBean(CoreContainer coreContext, Class<?> clazz) {
		Object returningBean = null;
		for (Entry<String, Object> entry : coreContext.getBeanFactory().entrySet()) {
			Object bean = entry.getValue();
			if (bean.getClass().equals(clazz)
					|| (bean.getClass().equals(Method.class) && ((Method) bean).getReturnType().equals(clazz))) {
				if (returningBean != null) {
					throw new RuntimeException(
							String.format("More than one bean with type: %s, please specify bean name during autowire.",
									clazz.getName()));
				}
				returningBean = coreContext.getBean(entry.getKey());
			}
		}
		return returningBean;
	}

	private static void executeStartupCode(CoreContainer coreContext) throws Exception {
		for (Object bean : coreContext.getBeanFactory().values()) {
			if (bean.getClass().isAnnotationPresent(Configuration.class)) {
				MultiValueTreeMap<Integer, Method> map = collectExecuteAfterContextCreateMethods(bean);
				executeMethodsByOrder(map, bean);
			}
		}
	}

	private static MultiValueTreeMap<Integer, Method> collectExecuteAfterContextCreateMethods(Object configurationBean)
			throws Exception {
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

	private static void executeMethodsByOrder(MultiValueTreeMap<Integer, Method> map, Object configurationBean)
			throws Exception {
		if (map != null) {
			for (Method method : map.getAllByOrder()) {
				method.invoke(configurationBean);
			}
		}
	}

	public static void stop(CoreContainer coreContext) {
		try {
			executeShutdownCode(coreContext);
			coreContext.clearBeanFactory();
			System.out.println("== MichaelCore Stopped Successfully ==");
		} catch (Exception e) {
			System.out.println("!! MichaelCore Failed To Stop !!");
			e.printStackTrace();
		}
	}

	static void executeShutdownCode(CoreContainer coreContext) throws Exception {
		for (Object bean : coreContext.getBeanFactory().values()) {
			if (bean.getClass().isAnnotationPresent(Configuration.class)) {
				MultiValueTreeMap<Integer, Method> map = collectExecuteBeforeContextDestroyMethods(bean);
				executeMethodsByOrder(map, bean);
			}
		}
	}

	private static MultiValueTreeMap<Integer, Method> collectExecuteBeforeContextDestroyMethods(
			Object configurationBean) throws Exception {
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