public class InsufficientFundException extends Exception {
    private double available;
    private double requested;

    public InsufficientFundException(String message, double available, double requested) {
        super(message);
        this.available = available;
        this.requested = requested;
    }

    public double getAvailable() {
        return available;
    }

    public double getRequested() {
        return requested;
    }
}
