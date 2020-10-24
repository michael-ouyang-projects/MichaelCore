package tw.framework.michaelcore.data;

import tw.framework.michaelcore.ioc.annotation.Component;
import tw.framework.michaelcore.ioc.annotation.Value;

@Component
public class DataSource {

    @Value
    public String url;

    @Value
    public String username;

    @Value
    public String password;

    @Value
    public String driver;

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDriver() {
        return driver;
    }

}
