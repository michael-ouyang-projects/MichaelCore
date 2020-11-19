package tw.framework.michaelcore.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
        boolean isSync = true;
        Object result = null;
        String className = proxy.getClass().getName().split("\\$\\$EnhancerByCGLIB\\$\\$")[0];
        Class<?> realClass = Class.forName(className);
        List<Object> aopHandlers = new ArrayList<>();

        if (asyncOnClass(realClass)) {
            aopHandlers.add(CoreContext.getBean(AsyncAop.class));
            isSync = false;
        } else {
            for (Method realMethod : realClass.getMethods()) {
                if (asyncOnMethod(realMethod, method)) {
                    aopHandlers.add(CoreContext.getBean(AsyncAop.class));
                    isSync = false;
                    break;
                }
            }
        }

        if (transactionalOnClass(realClass)) {
            aopHandlers.add(CoreContext.getBean(TransactionalAop.class));
        } else {
            for (Method realMethod : realClass.getMethods()) {
                if (transactionalOnMethod(realMethod, method)) {
                    aopHandlers.add(CoreContext.getBean(TransactionalAop.class));
                    break;
                }
            }
        }

        if (aopOnClass(realClass)) {
            aopHandlers.add(CoreContext.getBean(realClass.getAnnotation(AopHere.class).value()));
        }
        for (Method realMethod : realClass.getMethods()) {
            if (aopOnMethod(realMethod, method)) {
                aopHandlers.add(CoreContext.getBean(realMethod.getAnnotation(AopHere.class).value()));
                break;
            }
        }

        if (isSync) {
            for (int i = 0; i < aopHandlers.size(); i++) {
                Class<?> handlerClass = aopHandlers.get(i).getClass();
                for (Method handlerMethod : handlerClass.getMethods()) {
                    if (handlerMethod.isAnnotationPresent(Before.class)) {
                        handlerMethod.invoke(aopHandlers.get(i));
                    }
                }
            }
            result = method.invoke(CoreContext.getRealBean(className), args);
            for (int i = aopHandlers.size() - 1; i >= 0; i--) {
                Class<?> handlerClass = aopHandlers.get(i).getClass();
                for (Method handlerMethod : handlerClass.getMethods()) {
                    if (handlerMethod.isAnnotationPresent(After.class)) {
                        handlerMethod.invoke(aopHandlers.get(i));
                    }
                }
            }
        } else {
            Class<?> handlerClass = aopHandlers.get(0).getClass();
            for (Method handlerMethod : handlerClass.getMethods()) {
                if ("runAsync".equals(handlerMethod.getName())) {
                    result = handlerMethod.invoke(aopHandlers.get(0), aopHandlers, method, args);
                }
            }
        }

        return result;
    }

    private boolean asyncOnClass(Class<?> realClass) {
        return realClass.isAnnotationPresent(Async.class);
    }

    private boolean transactionalOnClass(Class<?> realClass) {
        return realClass.isAnnotationPresent(Transactional.class);
    }

    private boolean aopOnClass(Class<?> realClass) {
        return realClass.isAnnotationPresent(AopHere.class);
    }

    private boolean asyncOnMethod(Method realMethod, Method method) {
        return realMethod.isAnnotationPresent(Async.class) && realMethod.getName().equals(method.getName());
    }

    private boolean transactionalOnMethod(Method realMethod, Method method) {
        return realMethod.isAnnotationPresent(Transactional.class) && realMethod.getName().equals(method.getName());
    }

    private boolean aopOnMethod(Method realMethod, Method method) {
        return realMethod.isAnnotationPresent(AopHere.class) && realMethod.getName().equals(method.getName());
    }

}
