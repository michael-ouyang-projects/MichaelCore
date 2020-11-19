package demo.test;

import java.util.Date;

import demo.user.service.IUserService;
import demo.user.service.UserService;
import tw.framework.michaelcore.core.annotation.Value;
import tw.framework.michaelcore.ioc.BeanScope;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Component;

@Component(value = "testComponent", scope = BeanScope.PROTOTYPE)
public class TestComponent {

    private Date date;

    @Value
    private String test;

    @Autowired(UserService.class)
    private IUserService userService;

    public void showDate() {
        System.out.println(test + " + " + date);
        System.out.println(userService);
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
