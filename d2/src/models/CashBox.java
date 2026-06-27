/**
 * Singleton representing the physical cash box of the ABM.
 * Manages the current cash level, dispensing, refilling, and low-cash detection.
 */
public class CashBox {
    /** The singleton instance. */
    private static CashBox instance;
    /** The current amount of cash in the machine. */
    private double currentCash;
    /** The maximum cash capacity. */
    private double maxCapacity;
    /** The threshold below which cash is considered low. */
    private double lowThreshold;

    /** Private constructor; initializes with default cash levels. */
    private CashBox() {
        this.maxCapacity = 50000.0;
        this.currentCash = 20000.0;
        this.lowThreshold = 1000.0;
    }

    /**
     * Returns the singleton CashBox instance.
     *
     * @return the CashBox instance
     */
    public static CashBox getInstance() {
        if (instance == null) {
            instance = new CashBox();
        }
        return instance;
    }

    /**
     * Returns whether the cash box is empty.
     *
     * @return true if current cash is zero or less
     */
    public boolean isEmpty() {
        return currentCash <= 0.0;
    }

    /**
     * Returns whether the cash box is low (above zero but at or below the threshold).
     *
     * @return true if cash is low
     */
    public boolean isLow() {
        return currentCash > 0.0 && currentCash <= lowThreshold;
    }

    /**
     * Checks whether the cash box has enough cash to cover the given amount.
     *
     * @param amount the amount to check
     * @return true if sufficient cash is available
     */
    public boolean hasSufficientCash(double amount) {
        return currentCash >= amount;
    }

    /**
     * Dispenses the specified amount of cash from the box.
     * Only dispenses if there is enough cash.
     *
     * @param amount the amount to dispense
     */
    public void dispense(double amount) {
        if (amount <= currentCash) {
            currentCash -= amount;
        }
    }

    /**
     * Refills the cash box to its maximum capacity.
     */
    public void refill() {
        currentCash = maxCapacity;
    }

    /**
     * Refills the cash box to a specific amount.
     *
     * @param amount the target cash level (must be between 0 and max capacity)
     * @return true if the refill was accepted
     */
    public boolean refillTo(double amount) {
        if (amount <= 0 || amount > maxCapacity) {
            return false;
        }
        currentCash = amount;
        return true;
    }

    /**
     * Returns the current cash level.
     *
     * @return the current cash amount
     */
    public double getCurrentCash() {
        return currentCash;
    }

    /**
     * Returns the maximum cash capacity.
     *
     * @return the max capacity
     */
    public double getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Returns the low-cash warning threshold.
     *
     * @return the low threshold amount
     */
    public double getLowThreshold() {
        return lowThreshold;
    }

    /**
     * Resets the singleton instance (mainly for testing).
     */
    public static void resetInstance() {
        instance = null;
    }
}
