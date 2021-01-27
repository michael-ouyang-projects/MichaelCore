package tw.framework.michaelcore.test.aop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tw.framework.michaelcore.ioc.Core;
import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.mvc.MvcCore;

public class TestAop {

    private final static ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final MyComponent component = CoreContext.getBean("myComponent", MyComponent.class);

    @BeforeAll
    public static void beforeAll() {
        Core.start();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterAll
    public static void afterAll() {
        CoreContext.getBean(MvcCore.class).clean();
        Core.clean();
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        outputStream.reset();
    }

    @Test
    public void testClassAop() {
        component.testClassAop();
        Assertions.assertEquals(
                "This is ClassAop!\r\n" +
                        "Bye Bye ClassAop!\r\n",
                outputStream.toString());
    }

    @Test
    public void testMethodAop() {
        component.testMethodAop();
        Assertions.assertEquals(
                "This is ClassAop!\r\n" +
                        "This is MethodAop!\r\n" +
                        "Bye Bye MethodAop!\r\n" +
                        "Bye Bye ClassAop!\r\n",
                outputStream.toString());
    }

    @Test
    public void testInnerMethodCallDirectly() {
        component.testInnerMethodCallDirectly();
        Assertions.assertEquals(
                "This is ClassAop!\r\n" +
                        "Inner Method()\r\n" +
                        "Bye Bye ClassAop!\r\n",
                outputStream.toString());
    }

    @Test
    public void testInnerMethodCallUsingProxy() {
        component.testInnerMethodCallUsingProxy();
        Assertions.assertEquals(
                "This is ClassAop!\r\n" +
                        "This is ClassAop!\r\n" +
                        "This is InnerMethodAop!\r\n" +
                        "Inner Method()\r\n" +
                        "Bye Bye InnerMethodAop!\r\n" +
                        "Bye Bye ClassAop!\r\n" +
                        "Bye Bye ClassAop!\r\n",
                outputStream.toString());
    }

}
