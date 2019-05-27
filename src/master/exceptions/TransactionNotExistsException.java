package master.exceptions;

public class TransactionNotExistsException extends RuntimeException {
    public TransactionNotExistsException(String message) {
        super(message);
    }
}
