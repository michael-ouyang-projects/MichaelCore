package demo.user;

import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.mvc.annotation.Get;
import tw.framework.michaelcore.mvc.annotation.RequestParam;
import tw.framework.michaelcore.mvc.annotation.RestController;

@RestController
public class RestUserController {

    @Autowired
    public UserService userService;

//    @Get("/add")
//    public String addUserByRestGet(
//            @RequestParam(value = "name") String name,
//            @RequestParam(value = "age") Integer age) {
//        userService.addUser(new User(name, age));
//        return "return rest";
//    }

}
