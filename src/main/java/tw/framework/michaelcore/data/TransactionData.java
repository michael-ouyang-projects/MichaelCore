package tw.framework.michaelcore.data;

import java.sql.Connection;

import tw.framework.michaelcore.data.enumeration.TransactionIsolation;
import tw.framework.michaelcore.data.enumeration.TransactionPropagation;

public class TransactionData {

    private Connection connection;
    private Boolean isCommit;
    private TransactionPropagation propagation;
    private TransactionIsolation isolation;

    public TransactionData(TransactionPropagation propagation, TransactionIsolation isolation) {
        this.propagation = propagation;
        this.isolation = isolation;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Boolean getIsCommit() {
        return isCommit;
    }

    public void setIsCommit(Boolean isCommit) {
        this.isCommit = isCommit;
    }

    public TransactionPropagation getPropagation() {
        return propagation;
    }

    public void setPropagation(TransactionPropagation propagation) {
        this.propagation = propagation;
    }

    public TransactionIsolation getIsolation() {
        return isolation;
    }

    public void setIsolation(TransactionIsolation isolation) {
        this.isolation = isolation;
    }

}
