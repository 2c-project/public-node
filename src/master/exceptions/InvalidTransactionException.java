package master.exceptions;

public class InvalidTransactionException extends RuntimeException {
    private String hash;

    public InvalidTransactionException(String hash) {
        super("Hash = " + hash);
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }
}
