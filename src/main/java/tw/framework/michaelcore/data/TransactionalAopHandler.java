package tw.framework.michaelcore.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ListIterator;
import java.util.Stack;

import org.apache.commons.dbcp2.BasicDataSource;

import tw.framework.michaelcore.aop.annotation.After;
import tw.framework.michaelcore.aop.annotation.Before;
import tw.framework.michaelcore.data.enumeration.TransactionalPropagation;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.components.AopHandler;

@AopHandler
public class TransactionalAopHandler {

    @Autowired
    private BasicDataSource dataSource;
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<>();
    private static ThreadLocal<Savepoint> currentSavepoint = new ThreadLocal<>();
    private static ThreadLocal<Stack<TransactionalData>> transactionalDataStack = new ThreadLocal<>();

    @Before
    public void beforeTransactionalMethod() {
        TransactionalData transactionalData = transactionalDataStack.get().peek();
        try {
            if (needToCreateNewTransaction(transactionalData)) {
                createNewTransaction(transactionalData);
            } else if (transactionalData.getPropagation().equals(TransactionalPropagation.NESTED)) {
                currentSavepoint.set(currentConnection.get().setSavepoint());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean needToCreateNewTransaction(TransactionalData transactionData) {
        return currentConnection.get() == null || transactionData.getPropagation().equals(TransactionalPropagation.REQUIRES_NEW);
    }

    private void createNewTransaction(TransactionalData transactionData) throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(transactionData.getIsolation().getLevel());
        transactionData.setConnection(connection);
        transactionData.setIsCommit(true);
        currentConnection.set(connection);
    }

    @After
    public void afterTransactionalMethod() {
        TransactionalData transactionalData = transactionalDataStack.get().pop();
        if (transactionalData.getConnection() != null) {
            processCommitOrRollback(transactionalData);
            currentConnection.set(getCurrentConnection());
        } else if (!transactionalData.getIsCommit()) {
            doRollbackByCondition(transactionalData);
        }
    }

    private void processCommitOrRollback(TransactionalData transactionalData) {
        try (Connection connection = transactionalData.getConnection()) {
            if (transactionalData.getIsCommit()) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getCurrentConnection() {
        ListIterator<TransactionalData> TransactionalDataStackListIterator = transactionalDataStack.get().listIterator(transactionalDataStack.get().size());
        while (TransactionalDataStackListIterator.hasPrevious()) {
            Connection connection = TransactionalDataStackListIterator.previous().getConnection();
            if (connection != null) {
                return connection;
            }
        }
        return null;
    }

    private void doRollbackByCondition(TransactionalData transactionalData) {
        try {
            if (transactionalData.getPropagation().equals(TransactionalPropagation.REQUIRED)) {
                transactionalDataStack.get().peek().setIsCommit(false);
            } else if (transactionalData.getPropagation().equals(TransactionalPropagation.NESTED)) {
                currentConnection.get().rollback(currentSavepoint.get());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addNewTransactionData(TransactionalData transactionData) {
        if (transactionalDataStack.get() == null) {
            transactionalDataStack.set(new Stack<>());
        }
        transactionalDataStack.get().push(transactionData);
    }

    static Connection getConnection() {
        return currentConnection.get();
    }

    public static Class<? extends Throwable> getRollbackFor() {
        return transactionalDataStack.get().peek().getRollbackFor();
    }

    public static void setRollback() {
        if (transactionalDataStack.get() != null) {
            transactionalDataStack.get().peek().setIsCommit(false);
        }
    }

}
