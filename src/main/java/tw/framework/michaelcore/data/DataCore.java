package tw.framework.michaelcore.data;

import org.apache.commons.dbcp2.BasicDataSource;

import tw.framework.michaelcore.core.annotation.Configuration;
import tw.framework.michaelcore.ioc.annotation.Bean;
import tw.framework.michaelcore.ioc.annotation.Value;

@Configuration
public class DataCore {

    @Value
    private String url;

    @Value
    private String userName;

    @Value
    private String password;

    @Value
    private String driverClassName;

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
