public class Technician extends User {
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
