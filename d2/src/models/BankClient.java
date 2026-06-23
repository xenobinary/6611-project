import java.util.ArrayList;
import java.util.List;

public class BankClient extends User {
    private List<Account> accounts;

    public BankClient(String cardNumber, String pin, String userName) {
        super(cardNumber, pin, userName);
        this.accounts = new ArrayList<>();
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public Account findAccount(String accountNumber) {
        for (Account a : accounts) {
            if (a.getAccountNumber().equals(accountNumber)) {
                return a;
            }
        }
        return null;
    }

    @Override
    public String getRole() {
        return "BankClient";
    }
}
