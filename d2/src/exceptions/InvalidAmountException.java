public class InvalidAmountException extends Exception {
    private String errorKey;

    public InvalidAmountException(String message) {
        super(message);
        this.errorKey = "error.invalidAmount";
    }

    public InvalidAmountException(String message, String errorKey) {
        super(message);
        this.errorKey = errorKey;
    }

    public String getErrorKey() { return errorKey; }
}
