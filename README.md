# MichaelCore

### This is a tutorial focus on how to develop your own Java Framework. So far, it include IOC, AOP, MVC, Transcatinal Management, Async, etc.

First of all, the entrypoint!<br/>
User will call Core.start() method in the main function of the application.
```
public class DemoApplication {
    public static void main(String[] args) throws Exception {
        Core.start();
    }
}
```

Let's dive into "tw.framework.michaelcore.ioc.Core.java"
