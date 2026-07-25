package exception;

/**
 * Thrown when login credentials are invalid or a user account is inactive.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
