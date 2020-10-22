package demo.aop;

import tw.framework.michaelcore.aop.MichaelCoreAopHandler;
import tw.framework.michaelcore.aop.annotation.AopHandler;

@AopHandler
public class SayHelloAop extends MichaelCoreAopHandler {

    @Override
    public void before() {
        System.out.println("Hello POST!");
    }

    @Override
    public void after() {
        System.out.println("GoodBye POST!");
    }

}
