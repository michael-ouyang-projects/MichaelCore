package tw.framework.michaelcore.data;

import org.apache.commons.dbcp2.BasicDataSource;

import tw.framework.michaelcore.core.annotation.Configuration;
import tw.framework.michaelcore.ioc.annotation.Bean;
import tw.framework.michaelcore.ioc.annotation.Value;

@Configuration
public class DataCore {

    @Value
    public String url;

    @Value
    public String userName;

    @Value
    public String password;

    @Value
    public String driverClassName;

    @Bean
    public BasicDataSource createBasicDataSource() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(userName);
        basicDataSource.setPassword(password);
        basicDataSource.setDriverClassName(driverClassName);
        return basicDataSource;
    }

}
