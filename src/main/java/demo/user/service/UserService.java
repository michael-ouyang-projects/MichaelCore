package demo.user.service;

import java.util.List;

import demo.user.model.User;
import demo.user.repository.UserRepository;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Service;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> queryAll() {
        return userRepository.queryAll();
    }

    @Transactional
    public void addUser(User user) {
        userRepository.addUser(user);
    }

}
