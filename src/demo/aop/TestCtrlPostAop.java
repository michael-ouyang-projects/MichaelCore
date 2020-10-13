package demo.aop;

import tw.framework.michaelcore.aop.MichaelCoreAopHandler;
import tw.framework.michaelcore.aop.annotation.AopHandler;

@AopHandler
public class TestCtrlPostAop extends MichaelCoreAopHandler {

    @Override
    public void before() {
        System.out.println("I'm POST!");
    }

    @Override
    public void after() {
        System.out.println("88 POST!");
    }

}
