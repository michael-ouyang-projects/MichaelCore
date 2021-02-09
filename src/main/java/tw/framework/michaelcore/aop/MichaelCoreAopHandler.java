package tw.framework.michaelcore.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.cglib.proxy.InvocationHandler;
import tw.framework.michaelcore.aop.annotation.After;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.aop.annotation.Before;
import tw.framework.michaelcore.async.AsyncAopHandler;
import tw.framework.michaelcore.async.annotation.Async;
import tw.framework.michaelcore.data.TransactionalAopHandler;
import tw.framework.michaelcore.data.TransactionalData;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.components.AopHandler;

@AopHandler
public class MichaelCoreAopHandler implements InvocationHandler {

    @Autowired
    private CoreContext coreContext;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
        Class<?> clazz = method.getDeclaringClass();
        List<Object> aopHandlers = new ArrayList<>();
        processTransactional(clazz, method, aopHandlers);
        processAopHere(clazz, method, aopHandlers);

        if (asyncOnClassOrMethod(clazz, method)) {
            return ((AsyncAopHandler) coreContext.getBean(AsyncAopHandler.class.getName())).invokeAsync(proxy, method, args, aopHandlers);
        }
        return invokeSync(proxy, method, args, aopHandlers);
    }

    private void processTransactional(Class<?> clazz, Method method, List<Object> aopHandlers) {
        if (method.isAnnotationPresent(Transactional.class)) {
            addTransactionDataAndHandler(method.getAnnotation(Transactional.class), aopHandlers);
        } else if (clazz.isAnnotationPresent(Transactional.class)) {
            addTransactionDataAndHandler(clazz.getAnnotation(Transactional.class), aopHandlers);
        }
    }

    private void addTransactionDataAndHandler(Transactional transactional, List<Object> aopHandlers) {
        TransactionalAopHandler transactionalAopHandler = (TransactionalAopHandler) coreContext.getBean(TransactionalAopHandler.class.getName());
        transactionalAopHandler.addNewTransactionData(new TransactionalData(transactional.propagation(), transactional.isolation(), transactional.rollbackFor()));
        aopHandlers.add(transactionalAopHandler);
    }

    private void processAopHere(Class<?> clazz, Method method, List<Object> aopHandlers) {
        if (clazz.isAnnotationPresent(AopHere.class)) {
            aopHandlers.add(coreContext.getBean(clazz.getAnnotation(AopHere.class).value().getName()));
        }
        if (method.isAnnotationPresent(AopHere.class)) {
            aopHandlers.add(coreContext.getBean(method.getAnnotation(AopHere.class).value().getName()));
        }
    }

    private boolean asyncOnClassOrMethod(Class<?> clazz, Method method) {
        return clazz.isAnnotationPresent(Async.class) || method.isAnnotationPresent(Async.class);
    }

    private Object invokeSync(Object proxy, Method method, Object[] args, List<Object> aopHandlers) throws Exception {
        executeBeforeMethods(aopHandlers, args);
        Object returningObject = null;
        try {
            returningObject = method.invoke(coreContext.getRealBean(proxy), args);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            if (needToRollback(throwable.getCause())) {
                TransactionalAopHandler.setRollback();
            }
        }
        Collections.reverse(aopHandlers);
        executeAfterMethods(aopHandlers, returningObject);
        return returningObject;
    }

    private void executeBeforeMethods(List<Object> aopHandlers, Object[] args) throws Exception {
        for (Object handler : aopHandlers) {
            for (Method method : handler.getClass().getMethods()) {
                if (method.isAnnotationPresent(Before.class)) {
                    if (method.getParameterCount() == 0) {
                        method.invoke(handler);
                    } else {
                        method.invoke(handler, args);
                    }
                }
            }
        }
    }

    private void executeAfterMethods(List<Object> aopHandlers, Object arg) throws Exception {
        for (Object handler : aopHandlers) {
            for (Method method : handler.getClass().getMethods()) {
                if (method.isAnnotationPresent(After.class)) {
                    if (method.getParameterCount() == 0) {
                        method.invoke(handler);
                    } else {
                        method.invoke(handler, arg);
                    }
                }
            }
        }
    }

    private boolean needToRollback(Throwable throwable) {
        return throwable instanceof RuntimeException || throwable instanceof Error || TransactionalAopHandler.getRollbackFor().isInstance(throwable);
    }

}