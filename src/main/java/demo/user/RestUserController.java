package demo.user;

import java.util.List;

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
    public List<User> queryAll() {
        return userService.queryAll();
    }

    @Post("/api/user/add")
    public void addUserByRestGet(@RequestBody User user) {
        userService.addUser(user);
    }

}
