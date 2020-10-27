package demo.aop;

import tw.framework.michaelcore.aop.annotation.After;
import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.Before;

@AopHandler
public class SayHelloAop {

    @Before
    public void before() {
        System.out.println("Hello POST!");
    }

    @After
    public void after() {
        System.out.println("GoodBye POST!");
    }

}
