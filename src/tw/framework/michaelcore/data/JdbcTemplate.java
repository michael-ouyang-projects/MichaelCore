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
            String sqlTest = String.format("INSERT INTO TT_USER(NAME, AGE) VALUES('%s', '%s')", "Test", "10");
            PreparedStatement statementTest = connection.prepareStatement(sqlTest);
            statementTest.execute();

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            TransactionalAop.setRollback();
        }
    }

}
