package demo.user;

import java.util.List;

import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.mvc.annotation.Get;
import tw.framework.michaelcore.mvc.annotation.Post;
import tw.framework.michaelcore.mvc.annotation.RestController;

@RestController
public class RestUserController {

    @Autowired
    public UserService userService;

    @Get("/query")
    public List<User> queryAll() {
        return userService.queryAll();
    }

    @Post("/user/add")
    public void addUserByRestGet(User user) {
        userService.addUser(user);
    }

}
