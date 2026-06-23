public class CashBox {
    private static CashBox instance;
    private double currentCash;
    private double maxCapacity;
    private double lowThreshold;

    private CashBox() {
        this.maxCapacity = 50000.0;
        this.currentCash = 20000.0;
        this.lowThreshold = 1000.0;
    }

    public static CashBox getInstance() {
        if (instance == null) {
            instance = new CashBox();
        }
        return instance;
    }

    public boolean isEmpty() {
        return currentCash <= 0.0;
    }

    public boolean isLow() {
        return currentCash > 0.0 && currentCash <= lowThreshold;
    }

    public boolean hasSufficientCash(double amount) {
        return currentCash >= amount;
    }

    public void dispense(double amount) {
        if (amount <= currentCash) {
            currentCash -= amount;
        }
    }

    public void refill() {
        currentCash = maxCapacity;
    }

    public boolean refillTo(double amount) {
        if (amount <= 0 || amount > maxCapacity) {
            return false;
        }
        currentCash = amount;
        return true;
    }

    public double getCurrentCash() {
        return currentCash;
    }

    public double getMaxCapacity() {
        return maxCapacity;
    }

    public double getLowThreshold() {
        return lowThreshold;
    }

    public static void resetInstance() {
        instance = null;
    }
}
