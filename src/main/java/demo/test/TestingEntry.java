package demo.test;

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
        TestBean testBean = new TestBean();
        testBean.setName("Prototype TestBean");
        return testBean;
    }

    @ExecuteAfterContextStartup(order = 1)
    public void testIoCBean() {
        System.out.print("Test Singleton Bean (same) => ");
        TestBean bean1 = CoreContext.getBean("testBeanSingleton", TestBean.class);
        TestBean bean2 = CoreContext.getBean("testBeanSingleton", TestBean.class);
        if (bean1 == bean2) {
            System.out.println("same bean!");
        } else {
            System.out.println("different bean!");
        }

        System.out.print("Test Prototype Bean (different) => ");
        TestBean bean3 = CoreContext.getBean("testBeanPrototype", TestBean.class);
        TestBean bean4 = CoreContext.getBean("testBeanPrototype", TestBean.class);
        if (bean3 == bean4) {
            System.out.println("same bean!");
        } else {
            System.out.println("different bean!");
        }
    }

    @ExecuteAfterContextStartup(order = 2)
    public void testIoCComponent() throws InterruptedException {
        System.out.print("Test Singleton Component (same) => ");
        TestComponentSingleton component1 = CoreContext.getBean("testComponentSingleton", TestComponentSingleton.class);
        TestComponentSingleton component2 = CoreContext.getBean("testComponentSingleton", TestComponentSingleton.class);
        if (component1 == component2) {
            System.out.println("same component!");
        } else {
            System.out.println("different component!");
        }

        System.out.print("Test Prototype Component (different) => ");
        TestComponentPrototype component3 = CoreContext.getBean("testComponentPrototype", TestComponentPrototype.class);
        TestComponentPrototype component4 = CoreContext.getBean("testComponentPrototype", TestComponentPrototype.class);
        if (component3 == component4) {
            System.out.println("same component!");
        } else {
            System.out.println("different component!");
        }
    }

    @ExecuteAfterContextStartup(order = 3)
    public void testIoCComponentDependencies() throws InterruptedException {
        System.out.print("Test Prototype Dependency (different) => ");
        TestComponentSingleton component1 = CoreContext.getBean("testComponentSingleton", TestComponentSingleton.class);
        TestBean bean1 = component1.getTestBean();
        TestBean bean2 = CoreContext.getBean("testBeanPrototype", TestBean.class);
        if (bean1 == bean2) {
            System.out.println("same dependency bean!");
        } else {
            System.out.println("different dependency bean!");
        }

        System.out.print("Test Singleton Dependency (same) => ");
        TestComponentPrototype component2 = CoreContext.getBean("testComponentPrototype", TestComponentPrototype.class);
        TestBean bean3 = component2.getTestBean();
        TestBean bean4 = CoreContext.getBean("testBeanSingleton", TestBean.class);
        if (bean3 == bean4) {
            System.out.println("same dependency bean!");
        } else {
            System.out.println("different dependency bean!");
        }
        System.out.println();
    }

    @ExecuteAfterContextStartup(order = 4)
    public void testComponentAop() throws Exception {
        System.out.println("Test Singleton Component AOP =>");
        TestComponentSingleton testComponent1 = CoreContext.getBean("testComponentSingleton", TestComponentSingleton.class);
        testComponent1.sayHello();
        System.out.println();

        System.out.println("Test Prototype Component AOP =>");
        TestComponentPrototype testComponent2 = CoreContext.getBean("testComponentPrototype", TestComponentPrototype.class);
        testComponent2.sayHello();
        System.out.println();
    }

    @ExecuteAfterContextStartup(order = 5)
    public void testApplicationProperties() throws Exception {
        System.out.println("Test application.properties =>");
        TestComponentSingleton testComponent1 = CoreContext.getBean("testComponentSingleton", TestComponentSingleton.class);
        TestComponentPrototype testComponent2 = CoreContext.getBean("testComponentPrototype", TestComponentPrototype.class);
        System.out.println(testComponent1.getTest());
        System.out.println(testComponent2.getTest());
        System.out.println();
    }

}
