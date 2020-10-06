package demo;

import tw.framework.ouyang.aop.annotation.Aspect;
import tw.framework.ouyang.aop.annotation.Pointcut;

@Aspect
public class AopUtil {

    @Pointcut("within(com.ouyang.mvc.controller.*) && @within(org.springframework.stereotype.Controller)")
    public void controllerLayer() {
    }

    @Pointcut("execution(public String *(..))")
    public void publicMethod() {
    }

    @Pointcut("controllerLayer() && publicMethod()")
    public void controllerPublicMethod() {
    }

    // @Around("controllerPublicMethod()")
    // public String processRequest(ProceedingJoinPoint joinPoint) {
    // try {
    // return (String) joinPoint.proceed();
    // } catch (Throwable e) {
    // return "error.html";
    // }
    // }

}
