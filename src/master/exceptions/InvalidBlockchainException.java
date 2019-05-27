package master.exceptions;

public class InvalidBlockchainException extends RuntimeException {

    public InvalidBlockchainException() {
    }

    public InvalidBlockchainException(String message) {
        super(message);
    }

    public InvalidBlockchainException(String message, Throwable cause) {
        super(message, cause);
    }
}
