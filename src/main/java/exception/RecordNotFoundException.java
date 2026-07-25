package exception;

/**
 * Thrown when a requested entity (customer, loan, user, etc.) cannot be found in the database.
 */
public class RecordNotFoundException extends Exception {
    public RecordNotFoundException(String message) {
        super(message);
    }
}
