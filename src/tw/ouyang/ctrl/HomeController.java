package tw.ouyang.ctrl;

import java.util.Map;

import tw.ouyang.annotation.Controller;
import tw.ouyang.annotation.Get;
import tw.ouyang.annotation.Post;

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
