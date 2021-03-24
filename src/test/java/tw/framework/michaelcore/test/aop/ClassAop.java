package tw.framework.michaelcore.test.aop;

import tw.framework.michaelcore.ioc.annotation.components.AopHandler;

@AopHandler
public class ClassAop {

    public void before() {
        System.out.println("This is ClassAop!");
    }

    public void after() {
        System.out.println("Bye Bye ClassAop!");
    }

}
