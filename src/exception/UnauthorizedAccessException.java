package exception;

/**
 * UnauthorizedAccessException - Thrown when a user attempts an action they are not permitted to perform.
 */
public class UnauthorizedAccessException extends Exception {

    private final String username;
    private final String action;

    public UnauthorizedAccessException(String message) {
        super(message);
        this.username = null;
        this.action   = null;
    }

    public UnauthorizedAccessException(String username, String action, String message) {
        super(message);
        this.username = username;
        this.action   = action;
    }

    public String getUsername() { return username; }
    public String getAction()   { return action;   }
}
