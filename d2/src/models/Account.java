public class Account {
    private String accountNumber;
    private double balance;
    private String currency;
    private String accountType;

    public static final String CAD = "CAD";
    public static final String USD = "USD";
    public static final String EUR = "EUR";

    public Account(String accountNumber, double balance, String currency, String accountType) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.accountType = accountType;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        return true;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountType() {
        return accountType;
    }
}
