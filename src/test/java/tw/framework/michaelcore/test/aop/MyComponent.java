package tw.framework.michaelcore.test.aop;

import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Component;

@AopHere(ClassAop.class)
@Component(value = "myComponent")
public class MyComponent {

    @Autowired
    private CoreContext coreContext;

    public void testClassAop() {
    }

    @AopHere(MethodAop.class)
    public void testMethodAop() {
    }

    public void testInnerMethodCallDirectly() {
        innerMethodCall();
    }

    public void testInnerMethodCallUsingProxy() {
        coreContext.executeInnerMethodWithAop(this.getClass()).innerMethodCall();
    }

    @AopHere(InnerMethodAop.class)
    public void innerMethodCall() {
        System.out.println("Inner Method()");
    }

}
