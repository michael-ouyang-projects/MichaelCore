package demo.test;

import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Component;
import tw.framework.michaelcore.ioc.annotation.Value;

@Component(value = "testComponentSingleton")
public class TestComponentSingleton {

    @Value
    private String test;

    @Autowired(name = "testBeanPrototype")
    private TestBean testBean;

    @AopHere(TestAop.class)
    public void sayHello() {
        System.out.println("Hello in TestComponent!");
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public TestBean getTestBean() {
        return testBean;
    }

    public void setTestBean(TestBean testBean) {
        this.testBean = testBean;
    }

}
