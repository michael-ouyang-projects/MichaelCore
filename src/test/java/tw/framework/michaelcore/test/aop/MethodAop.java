package tw.framework.michaelcore.test.aop;

import tw.framework.michaelcore.ioc.annotation.components.AopHandler;

@AopHandler
public class MethodAop {

    public void before() {
        System.out.println("This is MethodAop!");
    }

    public void after() {
        System.out.println("Bye Bye MethodAop!");
    }

}
