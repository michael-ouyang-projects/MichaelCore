package tw.framework.michaelcore.data;

import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Component;

@Component
public class JdbcTemplate {

    @Autowired
    public DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void execute(String sql) {

    }

}
