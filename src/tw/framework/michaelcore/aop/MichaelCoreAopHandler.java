package tw.framework.michaelcore.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.ioc.SingletonBeanFactory;

public class MichaelCoreAopHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object returningObject = null;
        if(method.getDeclaringClass().isInterface() || method.getDeclaringClass().isAnnotationPresent(AopHere.class) || method.isAnnotationPresent(AopHere.class)) {
            before();
        }
        returningObject = method.invoke(SingletonBeanFactory.getBean(method.getDeclaringClass().getName() + ".real"), args);
        if(method.getDeclaringClass().isInterface() || method.getDeclaringClass().isAnnotationPresent(AopHere.class) || method.isAnnotationPresent(AopHere.class)) {
            after();
        }
        return returningObject;
    }

    public void before() {
    }

    public void after() {
    }

}
