package demo.user.service;

import java.util.List;

import demo.user.model.User;
import demo.user.repository.UserRepository;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> queryAll() {
        return userRepository.queryAll();
    }

    public void addUser(User user) {
        userRepository.save(user);
    }

    public void addUserWithTransactionalRollback(User user) {
        userRepository.save(user);
    }

}
