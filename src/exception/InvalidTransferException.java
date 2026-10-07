package exception;

/**
 * InvalidTransferException - Thrown when an evidence transfer is invalid.
 * Examples: sender doesn't hold the evidence, invalid receiver, same user transfer.
 */
public class InvalidTransferException extends Exception {

    private final String reason;

    public InvalidTransferException(String message) {
        super(message);
        this.reason = message;
    }

    public InvalidTransferException(String message, Throwable cause) {
        super(message, cause);
        this.reason = message;
    }

    public String getReason() { return reason; }
}
