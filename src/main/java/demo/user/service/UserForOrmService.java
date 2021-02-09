package demo.user.service;

import java.util.List;

import demo.user.model.UserForOrm;
import demo.user.repository.UserForOrmRepository;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.components.Service;

@Service
public class UserForOrmService {

    @Autowired
    private UserForOrmRepository userForOrmRepository;

    public List<UserForOrm> queryAll() {
        return userForOrmRepository.queryAll();
    }

    public void addUser(UserForOrm userForOrm) {
        userForOrmRepository.save(userForOrm);
    }

}
