package tw.framework.michaelcore.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.data.TransactionalAop;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.ioc.CoreContext;

@AopHandler
public class MichaelCoreAopHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        Class<?> realClass = getRealClass(method);
        List<MichaelCoreAopHandler> aopHandlers = new ArrayList<>();

        if (transactionalOnClass(realClass)) {
            aopHandlers.add(CoreContext.getBean(TransactionalAop.class.getName(), MichaelCoreAopHandler.class));
        } else {
            for (Method realMethod : realClass.getMethods()) {
                if (transactionalOnMethod(realMethod, method)) {
                    aopHandlers.add(CoreContext.getBean(TransactionalAop.class.getName(), MichaelCoreAopHandler.class));
                    break;
                }
            }
        }

        if (aopOnClass(realClass)) {
            aopHandlers.add(CoreContext.getBean(realClass.getAnnotation(AopHere.class).value().getName(), MichaelCoreAopHandler.class));
        }
        for (Method realMethod : realClass.getMethods()) {
            if (aopOnMethod(realMethod, method)) {
                aopHandlers.add(CoreContext.getBean(realMethod.getAnnotation(AopHere.class).value().getName(), MichaelCoreAopHandler.class));
                break;
            }
        }

        for (int i = 0; i < aopHandlers.size(); i++) {
            aopHandlers.get(i).before();
        }
        result = method.invoke(CoreContext.getBean(method.getDeclaringClass().getName() + ".real"), args);
        for (int i = aopHandlers.size() - 1; i >= 0; i--) {
            aopHandlers.get(i).after();
        }

        return result;
    }

    private Class<?> getRealClass(Method method) {
        return CoreContext.getBean(method.getDeclaringClass().getName() + ".real").getClass();
    }

    private boolean transactionalOnClass(Class<?> realClass) {
        return realClass.isAnnotationPresent(Transactional.class);
    }

    private boolean aopOnClass(Class<?> realClass) {
        return realClass.isAnnotationPresent(AopHere.class);
    }

    private boolean transactionalOnMethod(Method realMethod, Method method) {
        return realMethod.getName().equals(method.getName()) && realMethod.isAnnotationPresent(Transactional.class);
    }

    private boolean aopOnMethod(Method realMethod, Method method) {
        return realMethod.getName().equals(method.getName()) && realMethod.isAnnotationPresent(AopHere.class);
    }

    public void before() {
    }

    public void after() {
    }

}
