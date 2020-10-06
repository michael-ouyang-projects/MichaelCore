package demo;

import java.util.Map;

public interface IUserController {

    String home(Map<String, String> requestParameters);

    String addUserByGet(Map<String, String> requestParameters);

    String addUserByPost(Map<String, String> requestParameters);

}