package tw.framework.michaelcore.data;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import tw.framework.michaelcore.ioc.annotation.Component;

@Component
public class JdbcTemplate {

    public ResultSet query(String sql) {
        ResultSet result = null;
        Connection connection = TransactionalAop.getThreadConnection().get();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            result = statement.executeQuery(sql);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            TransactionalAop.setRollback();
        }
        return result;
    }

    public <T> List<T> queryList(String sql, Class<T> clazz) {
        List<T> resultList = new ArrayList<T>();
        Connection connection = TransactionalAop.getThreadConnection().get();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery(sql);
            while (result.next()) {
                T object = clazz.newInstance();
                for (Method method : clazz.getMethods()) {
                    if (method.getName().startsWith("set")) {
                        String fieldName = method.getName().split("set")[1];
                        Class<?> fieldType = method.getParameterTypes()[0];
                        if (fieldType.getName().equals(String.class.getName())) {
                            method.invoke(object, result.getString(fieldName));
                        } else if (fieldType.getName().equals(Integer.class.getName())) {
                            method.invoke(object, result.getInt(fieldName));
                        }
                    }
                }
                resultList.add(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            TransactionalAop.setRollback();
        }
        return resultList;
    }

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
