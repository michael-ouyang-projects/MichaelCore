package tw.framework.michaelcore.data;

import tw.framework.michaelcore.aop.MichaelCoreAopHandler;
import tw.framework.michaelcore.aop.annotation.AopHandler;

@AopHandler
public class TransactionalAop extends MichaelCoreAopHandler {

    @Override
    public void before() {
        System.out.println("IN TRANSACTION");
    }

    @Override
    public void after() {
        System.out.println("OUT TRANSACTION");
    }

}
