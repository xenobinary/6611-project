import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller that manages user authentication, session state, and user persistence.
 * Handles login, logout, PIN verification against the database, account locking,
 * and persisting user and account state changes.
 */
public class AuthenticationController {
    /** In-memory cache of all users, keyed by card number. */
    private Map<String, User> userDatabase;
    /** The currently authenticated user, or null if no one is logged in. */
    private User currentUser;
    /** Reference to the database manager for persistence. */
    private DatabaseManager db;

    /**
     * Constructs a new AuthenticationController and loads all users from the database.
     */
    public AuthenticationController() {
        userDatabase = new HashMap<>();
        currentUser = null;
        db = DatabaseManager.getInstance();
        try {
            userDatabase = db.loadUsers();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load users", e);
        }
    }

    /**
     * Authenticates a user with the given card number and PIN.
     * On success, the current user is set. On failure, the failed attempt counter
     * is incremented and the account may be locked.
     *
     * @param cardNumber the card number to authenticate
     * @param pin        the PIN to verify
     * @return the authenticated User, or null if authentication failed
     * @throws AccountLockedException if the account is locked or becomes locked
     */
    public User authenticate(String cardNumber, String pin)
            throws AccountLockedException {
        User user = userDatabase.get(cardNumber);
        if (user == null) {
            throw new AccountLockedException("Invalid card number", "error.invalidCard");
        }
        if (user.isLocked()) {
            throw new AccountLockedException("Account is locked", "error.locked");
        }
        boolean valid = false;
        try {
            valid = db.verifyPin(cardNumber, pin);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (!valid) {
            user.authenticate("wrong");
            try {
                db.updateUserState(user);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (user.isLocked()) {
                throw new AccountLockedException("Account locked after too many attempts", "error.lockedAttempts");
            }
            return null;
        }
        user.unlock();
        try {
            db.updateUserState(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        currentUser = user;
        return user;
    }

    /**
     * Logs out the current user by setting currentUser to null.
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Returns the currently authenticated user.
     *
     * @return the current user, or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns the entire in-memory user database.
     *
     * @return a map of card numbers to User objects
     */
    public Map<String, User> getUserDatabase() {
        return userDatabase;
    }

    /**
     * Persists the given account's balance to the database.
     *
     * @param account the account to persist
     */
    public void persistAccount(Account account) {
        try {
            db.updateAccountBalance(account);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Persists the given user's lock state and failed attempts to the database.
     *
     * @param user the user to persist
     */
    public void persistUserState(User user) {
        try {
            db.updateUserState(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
