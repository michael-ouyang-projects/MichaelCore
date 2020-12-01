package demo.test;

import java.util.Date;

import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Bean;
import tw.framework.michaelcore.ioc.annotation.Configuration;
import tw.framework.michaelcore.ioc.annotation.ExecuteAfterContextStartup;
import tw.framework.michaelcore.ioc.enumeration.BeanScope;

@Configuration
public class TestingEntry {

    @Bean(value = "testBeanSingleton", scope = BeanScope.SINGLETON)
    public TestBean testBeanSingleton() {
        TestBean testBean = new TestBean();
        testBean.setName("Singleton TestBean");
        return testBean;
    }

    @Bean(value = "testBeanPrototype", scope = BeanScope.PROTOTYPE)
    public TestBean testBeanPrototype() {
        return new TestBean();
    }

    @ExecuteAfterContextStartup(order = 1)
    public void testIoC() throws InterruptedException {
        TestBean bean1 = CoreContext.getBean("testBeanPrototype", TestBean.class);
        TestBean bean2 = CoreContext.getBean("testBeanPrototype", TestBean.class);
        bean1.setName("first");
        bean2.setName("second");
        if (bean1 == bean2) {
            System.out.println("same bean!");
        } else {
            System.out.println("different bean!");
        }
        System.out.println(String.format("Bean1: %s, Bean2: %s\n", bean1.getName(), bean2.getName()));

        TestComponent component1 = CoreContext.getBean("testComponent", TestComponent.class);
        TestComponent component2 = CoreContext.getBean("testComponent", TestComponent.class);
        component1.setDate(new Date());
        Thread.sleep(1000);
        component2.setDate(new Date());
        if (component1 == component2) {
            System.out.println("same component!");
        } else {
            System.out.println("different component!");
        }
        component1.showInfo();
        component2.showInfo();
        System.out.println();
    }

    @ExecuteAfterContextStartup(order = 2)
    public void testAop() throws Exception {
        TestComponent testComponent = CoreContext.getBean("testComponent", TestComponent.class);
        System.out.println(testComponent.getClass().getName());
        testComponent.sayHello("Bob");
    }

}
