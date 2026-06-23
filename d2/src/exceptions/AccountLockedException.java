public class AccountLockedException extends Exception {
    private String errorKey;

    public AccountLockedException(String message) {
        super(message);
        this.errorKey = "error.locked";
    }

    public AccountLockedException(String message, String errorKey) {
        super(message);
        this.errorKey = errorKey;
    }

    public String getErrorKey() { return errorKey; }
}
