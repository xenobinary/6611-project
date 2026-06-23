public abstract class User {
    protected String cardNumber;
    protected String pin;
    protected String userName;
    protected boolean locked;
    protected int failedAttempts;
    private static final int MAX_FAILED_ATTEMPTS = 3;

    public User(String cardNumber, String pin, String userName) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.userName = userName;
        this.locked = false;
        this.failedAttempts = 0;
    }

    public boolean canBeLocked() {
        return true;
    }

    public boolean authenticate(String inputPin) {
        if (locked) {
            return false;
        }
        if (this.pin.equals(inputPin)) {
            failedAttempts = 0;
            return true;
        }
        if (!canBeLocked()) {
            return false;
        }
        failedAttempts++;
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            locked = true;
        }
        return false;
    }

    public void unlock() {
        this.locked = false;
        this.failedAttempts = 0;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getUserName() {
        return userName;
    }

    public abstract String getRole();
}
