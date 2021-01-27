package tw.framework.michaelcore.test.ioc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tw.framework.michaelcore.ioc.Core;
import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Bean;
import tw.framework.michaelcore.ioc.annotation.Configuration;
import tw.framework.michaelcore.ioc.enumeration.BeanScope;
import tw.framework.michaelcore.mvc.MvcCore;

@Configuration
public class TestIoC {

    @Bean(value = "singletonBean")
    public TestBean singletonBean() {
        return new TestBean();
    }

    @Bean(value = "prototypeBean", scope = BeanScope.PROTOTYPE)
    public TestBean prototypeBean() {
        return new TestBean();
    }

    @BeforeAll
    public static void beforeAll() {
        Core.start();
    }

    @AfterAll
    public static void afterAll() {
        CoreContext.getBean(MvcCore.class).clean();
        Core.clean();
    }

    @Test
    public void testSingletonBean() {
        TestBean bean1 = CoreContext.getBean("singletonBean", TestBean.class);
        TestBean bean2 = CoreContext.getBean("singletonBean", TestBean.class);
        Assertions.assertNotNull(bean1);
        Assertions.assertNotNull(bean2);
        Assertions.assertEquals(bean1, bean2);
    }

    @Test
    public void testPrototypeBean() {
        TestBean bean1 = CoreContext.getBean("prototypeBean", TestBean.class);
        TestBean bean2 = CoreContext.getBean("prototypeBean", TestBean.class);
        Assertions.assertNotNull(bean1);
        Assertions.assertNotNull(bean2);
        Assertions.assertNotEquals(bean1, bean2);
    }

    @Test
    public void testSingletonComponent() {
        SingletonComponent component1 = CoreContext.getBean("singletonComponent", SingletonComponent.class);
        SingletonComponent component2 = CoreContext.getBean("singletonComponent", SingletonComponent.class);
        Assertions.assertNotNull(component1);
        Assertions.assertNotNull(component2);
        Assertions.assertEquals(component1, component2);
    }

    @Test
    public void testPrototypeComponent() {
        PrototypeComponent component1 = CoreContext.getBean("prototypeComponent", PrototypeComponent.class);
        PrototypeComponent component2 = CoreContext.getBean("prototypeComponent", PrototypeComponent.class);
        Assertions.assertNotNull(component1);
        Assertions.assertNotNull(component2);
        Assertions.assertNotEquals(component1, component2);
    }

    @Test
    public void testSingletonDependency() {
        PrototypeComponent component = CoreContext.getBean("prototypeComponent", PrototypeComponent.class);
        Assertions.assertNotNull(component.getTestBean());

        TestBean bean = CoreContext.getBean("singletonBean", TestBean.class);
        Assertions.assertNotNull(bean);

        Assertions.assertEquals(component.getTestBean(), bean);
    }

    @Test
    public void testPrototypeDependency() {
        SingletonComponent component = CoreContext.getBean("singletonComponent", SingletonComponent.class);
        Assertions.assertNotNull(component.getTestBean());

        TestBean bean = CoreContext.getBean("prototypeBean", TestBean.class);
        Assertions.assertNotNull(bean);

        Assertions.assertNotEquals(component.getTestBean(), bean);
    }

    @Test
    public void testApplicationProperties() {
        SingletonComponent singletonComponent = CoreContext.getBean("singletonComponent", SingletonComponent.class);
        PrototypeComponent prototypeComponent = CoreContext.getBean("prototypeComponent", PrototypeComponent.class);
        Assertions.assertEquals(singletonComponent.getTest(), "hello@12345");
        Assertions.assertEquals(prototypeComponent.getTest(), "hello@12345");
    }

}

class TestBean {

}
