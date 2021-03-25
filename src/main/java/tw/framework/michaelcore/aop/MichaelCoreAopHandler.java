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
import tw.framework.michaelcore.data.orm.OrmAopHandler;
import tw.framework.michaelcore.data.orm.OrmData;
import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.components.AopHandler;
import tw.framework.michaelcore.ioc.annotation.components.OrmRepository;

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
        processOrm(proxy, method, aopHandlers);

        if (method.isAnnotationPresent(Async.class) || clazz.isAnnotationPresent(Async.class)) {
            return ((AsyncAopHandler) coreContext.getBean(AsyncAopHandler.class.getName())).invokeAsync(proxy, method, args, aopHandlers);
        }
        return invokeSync(proxy, method, args, aopHandlers);
    }

    private void processTransactional(Method method, Class<?> clazz, List<Object> aopHandlers) {
        if (method.isAnnotationPresent(Transactional.class)) {
            TransactionalAopHandler transactionalAopHandler = attachTransactionDataToThread(method.getAnnotation(Transactional.class), aopHandlers);
            aopHandlers.add(transactionalAopHandler);
        } else if (clazz.isAnnotationPresent(Transactional.class)) {
            TransactionalAopHandler transactionalAopHandler = attachTransactionDataToThread(clazz.getAnnotation(Transactional.class), aopHandlers);
            aopHandlers.add(transactionalAopHandler);
        }
    }

    private TransactionalAopHandler attachTransactionDataToThread(Transactional transactional, List<Object> aopHandlers) {
        TransactionalAopHandler transactionalAopHandler = (TransactionalAopHandler) coreContext.getBean(TransactionalAopHandler.class.getName());
        transactionalAopHandler.attachNewTransactionDataToThread(new TransactionalData(transactional.propagation(), transactional.isolation(), transactional.rollbackFor()));
        return transactionalAopHandler;
    }

    private void processAopHere(Method method, Class<?> clazz, List<Object> aopHandlers) {
        if (clazz.isAnnotationPresent(AopHere.class)) {
            aopHandlers.add(coreContext.getBean(clazz.getAnnotation(AopHere.class).value().getName()));
        }
        if (method.isAnnotationPresent(AopHere.class)) {
            aopHandlers.add(coreContext.getBean(method.getAnnotation(AopHere.class).value().getName()));
        }
    }

    private void processOrm(Object proxy, Method method, List<Object> aopHandlers) throws ClassNotFoundException {
        Class<?> ormRepositoryClazz = Class.forName(proxy.getClass().getName().split("\\$\\$EnhancerByCGLIB\\$\\$")[0]);
        if (ormRepositoryClazz.isAnnotationPresent(OrmRepository.class)) {
            OrmAopHandler ormAopHandler = (OrmAopHandler) coreContext.getBean(OrmAopHandler.class.getName());
            ormAopHandler.attachNewOrmDataToThread(new OrmData(method, ormRepositoryClazz.getAnnotation(OrmRepository.class)));
            aopHandlers.add(ormAopHandler);
        }
    }

    private Object invokeSync(Object proxy, Method method, Object[] args, List<Object> aopHandlers) throws Exception {
        Object returningObject = null;
        try {
            executeHandlersSpecificMethod("before", aopHandlers, args);
            returningObject = method.invoke(coreContext.getRealBean(proxy), args);
            Collections.reverse(aopHandlers);
            Object handlerReturningObject = executeHandlersSpecificMethod("after", aopHandlers, returningObject);
            if (handlerReturningObject != null) {
                returningObject = handlerReturningObject;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            if (needToRollback(throwable.getCause())) {
                TransactionalAopHandler.setRollback();
            }
        }
        return returningObject;
    }

    private Object executeHandlersSpecificMethod(String specificMethod, List<Object> aopHandlers, Object... args) throws Exception {
        Object handlerReturningObject = null;
        for (Object handler : aopHandlers) {
            for (Method method : handler.getClass().getMethods()) {
                if (specificMethod.equals(method.getName())) {
                    if (method.getParameterCount() == 0) {
                        handlerReturningObject = method.invoke(handler);
                    } else {
                        handlerReturningObject = method.invoke(handler, args);
                    }
                }
            }
        }
        return handlerReturningObject;
    }

    private boolean needToRollback(Throwable throwable) {
        return throwable instanceof RuntimeException || throwable instanceof Error || TransactionalAopHandler.getRollbackFor().isInstance(throwable);
    }

}
