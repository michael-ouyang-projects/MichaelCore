package demo.user.service;

import java.util.List;

import demo.user.model.User;

public interface IUserService {

	public List<User> queryAll();

    public void addUser(User user);
    
}
