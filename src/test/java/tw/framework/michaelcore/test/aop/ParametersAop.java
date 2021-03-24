package tw.framework.michaelcore.test.aop;

import tw.framework.michaelcore.ioc.annotation.components.AopHandler;

@AopHandler
public class ParametersAop {

    public void before(String userName, String password) {
        System.out.println("In before aop: " + userName + ", " + password);
    }

    public void after(Integer age) {
        System.out.println("In after aop: " + age);
    }

}
