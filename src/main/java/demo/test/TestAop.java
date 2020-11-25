package demo.test;

import tw.framework.michaelcore.aop.annotation.After;
import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.Before;

@AopHandler
public class TestAop {

    @Before
    public void before() {
        System.out.println("This is TestAop!");
    }

    @After
    public void after() {
        System.out.println("Bye Bye TestAop!");
    }

}
