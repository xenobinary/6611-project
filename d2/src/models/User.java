/**
 * Abstract base class representing a user of the ABM system.
 * Handles authentication, account locking after failed attempts, and basic user identity.
 */
public abstract class User {
    /** The card number used for login identification. */
    protected String cardNumber;
    /** The plaintext PIN (only used during initial seed; actual auth uses DB hash). */
    protected String pin;
    /** The user's display name. */
    protected String userName;
    /** Whether the account is currently locked. */
    protected boolean locked;
    /** Number of consecutive failed authentication attempts. */
    protected int failedAttempts;
    /** Maximum failed attempts before the account is locked. */
    private static final int MAX_FAILED_ATTEMPTS = 3;

    /**
     * Constructs a new User.
     *
     * @param cardNumber the card number for identification
     * @param pin        the plaintext PIN
     * @param userName   the display name
     */
    public User(String cardNumber, String pin, String userName) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.userName = userName;
        this.locked = false;
        this.failedAttempts = 0;
    }

    /**
     * Returns whether this user type can be locked after failed attempts.
     * Technicians and admins cannot be locked.
     *
     * @return true if lockable, false otherwise
     */
    public boolean canBeLocked() {
        return true;
    }

    /**
     * Attempts to authenticate with the given PIN.
     * On success, resets failed attempts. On failure, increments the counter
     * and locks the account if the maximum is reached.
     *
     * @param inputPin the PIN to validate
     * @return true if authentication succeeded
     */
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

    /**
     * Unlocks the account and resets the failed attempt counter.
     */
    public void unlock() {
        this.locked = false;
        this.failedAttempts = 0;
    }

    /**
     * Returns whether the account is currently locked.
     *
     * @return true if locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Returns the number of consecutive failed authentication attempts.
     *
     * @return the failed attempt count
     */
    public int getFailedAttempts() {
        return failedAttempts;
    }

    /**
     * Returns the card number.
     *
     * @return the card number string
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * Returns the display name of the user.
     *
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Returns the role identifier for this user type.
     *
     * @return the role string
     */
    public abstract String getRole();
}
