package tw.framework.michaelcore.test.ioc;

import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Value;
import tw.framework.michaelcore.ioc.annotation.components.Component;
import tw.framework.michaelcore.ioc.enumeration.BeanScope;

@Component(value = "prototypeComponent", scope = BeanScope.PROTOTYPE)
public class PrototypeComponent {

    @Value("test")
    private String test;

    @Autowired(name = "singletonBean")
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
