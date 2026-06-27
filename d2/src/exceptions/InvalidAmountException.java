/**
 * Exception thrown when a transaction amount is invalid (e.g., not a multiple of 20,
 * exceeds per-transaction or daily limits, or uses a non-CAD account for withdrawal/deposit).
 */
public class InvalidAmountException extends Exception {
    /** The i18n key for the user-facing error message. */
    private String errorKey;

    /**
     * Constructs an InvalidAmountException with a default error key.
     *
     * @param message the detail message
     */
    public InvalidAmountException(String message) {
        super(message);
        this.errorKey = "error.invalidAmount";
    }

    /**
     * Constructs an InvalidAmountException with a specific error key.
     *
     * @param message  the detail message
     * @param errorKey the i18n key for the error message
     */
    public InvalidAmountException(String message, String errorKey) {
        super(message);
        this.errorKey = errorKey;
    }

    /**
     * Returns the i18n key for the error message shown to the user.
     *
     * @return the error key string
     */
    public String getErrorKey() { return errorKey; }
}
