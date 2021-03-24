package tw.framework.michaelcore.test.aop;

import tw.framework.michaelcore.ioc.annotation.components.AopHandler;

@AopHandler
public class InnerMethodAop {

    public void before() {
        System.out.println("This is InnerMethodAop!");
    }

    public void after() {
        System.out.println("Bye Bye InnerMethodAop!");
    }

}
