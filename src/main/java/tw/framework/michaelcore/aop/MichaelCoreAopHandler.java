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
import tw.framework.michaelcore.async.AsyncAopHandler;
import tw.framework.michaelcore.async.annotation.Async;
import tw.framework.michaelcore.data.TransactionData;
import tw.framework.michaelcore.data.TransactionalAopHandler;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.ioc.CoreContext;

@AopHandler
public class MichaelCoreAopHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
        Class<?> clazz = method.getDeclaringClass();
        List<Object> aopHandlers = new ArrayList<>();
        processTransactional(clazz, method, aopHandlers);
        processAopHere(clazz, method, aopHandlers);

        if (asyncOnClassOrMethod(clazz, method)) {
            return CoreContext.getBean(AsyncAopHandler.class).invokeAsync(proxy, method, args, aopHandlers);
        }
        return invokeSync(proxy, method, args, aopHandlers);
    }

    private void processTransactional(Class<?> clazz, Method method, List<Object> aopHandlers) {
        if (transactionalOnMethod(method)) {
            addTransactionDataAndHandler(method.getAnnotation(Transactional.class), aopHandlers);
        } else if (transactionalOnClass(clazz)) {
            addTransactionDataAndHandler(clazz.getAnnotation(Transactional.class), aopHandlers);
        }
    }

    private boolean transactionalOnMethod(Method method) {
        return method.isAnnotationPresent(Transactional.class);
    }

    private boolean transactionalOnClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Transactional.class);
    }

    private void addTransactionDataAndHandler(Transactional transactional, List<Object> aopHandlers) {
        TransactionalAopHandler transactionalAopHandler = CoreContext.getBean(TransactionalAopHandler.class);
        transactionalAopHandler.addNewTransactionData(new TransactionData(transactional.propagation(), transactional.isolation()));
        aopHandlers.add(transactionalAopHandler);
    }

    private void processAopHere(Class<?> clazz, Method method, List<Object> aopHandlers) {
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

    private Object invokeSync(Object proxy, Method method, Object[] args, List<Object> aopHandlers) throws Exception {
        executeMethodsWithSpecifiedAnnotation(aopHandlers, Before.class);
        Object returningObject = null;
        try {
            returningObject = method.invoke(CoreContext.getRealBeanByProxy(proxy), args);
        } catch (Exception e) {
            TransactionalAopHandler.setRollback();
        }
        Collections.reverse(aopHandlers);
        executeMethodsWithSpecifiedAnnotation(aopHandlers, After.class);
        return returningObject;
    }

    private void executeMethodsWithSpecifiedAnnotation(List<Object> aopHandlers, Class<? extends Annotation> annotation) throws Exception {
        for (Object handler : aopHandlers) {
            for (Method method : handler.getClass().getMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    method.invoke(handler);
                }
            }
        }
    }

}