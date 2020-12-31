package tw.framework.michaelcore.data.enumeration;

public enum TransactionalIsolation {

    READ_COMMITTED(2), SERIALIZABLE(8);

    private int level;

    TransactionalIsolation(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

}
