package exception;

/**
 * Thrown when a loan application cannot be processed due to a business rule
 * violation (e.g. applying twice, invalid state transitions).
 */
public class LoanProcessingException extends Exception {
    public LoanProcessingException(String message) {
        super(message);
    }
}
