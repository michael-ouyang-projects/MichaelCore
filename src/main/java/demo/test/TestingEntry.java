package demo.test;

import java.util.Date;

import tw.framework.michaelcore.core.CoreContext;
import tw.framework.michaelcore.core.annotation.Configuration;
import tw.framework.michaelcore.core.annotation.ExecuteAfterContainerStartup;
import tw.framework.michaelcore.ioc.BeanScope;
import tw.framework.michaelcore.ioc.annotation.Bean;

@Configuration
public class TestingEntry {

    @Bean(value = "testBean", scope = BeanScope.PROTOTYPE)
    public TestBean testBean() {
        return new TestBean();
    }

    @ExecuteAfterContainerStartup
    public void run() throws InterruptedException {
        TestBean bean1 = CoreContext.getBean("testBean", TestBean.class);
        TestBean bean2 = CoreContext.getBean("testBean", TestBean.class);
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
        component1.showDate();
        component2.showDate();
    }

}
