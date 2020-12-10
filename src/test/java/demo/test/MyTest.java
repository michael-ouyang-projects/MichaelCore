package demo.test;

import org.junit.jupiter.api.Test;

import demo.user.controller.UserController;
import demo.user.service.UserService;
import demo.user.service.UserServiceAsync;
import tw.framework.michaelcore.test.annotation.Spy;
import tw.framework.michaelcore.test.annotation.TestingClass;

public class MyTest {

    @TestingClass
    private UserController userController;

    @Spy
    private UserService userService;

    @Spy
    private UserServiceAsync userServiceAsync;

    @Test
    public void test() {

    }

}
