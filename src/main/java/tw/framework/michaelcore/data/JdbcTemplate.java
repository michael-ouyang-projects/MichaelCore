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
import tw.framework.michaelcore.ioc.annotation.components.Component;

@Component
public class JdbcTemplate {

    @Autowired
    private BasicDataSource dataSource;

    public <T> T queryValue(String sql, Class<T> clazz) {
        T returningValue = null;
        Connection connection = TransactionalAopHandler.getConnection();
        if (connection == null) {
            try (Connection newConnection = dataSource.getConnection();
                    PreparedStatement statement = newConnection.prepareStatement(sql)) {
                returningValue = resultSetToValue(statement.executeQuery(sql), clazz);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                returningValue = resultSetToValue(statement.executeQuery(sql), clazz);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                TransactionalAopHandler.setRollback();
            }
        }
        return returningValue;
    }

    public <T> List<T> queryValueList(String sql, Class<T> clazz) {
        List<T> returningValue = null;
        Connection connection = TransactionalAopHandler.getConnection();
        if (connection == null) {
            try (Connection newConnection = dataSource.getConnection();
                    PreparedStatement statement = newConnection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery(sql)) {
                returningValue = resultSetToValueList(resultSet, clazz);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery(sql)) {
                returningValue = resultSetToValueList(resultSet, clazz);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                TransactionalAopHandler.setRollback();
            }
        }
        return returningValue;
    }

    private <T> T resultSetToValue(ResultSet resultSet, Class<T> clazz) throws Exception {
        if (resultSet.next()) {
            return getValueFromResultSet(clazz, resultSet);
        }
        return null;
    }

    private <T> List<T> resultSetToValueList(ResultSet resultSet, Class<T> clazz) throws Exception {
        List<T> list = new ArrayList<T>();
        while (resultSet.next()) {
            list.add(getValueFromResultSet(clazz, resultSet));
        }
        return list;
    }

    private <T> T getValueFromResultSet(Class<T> clazz, ResultSet resultSet) throws Exception {
        return clazz.cast(resultSet.getObject(1));
    }

    public <T> T queryObject(String sql, Class<T> clazz) {
        T returningObject = null;
        Connection connection = TransactionalAopHandler.getConnection();
        if (connection == null) {
            try (Connection newConnection = dataSource.getConnection();
                    PreparedStatement statement = newConnection.prepareStatement(sql)) {
                returningObject = resultSetToObject(statement.executeQuery(sql), clazz);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                returningObject = resultSetToObject(statement.executeQuery(sql), clazz);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                TransactionalAopHandler.setRollback();
            }
        }
        return returningObject;
    }

    public <T> List<T> queryObjectList(String sql, Class<T> clazz) {
        List<T> returningList = null;
        Connection connection = TransactionalAopHandler.getConnection();
        if (connection == null) {
            try (Connection newConnection = dataSource.getConnection();
                    PreparedStatement statement = newConnection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery(sql)) {
                returningList = resultSetToObjectList(resultSet, clazz);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery(sql)) {
                returningList = resultSetToObjectList(resultSet, clazz);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                TransactionalAopHandler.setRollback();
            }
        }
        return returningList;
    }

    private <T> T resultSetToObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        if (resultSet.next()) {
            return createObjectFromResultSet(clazz, resultSet);
        }
        return null;
    }

    private <T> List<T> resultSetToObjectList(ResultSet resultSet, Class<T> clazz) throws Exception {
        List<T> list = new ArrayList<T>();
        while (resultSet.next()) {
            list.add(createObjectFromResultSet(clazz, resultSet));
        }
        return list;
    }

    private <T> T createObjectFromResultSet(Class<T> clazz, ResultSet resultSet) throws Exception {
        T object = clazz.getDeclaredConstructor().newInstance();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (String.class.equals(field.getType())) {
                field.set(object, resultSet.getString(field.getName()));
            } else {
                field.set(object, resultSet.getInt(field.getName()));
            }
        }
        return object;
    }

    public void execute(String sql) {
        Connection connection = TransactionalAopHandler.getConnection();
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