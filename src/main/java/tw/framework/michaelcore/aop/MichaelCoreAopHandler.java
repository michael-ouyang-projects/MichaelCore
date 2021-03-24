package tw.framework.michaelcore.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.cglib.proxy.InvocationHandler;
import tw.framework.michaelcore.aop.annotation.AopHere;
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
        List<Object> aopHandlers = new ArrayList<>();
        Class<?> clazz = method.getDeclaringClass();
        processTransactional(method, clazz, aopHandlers);
        processAopHere(method, clazz, aopHandlers);

        if (method.isAnnotationPresent(Async.class) || clazz.isAnnotationPresent(Async.class)) {
            return ((AsyncAopHandler) coreContext.getBean(AsyncAopHandler.class.getName())).invokeAsync(proxy, method, args, aopHandlers);
        }
        return invokeSync(proxy, method, args, aopHandlers);
    }

    private void processTransactional(Method method, Class<?> clazz, List<Object> aopHandlers) {
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

    private void processAopHere(Method method, Class<?> clazz, List<Object> aopHandlers) {
        if (clazz.isAnnotationPresent(AopHere.class)) {
            aopHandlers.add(coreContext.getBean(clazz.getAnnotation(AopHere.class).value().getName()));
        }
        if (method.isAnnotationPresent(AopHere.class)) {
            aopHandlers.add(coreContext.getBean(method.getAnnotation(AopHere.class).value().getName()));
        }
    }

    private Object invokeSync(Object proxy, Method method, Object[] args, List<Object> aopHandlers) throws Exception {
        executeHandlersSpecificMethod("before", aopHandlers, args);
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
        executeHandlersSpecificMethod("after", aopHandlers, returningObject);
        return returningObject;
    }

    private void executeHandlersSpecificMethod(String specificMethod, List<Object> aopHandlers, Object... args) throws Exception {
        for (Object handler : aopHandlers) {
            for (Method method : handler.getClass().getMethods()) {
                if (specificMethod.equals(method.getName())) {
                    if (method.getParameterCount() == 0) {
                        method.invoke(handler);
                    } else {
                        method.invoke(handler, args);
                    }
                }
            }
        }
    }

    private boolean needToRollback(Throwable throwable) {
        return throwable instanceof RuntimeException || throwable instanceof Error || TransactionalAopHandler.getRollbackFor().isInstance(throwable);
    }

}