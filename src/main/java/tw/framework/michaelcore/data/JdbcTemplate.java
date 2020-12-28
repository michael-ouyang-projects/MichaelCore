package tw.framework.michaelcore.data;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Component;

@Component
public class JdbcTemplate {

    @Autowired
    private BasicDataSource dataSource;

    public ResultSet query(String sql) {
        ResultSet result = null;
        Connection connection = TransactionalAopHandler.getCurrentConnection();
        if (connection == null) {
            try (Connection newConnection = dataSource.getConnection();
                    PreparedStatement statement = newConnection.prepareStatement(sql)) {
                result = statement.executeQuery(sql);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                result = statement.executeQuery(sql);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                TransactionalAopHandler.setRollback();
            }
        }
        return result;
    }

    public <T> List<T> queryList(String sql, Class<T> clazz) {
        List<T> returningList = new ArrayList<T>();
        Connection connection = TransactionalAopHandler.getCurrentConnection();
        if (connection == null) {
            try (Connection newConnection = dataSource.getConnection();
                    PreparedStatement statement = newConnection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery(sql)) {
                resultSetToReturnList(resultSet, clazz, returningList);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery(sql)) {
                resultSetToReturnList(resultSet, clazz, returningList);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                TransactionalAopHandler.setRollback();
            }
        }
        return returningList;
    }

    private <T> void resultSetToReturnList(ResultSet resultSet, Class<T> clazz, List<T> returningList) throws Exception {
        while (resultSet.next()) {
            T object = clazz.newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getType().equals(String.class)) {
                    field.set(object, resultSet.getString(field.getName()));
                } else {
                    field.set(object, resultSet.getInt(field.getName()));
                }
            }
            returningList.add(object);
        }
    }

    public void execute(String sql) {
        Connection connection = TransactionalAopHandler.getCurrentConnection();
        if (connection == null) {
            try (Connection newConnection = dataSource.getConnection();
                    PreparedStatement statement = newConnection.prepareStatement(sql)) {
                statement.execute();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.execute();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                TransactionalAopHandler.setRollback();
            }
        }
    }

}
