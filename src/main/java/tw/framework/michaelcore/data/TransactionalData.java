package tw.framework.michaelcore.data;

import java.sql.Connection;

import tw.framework.michaelcore.data.enumeration.TransactionalIsolation;
import tw.framework.michaelcore.data.enumeration.TransactionalPropagation;

public class TransactionalData {

    private Boolean isCommit;
    private Connection connection;
    private Class<? extends Throwable> rollbackFor;
    private TransactionalPropagation propagation;
    private TransactionalIsolation isolation;

    public TransactionalData(TransactionalPropagation propagation, TransactionalIsolation isolation, Class<? extends Throwable> rollbackFor) {
        this.isCommit = true;
        this.propagation = propagation;
        this.isolation = isolation;
        this.rollbackFor = rollbackFor;
    }

    public Boolean getIsCommit() {
        return isCommit;
    }

    public void setIsCommit(Boolean isCommit) {
        this.isCommit = isCommit;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Class<? extends Throwable> getRollbackFor() {
        return rollbackFor;
    }

    public void setRollbackFor(Class<? extends Throwable> rollbackFor) {
        this.rollbackFor = rollbackFor;
    }

    public TransactionalPropagation getPropagation() {
        return propagation;
    }

    public void setPropagation(TransactionalPropagation propagation) {
        this.propagation = propagation;
    }

    public TransactionalIsolation getIsolation() {
        return isolation;
    }

    public void setIsolation(TransactionalIsolation isolation) {
        this.isolation = isolation;
    }

}
