import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationController {
    private Map<String, User> userDatabase;
    private User currentUser;
    private DatabaseManager db;

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

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Map<String, User> getUserDatabase() {
        return userDatabase;
    }

    public void persistAccount(Account account) {
        try {
            db.updateAccountBalance(account);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void persistUserState(User user) {
        try {
            db.updateUserState(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
