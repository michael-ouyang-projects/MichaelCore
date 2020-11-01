package demo.aop;

import tw.framework.michaelcore.aop.annotation.After;
import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.Before;

@AopHandler
public class TestInnerMethodCallAop {

	@Before
    public void before() {
        System.out.println("In InnerMethod!");
    }

    @After
    public void after() {
        System.out.println("Out InnerMethod!");
    }
	
}
