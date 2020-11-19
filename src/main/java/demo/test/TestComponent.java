package demo.test;

import java.util.Date;
import java.util.List;

import demo.user.controller.RestUserController;
import demo.user.model.User;
import tw.framework.michaelcore.core.annotation.Value;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Component;

@Component(value = "testComponent")
public class TestComponent {

    private Date date;

    @Value
    private String test;

    @Autowired
    private RestUserController userController;

    public void showInfo() {
        System.out.println(test + " + " + date);
        System.out.println(userController);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<User> queryAll() {
        return userController.queryAll();
    }

}
