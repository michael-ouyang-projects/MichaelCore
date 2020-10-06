package demo;

import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.ioc.annotation.Service;

@Service
public class UserService {

    @Transactional
    public void addUser(User user) {
        System.out.println(String.format("Add new user to db => name: %s, age: %d", user.getName(), user.getAge()));
    }

}
