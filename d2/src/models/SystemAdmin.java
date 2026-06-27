/**
 * Represents a system administrator user who can unlock locked client accounts
 * and manage exchange rates. System admins cannot be locked out after failed logins.
 */
public class SystemAdmin extends User {
    /**
     * Constructs a new SystemAdmin.
     *
     * @param cardNumber the card number for identification
     * @param pin        the plaintext PIN
     * @param userName   the display name
     */
    public SystemAdmin(String cardNumber, String pin, String userName) {
        super(cardNumber, pin, userName);
    }

    /**
     * Unlocks the specified user account.
     *
     * @param user the user to unlock (may be null, in which case nothing happens)
     */
    public void unlockUser(User user) {
        if (user != null) {
            user.unlock();
        }
    }

    @Override
    public boolean canBeLocked() {
        return false;
    }

    @Override
    public String getRole() {
        return "SystemAdmin";
    }
}
