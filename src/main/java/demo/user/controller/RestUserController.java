package demo.user.controller;

import java.util.List;

import demo.user.model.User;
import demo.user.service.UserService;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.components.RestController;
import tw.framework.michaelcore.mvc.annotation.Get;
import tw.framework.michaelcore.mvc.annotation.Post;
import tw.framework.michaelcore.mvc.annotation.RequestBody;

@RestController
public class RestUserController {

    @Autowired
    private UserService userService;

    @Get("/api/users")
    public List<User> queryAll() {
        return userService.queryAll();
    }

    @Post("/api/user/add")
    public void addUserByRestGet(@RequestBody User user) {
        userService.addUser(user);
    }

}
