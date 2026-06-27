/**
 * Exception thrown when a user account is locked due to too many failed login attempts.
 */
public class AccountLockedException extends Exception {
    /** The i18n key for the user-facing error message. */
    private String errorKey;

    /**
     * Constructs an AccountLockedException with a default error key.
     *
     * @param message the detail message
     */
    public AccountLockedException(String message) {
        super(message);
        this.errorKey = "error.locked";
    }

    /**
     * Constructs an AccountLockedException with a specific error key.
     *
     * @param message  the detail message
     * @param errorKey the i18n key for the error message
     */
    public AccountLockedException(String message, String errorKey) {
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
