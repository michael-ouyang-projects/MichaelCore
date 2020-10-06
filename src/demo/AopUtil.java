package demo;

import tw.framework.michaelcore.aop.annotation.Aspect;
import tw.framework.michaelcore.aop.annotation.Before;

@Aspect
public class AopUtil {

    @Before(ErrorPageHandlingAop.class)
    public void errorPageHandling() {
        System.out.println("Catch by AOP!");
    }

}
