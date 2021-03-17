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

Note: CoreContext is a container that store all the data relevant to the framework, such as properties, classes, and objects.
