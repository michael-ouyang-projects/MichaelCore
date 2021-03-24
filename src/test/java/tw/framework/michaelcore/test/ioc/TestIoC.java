package tw.framework.michaelcore.test.ioc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Bean;
import tw.framework.michaelcore.ioc.annotation.components.Configuration;
import tw.framework.michaelcore.ioc.enumeration.BeanScope;
import tw.framework.michaelcore.test.utils.MichaelcoreExtension;

@Configuration
@ExtendWith(MichaelcoreExtension.class)
public class TestIoC {

    private static CoreContext coreContext;

    @BeforeAll
    public static void beforeAll() {
        coreContext = MichaelcoreExtension.getCoreContext();
    }

    @Bean("hello")
    public String helloString() {
        return "Hello, I'm Michael.";
    }

    @Bean(value = "singletonBean")
    public TestBean singletonBean() {
        return new TestBean();
    }

    @Bean(value = "prototypeBean", scope = BeanScope.PROTOTYPE)
    public TestBean prototypeBean() {
        return new TestBean();
    }

    @Test
    public void testSingletonBean() {
        TestBean bean1 = coreContext.getBean("singletonBean", TestBean.class);
        TestBean bean2 = coreContext.getBean("singletonBean", TestBean.class);
        Assertions.assertNotNull(bean1);
        Assertions.assertNotNull(bean2);
        Assertions.assertEquals(bean1, bean2);
    }

    @Test
    public void testPrototypeBean() {
        TestBean bean1 = coreContext.getBean("prototypeBean", TestBean.class);
        TestBean bean2 = coreContext.getBean("prototypeBean", TestBean.class);
        Assertions.assertNotNull(bean1);
        Assertions.assertNotNull(bean2);
        Assertions.assertNotEquals(bean1, bean2);
    }

    @Test
    public void testSingletonComponent() {
        SingletonComponent component1 = coreContext.getBean("singletonComponent", SingletonComponent.class);
        SingletonComponent component2 = coreContext.getBean("singletonComponent", SingletonComponent.class);
        Assertions.assertNotNull(component1);
        Assertions.assertNotNull(component2);
        Assertions.assertEquals(component1, component2);
    }

    @Test
    public void testPrototypeComponent() {
        PrototypeComponent component1 = coreContext.getBean("prototypeComponent", PrototypeComponent.class);
        PrototypeComponent component2 = coreContext.getBean("prototypeComponent", PrototypeComponent.class);
        Assertions.assertNotNull(component1);
        Assertions.assertNotNull(component2);
        Assertions.assertNotEquals(component1, component2);
    }

    @Test
    public void testSingletonDependency() {
        PrototypeComponent component = coreContext.getBean("prototypeComponent", PrototypeComponent.class);
        Assertions.assertNotNull(component.getTestBean());

        TestBean bean = coreContext.getBean("singletonBean", TestBean.class);
        Assertions.assertNotNull(bean);

        Assertions.assertEquals(component.getTestBean(), bean);
    }

    @Test
    public void testPrototypeDependency() {
        SingletonComponent component = coreContext.getBean("singletonComponent", SingletonComponent.class);
        Assertions.assertNotNull(component.getTestBean());

        TestBean bean = coreContext.getBean("prototypeBean", TestBean.class);
        Assertions.assertNotNull(bean);

        Assertions.assertNotEquals(component.getTestBean(), bean);
    }

    @Test
    public void testThirdPatryBean() {
        SingletonComponent component = coreContext.getBean("singletonComponent", SingletonComponent.class);
        Assertions.assertEquals("Hello, I'm Michael.", component.getHello());
    }

    @Test
    public void testApplicationProperties() {
        SingletonComponent singletonComponent = coreContext.getBean("singletonComponent", SingletonComponent.class);
        PrototypeComponent prototypeComponent = coreContext.getBean("prototypeComponent", PrototypeComponent.class);
        Assertions.assertEquals(singletonComponent.getTest(), "hello@12345");
        Assertions.assertEquals(prototypeComponent.getTest(), "hello@12345");
    }

}

class TestBean {

}
