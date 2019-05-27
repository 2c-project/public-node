package master.exceptions;

public class FailedRequestException extends RuntimeException {
    public FailedRequestException(String message) {
        super(message);
    }

    public FailedRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
