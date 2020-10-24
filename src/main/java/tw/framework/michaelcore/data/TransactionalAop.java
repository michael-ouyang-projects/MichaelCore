package tw.framework.michaelcore.data;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

import tw.framework.michaelcore.aop.MichaelCoreAopHandler;
import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.ioc.CoreContext;

@AopHandler
public class TransactionalAop extends MichaelCoreAopHandler {

    private static ThreadLocal<Connection> threadConnection = new ThreadLocal<>();
    private static ThreadLocal<Boolean> isCommit = new ThreadLocal<>();

    @Override
    public void before() {
        BasicDataSource basicDataSource = CoreContext.getBean("basicDataSource", BasicDataSource.class);
        try {
            Connection connection = basicDataSource.getConnection();
            connection.setAutoCommit(false);
            threadConnection.set(connection);
            isCommit.set(true);
            System.out.println("IN Transaction");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void after() {
        try (Connection connection = threadConnection.get()) {
            if (isCommit.get()) {
                connection.commit();
            } else {
                connection.rollback();
            }
            System.out.println("OUT Transaction");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        threadConnection.remove();
        isCommit.remove();
    }

    public static ThreadLocal<Connection> getThreadConnection() {
        return threadConnection;
    }

    public static void setRollback() {
        isCommit.set(false);
    }

}
