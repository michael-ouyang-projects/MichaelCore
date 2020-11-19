package tw.framework.michaelcore.aop;

import tw.framework.michaelcore.core.CoreContext;

public class AopHelper {

	public static <T> T executeInnerMethodWithAop(Class<T> clazz) {
		return CoreContext.getBean(clazz);
	}
	
}
