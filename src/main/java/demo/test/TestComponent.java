package demo.test;

import java.util.Date;

import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Component;
import tw.framework.michaelcore.ioc.annotation.Value;

@Component(value = "testComponent")
public class TestComponent {

    private Date date;

    @Value
    private String test;

    @Autowired(name = "testBeanSingleton")
    private TestBean testBean;

    public void showInfo() {
        System.out.println(test + ", " + date);
        System.out.println(testBean.getName());
    }

    @AopHere(TestAop.class)
    public void sayHello(String hi) {
        System.out.println("Hello in TestComponent! " + hi);
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
