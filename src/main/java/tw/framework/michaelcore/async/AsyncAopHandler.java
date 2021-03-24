package tw.framework.michaelcore.async;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import tw.framework.michaelcore.data.TransactionalAopHandler;
import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.components.AopHandler;

@AopHandler
public class AsyncAopHandler {

    @Autowired
    private CoreContext coreContext;

    @SuppressWarnings("unchecked")
    public CompletableFuture<Object> invokeAsync(Object proxy, Method method, Object[] args, List<Object> aopHandlers) {
        return CompletableFuture.supplyAsync(() -> {
            Object returningObject = null;
            try {
                executeHandlersSpecificMethod("before", aopHandlers, args);
                returningObject = method.invoke(coreContext.getRealBean(proxy), args);
                if (returningObject != null) {
                    returningObject = ((CompletableFuture<Object>) returningObject).get();
                }
                Collections.reverse(aopHandlers);
                executeHandlersSpecificMethod("after", aopHandlers, returningObject);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                if (needToRollback(throwable.getCause())) {
                    TransactionalAopHandler.setRollback();
                }
            }
            return returningObject;
        });
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
