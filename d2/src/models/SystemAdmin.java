public class SystemAdmin extends User {
    public SystemAdmin(String cardNumber, String pin, String userName) {
        super(cardNumber, pin, userName);
    }

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
