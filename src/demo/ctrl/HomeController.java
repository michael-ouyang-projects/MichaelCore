package demo.ctrl;

import java.util.Map;

import tw.framework.ouyang.mvc.annotation.Controller;
import tw.framework.ouyang.mvc.annotation.Get;
import tw.framework.ouyang.mvc.annotation.Post;

@Controller
public class HomeController {

    @Get("/")
    public String home(Map<String, String> requestParameters) {
        return "index.html";
    }

    @Get("/hello")
    public String sayHelloToSomeoneByGet(Map<String, String> requestParameters) {
        return "welcome.html";
    }

    @Post("/hello")
    public String sayHelloToSomeoneByPost(Map<String, String> requestParameters) {
        return "welcome.html";
    }

}
