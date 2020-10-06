package demo;

import tw.framework.ouyang.aop.annotation.Aspect;
import tw.framework.ouyang.aop.annotation.Before;

@Aspect
public class AopUtil {

    @Before(ErrorPageHandlingAop.class)
    public void errorPageHandling() {
        System.out.println("Catch by AOP!");
    }

}
