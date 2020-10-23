package demo.user;

import java.util.Map;

import demo.aop.SayHelloAop;
import demo.aop.TestCtrlAop;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.mvc.annotation.Controller;
import tw.framework.michaelcore.mvc.annotation.Get;
import tw.framework.michaelcore.mvc.annotation.Post;

@Controller
@AopHere(TestCtrlAop.class)
public class UserController {

    @Autowired
    public UserService userService;

    @Get("/")
    public String home(Map<String, String> requestParameters) {
        return "index.html";
    }

    @Get("/add")
    public String addUserByGet(Map<String, String> requestParameters) {
        String name = requestParameters.get("name");
        int age = Integer.parseInt(requestParameters.get("age"));
        userService.addUser(new User(name, age));
        return "success.html";
    }

    @Post("/add")
    @AopHere(SayHelloAop.class)
    public String addUserByPost(Map<String, String> requestParameters) {
        String name = requestParameters.get("name");
        int age = Integer.parseInt(requestParameters.get("age"));
        userService.addUser(new User(name, age));
        return "success.html";
    }

}
