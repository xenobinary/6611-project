import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank client user. A BankClient owns one or more {@link Account}s
 * across different currencies and can perform transactions (withdraw, deposit, transfer).
 */
public class BankClient extends User {
    /** The list of accounts owned by this client. */
    private List<Account> accounts;

    /**
     * Constructs a new BankClient.
     *
     * @param cardNumber the card number for identification
     * @param pin        the plaintext PIN
     * @param userName   the display name
     */
    public BankClient(String cardNumber, String pin, String userName) {
        super(cardNumber, pin, userName);
        this.accounts = new ArrayList<>();
    }

    /**
     * Adds an account to this client's portfolio.
     *
     * @param account the account to add
     */
    public void addAccount(Account account) {
        accounts.add(account);
    }

    /**
     * Returns all accounts owned by this client.
     *
     * @return the list of accounts
     */
    public List<Account> getAccounts() {
        return accounts;
    }

    /**
     * Finds an account by its account number.
     *
     * @param accountNumber the account number to search for
     * @return the matching Account, or null if not found
     */
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
