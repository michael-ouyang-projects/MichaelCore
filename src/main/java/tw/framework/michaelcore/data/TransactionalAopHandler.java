package tw.framework.michaelcore.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Stack;

import org.apache.commons.dbcp2.BasicDataSource;

import tw.framework.michaelcore.aop.annotation.After;
import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.aop.annotation.Before;
import tw.framework.michaelcore.data.enumeration.TransactionPropagation;
import tw.framework.michaelcore.ioc.annotation.Autowired;

@AopHandler
public class TransactionalAopHandler {

    @Autowired
    private BasicDataSource dataSource;
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<>();
    private static ThreadLocal<Savepoint> currentSavePoint = new ThreadLocal<>();
    private static ThreadLocal<Stack<TransactionData>> TransactionDataStackThreadLocal = new ThreadLocal<>();

    @Before
    public void getConnectionAndSetAutoCommitToFalse() {
        TransactionData transactionData = TransactionDataStackThreadLocal.get().peek();
        try {
            if (needToCreateNewConnection(transactionData)) {
                Connection connection = dataSource.getConnection();
                connection.setAutoCommit(false);
                connection.setTransactionIsolation(transactionData.getIsolation().getLevel());
                transactionData.setConnection(connection);
                transactionData.setIsCommit(true);
                currentConnection.set(connection);
            } else if (transactionData.getPropagation().equals(TransactionPropagation.NESTED)) {
                currentSavePoint.set(currentConnection.get().setSavepoint());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean needToCreateNewConnection(TransactionData transactionData) {
        return currentConnection.get() == null || transactionData.getPropagation().equals(TransactionPropagation.REQUIRES_NEW);
    }

    @After
    public void commitOrRollbackConnectionAndCloseIt() {
        TransactionData transactionData = TransactionDataStackThreadLocal.get().pop();
        if (transactionData.getConnection() != null) {
            try (Connection connection = transactionData.getConnection()) {
                if (transactionData.getIsCommit()) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (transactionData.getPropagation().equals(TransactionPropagation.REQUIRES_NEW) && !TransactionDataStackThreadLocal.get().isEmpty()) {
                currentConnection.set(TransactionDataStackThreadLocal.get().peek().getConnection());
            }
        } else if (transactionData.getIsCommit() != null && transactionData.getIsCommit() == false) {
            try {
                if (transactionData.getPropagation().equals(TransactionPropagation.REQUIRED)) {
                    TransactionDataStackThreadLocal.get().peek().setIsCommit(false);
                } else if (transactionData.getPropagation().equals(TransactionPropagation.NESTED)) {
                    currentConnection.get().rollback(currentSavePoint.get());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (TransactionDataStackThreadLocal.get().isEmpty()) {
            currentConnection.remove();
            currentSavePoint.remove();
        }
    }

    public void addNewTransactionData(TransactionData transactionData) {
        if (TransactionDataStackThreadLocal.get() == null) {
            TransactionDataStackThreadLocal.set(new Stack<>());
        }
        TransactionDataStackThreadLocal.get().push(transactionData);
    }

    static Connection getCurrentConnection() {
        return currentConnection.get();
    }

    public static void setRollback() {
        TransactionDataStackThreadLocal.get().peek().setIsCommit(false);
    }

}
