package tw.framework.michaelcore.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.ioc.CoreContext;

@AopHandler
public class MichaelCoreAopHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object returningObject = null;
        MichaelCoreAopHandler aopHandler = null;
        Class<?> realClass = getRealClass(method);
        if (aopOnClass(realClass)) {
            aopHandler = CoreContext.getBean(realClass.getAnnotation(AopHere.class).value().getName(), MichaelCoreAopHandler.class);
            aopHandler.before();
        } else {
            for (Method realMethod : realClass.getMethods()) {
                if (aopOnMethod(realMethod, method)) {
                    aopHandler = CoreContext.getBean(realMethod.getAnnotation(AopHere.class).value().getName(), MichaelCoreAopHandler.class);
                    aopHandler.before();
                    break;
                }
            }
        }
        returningObject = method.invoke(CoreContext.getBean(method.getDeclaringClass().getName() + ".real"), args);
        if (aopHandler != null) {
            aopHandler.after();
        }
        return returningObject;
    }

    private Class<?> getRealClass(Method method) {
        return CoreContext.getBean(method.getDeclaringClass().getName() + ".real").getClass();
    }

    private boolean aopOnClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(AopHere.class);
    }

    private boolean aopOnMethod(Method realMethod, Method method) {
        return realMethod.getName().equals(method.getName()) && realMethod.isAnnotationPresent(AopHere.class);
    }

    public void before() {
    }

    public void after() {
    }

}
