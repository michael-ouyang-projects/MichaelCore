package demo.user;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Service;
import tw.framework.michaelcore.thread.annotation.Async;

@Service
@Transactional
public class UserService {

    @Autowired
    public UserRepository userRepository;

    public List<User> queryAll() {
        return userRepository.queryAll();
    }

    public void addUser(User user) {
        userRepository.addUser(user);
    }

    @Async
    public CompletableFuture<String> addUserAsync(User user) {
        try {
            Thread.sleep(5000);
            System.out.println("3");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        userRepository.addUser(user);
        return CompletableFuture.completedFuture("Test");
    }

}
