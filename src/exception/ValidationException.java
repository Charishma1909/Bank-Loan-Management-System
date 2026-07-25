package exception;

/**
 * Thrown when user-supplied input fails business or format validation rules.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
