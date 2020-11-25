package tw.framework.michaelcore.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.cglib.proxy.InvocationHandler;
import tw.framework.michaelcore.aop.annotation.After;
import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.aop.annotation.Before;
import tw.framework.michaelcore.async.AsyncAop;
import tw.framework.michaelcore.async.annotation.Async;
import tw.framework.michaelcore.core.CoreContext;
import tw.framework.michaelcore.data.TransactionalAop;
import tw.framework.michaelcore.data.annotation.Transactional;

@AopHandler
public class MichaelCoreAopHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> clazz = getRealClassByProxy(proxy);
        List<Object> aopHandlers = new ArrayList<>();
        dealWithTransactional(clazz, method, aopHandlers);
        dealWithAopHere(clazz, method, aopHandlers);

        if (asyncOnClassOrMethod(clazz, method)) {
            return CoreContext.getBean(AsyncAop.class).invokeAsync(aopHandlers, method, args);
        } else {
            return invokeSync(clazz, method, args, aopHandlers);
        }
    }

    private Class<?> getRealClassByProxy(Object proxy) throws Exception {
        return Class.forName(proxy.getClass().getName().split("\\$\\$EnhancerByCGLIB\\$\\$")[0]);
    }

    private void dealWithTransactional(Class<?> clazz, Method method, List<Object> aopHandlers) {
        if (transactionalOnClassOrMethod(clazz, method)) {
            addTransactionalAopToAopHandlers(aopHandlers);
        }
    }

    private boolean transactionalOnClassOrMethod(Class<?> clazz, Method method) {
        return clazz.isAnnotationPresent(Transactional.class) || method.isAnnotationPresent(Transactional.class);
    }

    private void addTransactionalAopToAopHandlers(List<Object> aopHandlers) {
        aopHandlers.add(CoreContext.getBean(TransactionalAop.class));
    }

    private void dealWithAopHere(Class<?> clazz, Method method, List<Object> aopHandlers) {
        if (clazz.isAnnotationPresent(AopHere.class)) {
            aopHandlers.add(CoreContext.getBean(clazz.getAnnotation(AopHere.class).value()));
        }
        if (method.isAnnotationPresent(AopHere.class)) {
            aopHandlers.add(CoreContext.getBean(method.getAnnotation(AopHere.class).value()));
        }
    }

    private boolean asyncOnClassOrMethod(Class<?> clazz, Method method) {
        return clazz.isAnnotationPresent(Async.class) || method.isAnnotationPresent(Async.class);
    }

    private Object invokeSync(Class<?> clazz, Method method, Object[] args, List<Object> aopHandlers) throws Exception {
        executeAopMethodsWithSpecifiedAnnotation(aopHandlers, Before.class);
        Object returningObject = method.invoke(CoreContext.getRealBean(clazz), args);
        Collections.reverse(aopHandlers);
        executeAopMethodsWithSpecifiedAnnotation(aopHandlers, After.class);
        return returningObject;
    }

    private void executeAopMethodsWithSpecifiedAnnotation(List<Object> aopHandlers, Class<? extends Annotation> specifiedAnnotation) throws Exception {
        for (Object handler : aopHandlers) {
            for (Method handlerMethod : handler.getClass().getMethods()) {
                if (handlerMethod.isAnnotationPresent(specifiedAnnotation)) {
                    handlerMethod.invoke(handler);
                }
            }
        }
    }

}