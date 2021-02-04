package tw.framework.michaelcore.test.aop;

import tw.framework.michaelcore.aop.annotation.After;
import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.Before;

@AopHandler
public class ParametersAop {

    @Before
    public void before(String userName, String password) {
        System.out.println("In before aop: " + userName + ", " + password);
    }

    @After
    public void after(Integer age) {
        System.out.println("In after aop: " + age);
    }

}
