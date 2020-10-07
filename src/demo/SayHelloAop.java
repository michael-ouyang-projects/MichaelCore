package demo;

import tw.framework.michaelcore.aop.MichaelCoreAopHandler;
import tw.framework.michaelcore.aop.annotation.AopHandler;

@AopHandler
public class SayHelloAop extends MichaelCoreAopHandler {

    @Override
    public void before() {
        System.out.println("Hello AOP!");
    }

    @Override
    public void after() {
        System.out.println("GoodBye AOP!");
    }

}
