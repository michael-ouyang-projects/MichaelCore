# MichaelCore

### This is a tutorial focus on how to develop your own Java Framework. So far, it include Embedded Web Server, IOC, AOP, MVC, Transactional Management, Asynchronous Processing, etc. (ORM and Mocking features are still under construction)
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
Take a look at readPropertiesToContainer(), it will first read lines in application.properties, and put the key-value pair into a HashMap one after another in [CoreContext.java](src/main/java/tw/framework/michaelcore/ioc/CoreContext.java).

[Note] CoreContext is a container that store all the data relevant to the framework, such as properties, classes, and objects.

Next, scanClassesToContainer(isJUnitTest()), it takes a boolean parameter which indicate that the execution of the application is an unit test or not, if you run the application from the main function then the parameter will be false, but if you run the application as junit test then you will find that it's true.

When we pass true to the scanClassesToContainer(), framework will only scan classes in the "target/classes" directory; otherwise, it will scan classes both in "target/classes" and "target/test-classes" for the testing purpose.
