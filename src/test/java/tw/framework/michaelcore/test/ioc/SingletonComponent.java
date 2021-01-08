package tw.framework.michaelcore.test.ioc;

import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Component;
import tw.framework.michaelcore.ioc.annotation.Value;

@Component(value = "singletonComponent")
public class SingletonComponent {

    @Value
    private String test;

    @Autowired(name = "prototypeBean")
    private TestBean testBean;

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
