package demo;

import tw.framework.ouyang.data.annotation.Transactional;
import tw.framework.ouyang.ioc.annotation.Service;

@Service
public class UserService {

    @Transactional
    public void addUser(User user) {
        System.out.println(String.format("add new user to db => name: %s, age: %d", user.getName(), user.getAge()));
    }

}
