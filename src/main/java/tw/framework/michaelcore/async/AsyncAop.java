package tw.framework.michaelcore.async;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import tw.framework.michaelcore.aop.annotation.After;
import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.Before;
import tw.framework.michaelcore.ioc.CoreContext;

@AopHandler
public class AsyncAop {

    @SuppressWarnings("unchecked")
    public CompletableFuture<Object> invokeAsync(Class<?> clazz, Method method, Object[] args, List<Object> aopHandlers) {
        return CompletableFuture.supplyAsync(() -> {
            Object returningObject = null;
            try {
                executeMethodsWithSpecifiedAnnotation(aopHandlers, Before.class);
                returningObject = method.invoke(CoreContext.getRealBean(clazz), args);
                if (returningObject != null) {
                    returningObject = ((CompletableFuture<Object>) returningObject).get();
                }
                Collections.reverse(aopHandlers);
                executeMethodsWithSpecifiedAnnotation(aopHandlers, After.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return returningObject;
        });
    }

    private void executeMethodsWithSpecifiedAnnotation(List<Object> aopHandlers, Class<? extends Annotation> specifiedAnnotation) throws Exception {
        for (Object handler : aopHandlers) {
            for (Method handlerMethod : handler.getClass().getMethods()) {
                if (handlerMethod.isAnnotationPresent(specifiedAnnotation)) {
                    handlerMethod.invoke(handler);
                }
            }
        }
    }

}
