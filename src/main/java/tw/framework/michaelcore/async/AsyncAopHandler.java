package tw.framework.michaelcore.async;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import tw.framework.michaelcore.aop.annotation.After;
import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.Before;
import tw.framework.michaelcore.data.TransactionalAopHandler;
import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Autowired;

@AopHandler
public class AsyncAopHandler {

    @Autowired
    private CoreContext coreContext;

    @SuppressWarnings("unchecked")
    public CompletableFuture<Object> invokeAsync(Object proxy, Method method, Object[] args, List<Object> aopHandlers) {
        return CompletableFuture.supplyAsync(() -> {
            Object returningObject = null;
            try {
                executeMethodsWithSpecifiedAnnotation(aopHandlers, Before.class);
                returningObject = method.invoke(coreContext.getRealBean(proxy), args);
                if (returningObject != null) {
                    returningObject = ((CompletableFuture<Object>) returningObject).get();
                }
                Collections.reverse(aopHandlers);
                executeMethodsWithSpecifiedAnnotation(aopHandlers, After.class);
            } catch (Throwable throwable) {
                if (needToRollback(throwable.getCause())) {
                    TransactionalAopHandler.setRollback();
                }
            }
            return returningObject;
        });
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

    private boolean needToRollback(Throwable throwable) {
        return throwable instanceof RuntimeException || throwable instanceof Error || TransactionalAopHandler.getRollbackFor().isInstance(throwable);
    }

}
