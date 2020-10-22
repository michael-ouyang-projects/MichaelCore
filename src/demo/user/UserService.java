package demo.user;

import tw.framework.michaelcore.aop.annotation.AopInterface;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Service;

@Service
@AopInterface(IUserService.class)
public class UserService implements IUserService {

    @Autowired
    public UserRepository userRepository;

    @Override
    @Transactional
    public void addUser(User user) {
        userRepository.addUser(user);
    }

}
