package demo.user.model;

import tw.framework.michaelcore.data.orm.annotation.Entity;
import tw.framework.michaelcore.data.orm.annotation.Id;

@Entity
public class UserForOrm {

    @Id
    private String name;
    private Integer age;

    public UserForOrm() {
    }

    public UserForOrm(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

}
