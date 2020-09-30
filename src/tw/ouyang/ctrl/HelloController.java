package tw.ouyang.ctrl;

import tw.ouyang.annotation.Controller;
import tw.ouyang.annotation.Get;
import tw.ouyang.annotation.Post;

@Controller
public class HelloController {

    @Get("/hello")
    public String helloGet() {
        return "hello GET";
    }

    @Post("/hello")
    public String helloPost() {
        return "hello POST";
    }

}
