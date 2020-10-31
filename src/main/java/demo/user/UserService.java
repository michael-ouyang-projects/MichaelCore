package demo.user;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import tw.framework.michaelcore.async.annotation.Async;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> queryAll() {
        return userRepository.queryAll();
    }

    @Transactional
    public void addUser(User user) {
        userRepository.addUser(user);
    }

    @Async
    @Transactional
    public CompletableFuture<String> addUserAsync(User user) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        userRepository.addUser(user);
        return CompletableFuture.completedFuture("Async Completed.");
    }

}
