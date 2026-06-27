/**
 * Represents a bank account with a balance, currency, and type (chequing or savings).
 * Supports withdraw and deposit operations.
 */
public class Account {
    /** The unique account number. */
    private String accountNumber;
    /** The current balance. */
    private double balance;
    /** The currency code (CAD, USD, EUR). */
    private String currency;
    /** The account type (chequing or savings). */
    private String accountType;

    /** Canadian Dollar currency constant. */
    public static final String CAD = "CAD";
    /** US Dollar currency constant. */
    public static final String USD = "USD";
    /** Euro currency constant. */
    public static final String EUR = "EUR";

    /**
     * Constructs a new Account.
     *
     * @param accountNumber the unique account number
     * @param balance       the initial balance
     * @param currency      the currency code
     * @param accountType   the account type (chequing or savings)
     */
    public Account(String accountNumber, double balance, String currency, String accountType) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.accountType = accountType;
    }

    /**
     * Withdraws the specified amount from this account.
     *
     * @param amount the amount to withdraw
     * @return true if the withdrawal was successful, false if insufficient funds or invalid amount
     */
    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        return true;
    }

    /**
     * Deposits the specified amount into this account.
     *
     * @param amount the amount to deposit (must be positive)
     */
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    /**
     * Returns the account number.
     *
     * @return the account number string
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Returns the current balance.
     *
     * @return the balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Returns the currency code.
     *
     * @return the currency string (e.g., "CAD")
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Returns the account type.
     *
     * @return the account type string (e.g., "chequing")
     */
    public String getAccountType() {
        return accountType;
    }
}
