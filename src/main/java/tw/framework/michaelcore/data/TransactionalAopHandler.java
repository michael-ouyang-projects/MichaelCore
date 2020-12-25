package tw.framework.michaelcore.data;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

import tw.framework.michaelcore.aop.annotation.After;
import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.Before;
import tw.framework.michaelcore.ioc.annotation.Autowired;

@AopHandler
public class TransactionalAopHandler {

    @Autowired
    private BasicDataSource dataSource;
    private static ThreadLocal<Connection> threadConnection = new ThreadLocal<>();
    private static ThreadLocal<Boolean> commit = new ThreadLocal<>();

    @Before
    public void getConnectionAndSetAutoCommitToFalse() {
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            threadConnection.set(connection);
            commit.set(true);
            System.out.println("In Transaction");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @After
    public void commitOrRollbackConnectionAndCloseIt() {
        try (Connection connection = threadConnection.get()) {
            if (commit.get()) {
                connection.commit();
            } else {
                connection.rollback();
            }
            System.out.println("Out Transaction");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        threadConnection.remove();
        commit.remove();
    }

    static ThreadLocal<Connection> getThreadConnection() {
        return threadConnection;
    }

    static void setRollback() {
        commit.set(false);
    }

}
