package demo.test;

import java.util.Date;

import tw.framework.michaelcore.ioc.BeanScope;
import tw.framework.michaelcore.ioc.annotation.Component;

@Component(value = "testComponent", scope = BeanScope.PROTOTYPE)
public class TestComponent {

    private Date date;

    public void sayHello() {
        System.out.println("Hello TestComponent!");
        showDate();
    }

    public void showDate() {
        System.out.println(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
