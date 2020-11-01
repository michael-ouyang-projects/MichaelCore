package demo.user.service;

import java.util.List;

import demo.aop.TestInnerMethodCallAop;
import demo.user.model.User;
import tw.framework.michaelcore.aop.annotation.AopHere;
import tw.framework.michaelcore.async.annotation.Async;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Service;

@Async
@Service
public class UserServiceAsync implements IUserService {

	@Autowired
    private UserService userService;

    public List<User> queryAll() {
        return userService.queryAll();
    }

    @Transactional
    public void addUser(User user) {
    	try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    	testInnerMethodCall();
    	userService.addUser(user);
    }

    @AopHere(TestInnerMethodCallAop.class)
	public void testInnerMethodCall() {
		System.out.println("Inner Method Call~");
	}
	
}
