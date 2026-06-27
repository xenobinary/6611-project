/**
 * Exception thrown when an account has insufficient funds to complete a transaction.
 */
public class InsufficientFundException extends Exception {
    /** The available balance in the account. */
    private double available;
    /** The amount that was requested. */
    private double requested;

    /**
     * Constructs an InsufficientFundException.
     *
     * @param message   the detail message
     * @param available the available balance
     * @param requested the requested amount
     */
    public InsufficientFundException(String message, double available, double requested) {
        super(message);
        this.available = available;
        this.requested = requested;
    }

    /**
     * Returns the available balance at the time of the exception.
     *
     * @return the available balance
     */
    public double getAvailable() {
        return available;
    }

    /**
     * Returns the amount that was requested.
     *
     * @return the requested amount
     */
    public double getRequested() {
        return requested;
    }
}
