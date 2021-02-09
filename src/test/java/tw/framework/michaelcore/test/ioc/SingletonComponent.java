package tw.framework.michaelcore.test.ioc;

import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Value;
import tw.framework.michaelcore.ioc.annotation.components.Component;

@Component(value = "singletonComponent")
public class SingletonComponent {

    @Value
    private String test;

    @Autowired
    private String hello;

    @Autowired(name = "prototypeBean")
    private TestBean testBean;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getHello() {
        return hello;
    }

    public void setHello(String hello) {
        this.hello = hello;
    }

    public TestBean getTestBean() {
        return testBean;
    }

    public void setTestBean(TestBean testBean) {
        this.testBean = testBean;
    }

}
