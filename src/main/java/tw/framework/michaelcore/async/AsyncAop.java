package tw.framework.michaelcore.async;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import tw.framework.michaelcore.aop.annotation.After;
import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.Before;
import tw.framework.michaelcore.core.CoreContext;

@AopHandler
public class AsyncAop {

    @SuppressWarnings("unchecked")
    public CompletableFuture<Object> runAsync(List<Object> aopHandlers, Method method, Object[] args) {
        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
            Object returningObject = null;
            try {
                for (int i = 1; i < aopHandlers.size(); i++) {
                    Class<?> handlerClass = aopHandlers.get(i).getClass();
                    for (Method handlerMethod : handlerClass.getMethods()) {
                        if (handlerMethod.isAnnotationPresent(Before.class)) {
                            handlerMethod.invoke(aopHandlers.get(i));
                        }
                    }
                }
                returningObject = method.invoke(CoreContext.getBean(method.getDeclaringClass().getName() + ".real"), args);
                for (int i = aopHandlers.size() - 1; i >= 1; i--) {
                    Class<?> handlerClass = aopHandlers.get(i).getClass();
                    for (Method handlerMethod : handlerClass.getMethods()) {
                        if (handlerMethod.isAnnotationPresent(After.class)) {
                            handlerMethod.invoke(aopHandlers.get(i));
                        }
                    }
                }
                returningObject = ((CompletableFuture<Object>) returningObject).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return returningObject;
        });
        return future;
    }

}
