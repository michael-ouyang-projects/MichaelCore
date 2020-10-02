package tw.ouyang.ctrl;

import java.util.Map;

import tw.ouyang.annotation.Controller;
import tw.ouyang.annotation.Get;

@Controller
public class HomeController {

	@Get("/")
    public String home(Map<String, String> requestParameters) {
        return "index.html";
    }
	
    @Get("/hello")
    public String sayHelloToSomeOne(Map<String, String> requestParameters) {
    	return "welcome.html";
    }

}
