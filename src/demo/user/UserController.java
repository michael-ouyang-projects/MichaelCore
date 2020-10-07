package demo.user;

import java.util.Map;

import demo.aop.SayHelloAop;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.aop.annotation.InterfaceForAop;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.mvc.annotation.Controller;
import tw.framework.michaelcore.mvc.annotation.Get;
import tw.framework.michaelcore.mvc.annotation.Post;

@Controller
@InterfaceForAop(IUserController.class)
@AopHere(SayHelloAop.class)
public class UserController implements IUserController {

    @Autowired
    public UserService homeService;

    @Override
    @Get("/")
    public String home(Map<String, String> requestParameters) {
        return "index.html";
    }

    @Override
    @Get("/add")
    public String addUserByGet(Map<String, String> requestParameters) {
        String name = requestParameters.get("name");
        int age = Integer.parseInt(requestParameters.get("age"));
        homeService.addUser(new User(name, age));
        return "success.html";
    }

    @Override
    @Post("/add")
    public String addUserByPost(Map<String, String> requestParameters) {
        String name = requestParameters.get("name");
        int age = Integer.parseInt(requestParameters.get("age"));
        homeService.addUser(new User(name, age));
        return "success.html";
    }

}
