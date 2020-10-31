package demo.user;

import java.util.List;

public interface IUserService {

	public List<User> queryAll();

    public void addUser(User user);
    
}
