package demo;

import java.util.Map;

import tw.framework.ouyang.ioc.annotation.Autowired;
import tw.framework.ouyang.mvc.annotation.Controller;
import tw.framework.ouyang.mvc.annotation.Get;
import tw.framework.ouyang.mvc.annotation.Post;

@Controller
public class UserController {

    @Autowired
    public UserService homeService;

    @Get("/")
    public String home(Map<String, String> requestParameters) {
        return "index.html";
    }

    @Get("/add")
    public String addUserByGet(Map<String, String> requestParameters) {
        String name = requestParameters.get("name");
        int age = Integer.parseInt(requestParameters.get("age"));
        homeService.addUser(new User(name, age));
        return "success.html";
    }

    @Post("/add")
    public String addUserByPost(Map<String, String> requestParameters) {
        String name = requestParameters.get("name");
        int age = Integer.parseInt(requestParameters.get("age"));
        homeService.addUser(new User(name, age));
        return "success.html";
    }

}
