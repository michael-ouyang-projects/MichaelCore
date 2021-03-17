# MichaelCore

### This is a tutorial focus on how to develop your own Java Framework. So far, it include Embedded Web Server, IoC, AOP, MVC, Transactional Management, Asynchronous Processing, etc. (ORM and Mocking features are still under construction)

---
### Init
First of all, the Entrypoint!<br/>
Invoke Core.start() method in the main function, it will trigger the framework to do all the infrastructural stuff.
```
public class DemoApplication {
    public static void main(String[] args) throws Exception {
        Core.start();
    }
}
```
Let's dive into [Core.java](src/main/java/tw/framework/michaelcore/ioc/Core.java), the beginning of the framework! It has a static block which including two method, one for reading the properties file, the other one for scanning the classes within the application.
```
static {
    try {
        readPropertiesToContainer();
        scanClassesToContainer(isJUnitTest());
    } catch (Exception e) {
        System.err.println("Core Initial Error!");
        e.printStackTrace();
    }
}
```
Take a look at readPropertiesToContainer(), it will first read lines in application.properties, and put the key-value pair into a Map<String, String> one after another in [CoreContext.java](src/main/java/tw/framework/michaelcore/ioc/CoreContext.java).

[Note] CoreContext is a container that store all the data relevant to the framework, such as properties, classes, and objects.

Next, scanClassesToContainer(isJUnitTest()), it takes a boolean parameter which indicate that the execution of the application is an unit test or not, if you run the application from the main function then the parameter will be false, but if you run the application as junit test then you will find that it's true.
```
private static boolean isJUnitTest() {
    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
        if (element.getClassName().startsWith("org.junit")) {
            return true;
        }
    }
    return false;
}
```
When we pass true to the method, framework will only scan classes in the "target/classes" directory; otherwise, it will scan classes both in "target/classes" and "target/test-classes" for testing purpose. After finishing scanning task, the method will then put these classes into a List<Class<?>> in [CoreContext.java](src/main/java/tw/framework/michaelcore/ioc/CoreContext.java).

It's the end of the initial static block, leading us back to the Entrypoint => Core.start()
```
public static CoreContext start() {
    CoreContext coreContext = new CoreContext();
    try {
        initializeIoC(coreContext);
        initializeProperties(coreContext);
        initializeIoCForBean(coreContext);
        initializeAOP(coreContext);
        initializeAutowired(coreContext);
        executeStartupCode(coreContext);
        System.out.println("== MichaelCore Started Successfully ==");
    } catch (Exception e) {
        System.err.println("!! MichaelCore Started Error !!");
        e.printStackTrace();
    }
    return coreContext;
}
```
Above is the execution flow of the initialization, note that the sequence of these steps is very important and cannot be mess up! It will be divided into four block for detail explanation.

---
### IoC
