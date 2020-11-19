package demo.user.controller;

import java.util.List;

import demo.aop.ControllerAop;
import demo.user.model.User;
import demo.user.service.IUserService;
import demo.user.service.UserService;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.mvc.annotation.Get;
import tw.framework.michaelcore.mvc.annotation.Post;
import tw.framework.michaelcore.mvc.annotation.RequestBody;
import tw.framework.michaelcore.mvc.annotation.RestController;

@RestController
public class RestUserController {

    @Autowired(UserService.class)
    private IUserService userService;

    @Get("/api/users")
    @AopHere(ControllerAop.class)
    public List<User> queryAll() {
        return userService.queryAll();
    }

    @Post("/api/user/add")
    public void addUserByRestGet(@RequestBody User user) {
        userService.addUser(user);
    }

}
