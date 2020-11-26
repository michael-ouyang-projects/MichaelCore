package demo.user.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import demo.user.model.User;
import tw.framework.michaelcore.async.annotation.Async;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Service;

@Async
@Service
public class UserServiceAsync {

    @Autowired
    private UserService userService;

    public CompletableFuture<List<User>> queryAllAsync() throws Exception {
        Thread.sleep(5000);
        return CompletableFuture.completedFuture(userService.queryAll());
    }

    public void addUserAsync(User user) throws Exception {
        Thread.sleep(5000);
        userService.addUser(user);
    }

}
