package tw.framework.michaelcore.data.enumeration;

public enum TransactionIsolation {

    READ_COMMITTED(2), SERIALIZABLE(8);

    private int level;

    TransactionIsolation(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

}
