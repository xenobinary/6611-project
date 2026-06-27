/**
 * Represents a technician user who can refill the ABM cash box.
 * Technicians cannot be locked out after failed login attempts.
 */
public class Technician extends User {
    /**
     * Constructs a new Technician.
     *
     * @param cardNumber the card number for identification
     * @param pin        the plaintext PIN
     * @param userName   the display name
     */
    public Technician(String cardNumber, String pin, String userName) {
        super(cardNumber, pin, userName);
    }

    @Override
    public boolean canBeLocked() {
        return false;
    }

    @Override
    public String getRole() {
        return "Technician";
    }
}
