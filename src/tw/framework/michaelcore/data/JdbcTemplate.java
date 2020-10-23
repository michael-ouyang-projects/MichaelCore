package tw.framework.michaelcore.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import tw.framework.michaelcore.ioc.annotation.Component;

@Component
public class JdbcTemplate {

    public void execute(String sql) {
        Connection connection = TransactionalAop.getThreadConnection().get();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            TransactionalAop.setRollback();
        }
    }

}
