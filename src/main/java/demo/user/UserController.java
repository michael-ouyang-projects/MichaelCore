package demo.user;

import demo.aop.SayHelloAop;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.mvc.Model;
import tw.framework.michaelcore.mvc.annotation.Controller;
import tw.framework.michaelcore.mvc.annotation.Get;
import tw.framework.michaelcore.mvc.annotation.Post;
import tw.framework.michaelcore.mvc.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    public UserService userService;

    @Get("/")
    public Model home() {
        return new Model("index.html");
    }

    @Get("/add")
    public Model addUserByGet(@RequestParam("name") String name, @RequestParam("age") int age) {
        userService.addUser(new User(name, age));
        Model model = new Model("success.html");
        model.add("name", name);
        model.add("age", age);
        return model;
    }

    @Post("/add")
    @AopHere(SayHelloAop.class)
    public Model addUserByPost(@RequestParam("name") String name, @RequestParam("age") int age) {
        userService.addUser(new User(name, age));
        Model model = new Model("success.html");
        model.add("name", name);
        model.add("age", age);
        return model;
    }

}
