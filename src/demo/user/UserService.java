package demo.user;

import demo.aop.SayHelloAop;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.aop.annotation.AopInterface;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.ioc.annotation.Service;

@Service
@AopInterface(IUserService.class)
@AopHere(SayHelloAop.class)
public class UserService implements IUserService {

    @Override
    @Transactional
    public void addUser(User user) {
        System.out.println(String.format("Add new user to db => name: %s, age: %d", user.getName(), user.getAge()));
    }

}
