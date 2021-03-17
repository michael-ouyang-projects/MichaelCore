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

[Note] CoreContext is a container that store all the data relevant to the framework, such as properties, classes, and objects. Below are the fields inside CoreContext.
```
private static Map<String, String> properties = new HashMap<>();
private static List<Class<?>> classes;
private Map<String, Object> beanFactory = new HashMap<>();
private Map<String, Object> realBeanFactory = new HashMap<>();
```

Next, scanClassesToContainer(isJUnitTest()), it takes a boolean parameter which indicate that the execution of the application is an unit test or not, if you run the application from main function then the parameter will be false, but if you run the application as junit test then you will find that it's true.
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
When we pass false to the method, framework will only scan classes in the "target/classes" directory; otherwise, it will scan classes both in "target/classes" and "target/test-classes" for testing purpose. After finishing scanning task, the method will then put these classes into a List<Class<?>> in [CoreContext.java](src/main/java/tw/framework/michaelcore/ioc/CoreContext.java).

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
Above is the execution flow of the initialization, note that the sequence of these steps is very important and cannot be mess up!

That kick off by create a new CoreContext. Actually, it put itself (the CoreContext object) into one of it's object map during construction. We will talk about the other object map util the AOP initialization when we need to create some proxy objects.
```
public CoreContext() {
    beanFactory.put(this.getClass().getName(), this);
}
```
Next, initializeIoC(coreContext), the code is down below.
```
private static void initializeIoC(CoreContext coreContext) throws Exception {
    for (Class<?> clazz : CoreContext.getClasses()) {
        if (Components.isComponentClass(clazz)) {
            processIoC(coreContext, clazz);
        }
    }
}
```
It will iterate the classes get from CoreContext and check if the current class has these annotation presented. These annotations are all located in [here](src/main/java/tw/framework/michaelcore/ioc/annotation/components).
```
COMPONENT(Component.class),
CONFIGURATION(Configuration.class),
CONTROLLER(Controller.class),
RESTCONTROLLER(RestController.class),
SERVICE(Service.class),
REPOSITORY(Repository.class),
ORMREPOSITORY(OrmRepository.class),
AOPHANDLER(AopHandler.class);
```
