package tw.framework.michaelcore.test.aop;

import tw.framework.michaelcore.aop.annotation.After;
import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.Before;

@AopHandler
public class InnerMethodAop {

    @Before
    public void before() {
        System.out.println("This is InnerMethodAop!");
    }

    @After
    public void after() {
        System.out.println("Bye Bye InnerMethodAop!");
    }

}
