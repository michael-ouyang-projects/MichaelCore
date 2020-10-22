package demo.aop;

import tw.framework.michaelcore.aop.MichaelCoreAopHandler;
import tw.framework.michaelcore.aop.annotation.AopHandler;

@AopHandler
public class TestCtrlAop extends MichaelCoreAopHandler {

    @Override
    public void before() {
        System.out.println("In Controller!");
    }

    @Override
    public void after() {
        System.out.println("Out Controller!");
    }

}
