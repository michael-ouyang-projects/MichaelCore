package tw.framework.michaelcore.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.dbcp2.BasicDataSource;

import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Bean;
import tw.framework.michaelcore.ioc.annotation.ExecuteAfterContextCreate;
import tw.framework.michaelcore.ioc.annotation.Value;
import tw.framework.michaelcore.ioc.annotation.components.Configuration;

@Configuration
public class DataCore {

    @Value("db.url")
    private String url;

    @Value("db.userName")
    private String userName;

    @Value("db.password")
    private String password;

    @Value("db.driverClassName")
    private String driverClassName;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Bean
    public BasicDataSource createBasicDataSource() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(userName);
        basicDataSource.setPassword(password);
        basicDataSource.setDriverClassName(driverClassName);
        return basicDataSource;
    }

    @ExecuteAfterContextCreate(order = 1)
    public void initializeData() {
        StringBuilder command = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/data.sql"))) {
            while (reader.ready()) {
                char data = (char) reader.read();
                if (data == ';') {
                    jdbcTemplate.execute(command.toString());
                    command.setLength(0);
                } else {
                    command.append(data);
                }
            }
        } catch (IOException e) {
        }
    }

}
