package tw.ouyang.ctrl;

import tw.ouyang.annotation.Controller;
import tw.ouyang.annotation.Get;

@Controller
public class TestController {

    @Get("/testing")
    public String testing() {
        return "hello NEW CTRL ~~~";
    }

}
