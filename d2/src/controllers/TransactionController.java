import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller that handles all financial transactions in the ABM system.
 * Manages withdraw, deposit, and transfer operations with business rule
 * enforcement (daily limits, per-transaction caps, currency restrictions)
 * and maintains an in-memory transaction history for the current session.
 */
public class TransactionController {
    /** In-memory list of transaction records for the current user session. */
    private List<TransactionRecord> transactionHistory;
    /** Reference to the database manager for persistence. */
    private DatabaseManager db;
    /** The card number of the currently authenticated user. */
    private String currentCardNumber;

    /** Maximum withdrawal amount per transaction. */
    private static final double MAX_WITHDRAW_PER_TXN = 500.0;
    /** Maximum total withdrawal amount per day. */
    private static final double MAX_WITHDRAW_PER_DAY = 2000.0;
    /** Maximum deposit amount per transaction. */
    private static final double MAX_DEPOSIT_PER_TXN = 2000.0;
    /** Maximum total deposit amount per day. */
    private static final double MAX_DEPOSIT_PER_DAY = 5000.0;

    /**
     * Constructs a new TransactionController with an empty history.
     */
    public TransactionController() {
        db = DatabaseManager.getInstance();
        transactionHistory = new ArrayList<>();
    }

    /**
     * Sets the current user and loads their transaction history from the database.
     *
     * @param cardNumber the card number of the authenticated user
     */
    public void setCurrentUser(String cardNumber) {
        this.currentCardNumber = cardNumber;
        transactionHistory.clear();
        try {
            transactionHistory = db.loadTransactions(cardNumber);
        } catch (SQLException e) {
            transactionHistory = new ArrayList<>();
        }
    }

    /**
     * Performs a withdrawal from the specified account.
     * Enforces multiple-of-20, per-transaction cap, daily limit, CAD-only,
     * sufficient balance, and cash availability rules.
     *
     * @param account      the account to withdraw from
     * @param amount       the amount to withdraw
     * @param actionLabel  the localized label for the withdrawal action
     * @param cashLowLabel the localized label for the low-cash warning
     * @param fromWord     the localized word for "from"
     * @param toWord       the localized word for "to"
     * @return a formatted result string
     * @throws InsufficientFundException if the account has insufficient funds
     * @throws InvalidAmountException    if the amount violates business rules
     */
    public String withdraw(Account account, double amount,
            String actionLabel, String cashLowLabel,
            String fromWord, String toWord)
            throws InsufficientFundException, InvalidAmountException {
        if (amount <= 0 || amount % 20 != 0) {
            throw new InvalidAmountException("Amount must be a multiple of 20",
                    "error.multipleOf20");
        }
        if (amount > MAX_WITHDRAW_PER_TXN) {
            throw new InvalidAmountException("Maximum withdrawal per transaction: $500",
                    "error.maxPerTxnW");
        }
        double dailyW = 0;
        try { dailyW = db.getDailyTotal(currentCardNumber, "withdraw"); } catch (SQLException e) {}
        if (dailyW + amount > MAX_WITHDRAW_PER_DAY) {
            throw new InvalidAmountException("Daily withdrawal limit reached: $2,000",
                    "error.maxPerDayW");
        }
        if (!"CAD".equals(account.getCurrency())) {
            throw new InvalidAmountException("Only CAD accounts can be withdrawn",
                    "error.cadOnly");
        }
        if (amount > account.getBalance()) {
            throw new InsufficientFundException("Insufficient funds",
                    account.getBalance(), amount);
        }
        CashBox cashBox = CashBox.getInstance();
        if (!cashBox.hasSufficientCash(amount)) {
            throw new InvalidAmountException(
                    "Cash box has insufficient funds. Available: "
                            + String.format("%.2f", cashBox.getCurrentCash()),
                    "error.cashInsufficient");
        }
        account.withdraw(amount);
        cashBox.dispense(amount);
        TransactionRecord r = new TransactionRecord(
                "withdraw", amount, account.getCurrency(),
                account.getAccountNumber(), null, 0, null, currentCardNumber);
        transactionHistory.add(r);
        try { 
            db.updateAccountBalance(account);
            db.saveTransaction(r); 
        } catch (SQLException e) {}
        String status = cashBox.isLow() ? " " + cashLowLabel : "";
        return r.format(actionLabel, "", "", fromWord, toWord) + status;
    }

    /**
     * Performs a deposit into the specified account.
     * Enforces multiple-of-20, per-transaction cap, daily limit, and CAD-only rules.
     *
     * @param account     the account to deposit into
     * @param amount      the amount to deposit
     * @param actionLabel the localized label for the deposit action
     * @param fromWord    the localized word for "from"
     * @param toWord      the localized word for "to"
     * @return a formatted result string
     * @throws InvalidAmountException if the amount violates business rules
     */
    public String deposit(Account account, double amount, String actionLabel,
            String fromWord, String toWord)
            throws InvalidAmountException {
        if (amount <= 0 || amount % 20 != 0) {
            throw new InvalidAmountException("Amount must be a multiple of 20",
                    "error.multipleOf20");
        }
        if (amount > MAX_DEPOSIT_PER_TXN) {
            throw new InvalidAmountException("Maximum deposit per transaction: $2,000",
                    "error.maxPerTxnD");
        }
        double dailyD = 0;
        try { dailyD = db.getDailyTotal(currentCardNumber, "deposit"); } catch (SQLException e) {}
        if (dailyD + amount > MAX_DEPOSIT_PER_DAY) {
            throw new InvalidAmountException("Daily deposit limit reached: $5,000",
                    "error.maxPerDayD");
        }
        if (!"CAD".equals(account.getCurrency())) {
            throw new InvalidAmountException("Only CAD accounts can be deposited",
                    "error.cadOnly");
        }
        account.deposit(amount);
        TransactionRecord r = new TransactionRecord(
                "deposit", amount, account.getCurrency(),
                account.getAccountNumber(), null, 0, null, currentCardNumber);
        transactionHistory.add(r);
        try { 
            db.updateAccountBalance(account);
            db.saveTransaction(r); 
        } catch (SQLException e) {}
        return r.format("", actionLabel, "", fromWord, toWord);
    }

    /**
     * Performs a transfer between two accounts, with automatic currency conversion
     * if the accounts use different currencies.
     *
     * @param from        the source account
     * @param to          the destination account
     * @param amount      the amount to transfer in the source currency
     * @param actionLabel the localized label for the transfer action
     * @param fromWord    the localized word for "from"
     * @param toWord      the localized word for "to"
     * @return a formatted result string
     * @throws InsufficientFundException if the source account has insufficient funds
     * @throws InvalidAmountException    if the amount is invalid or source equals destination
     */
    public String transfer(Account from, Account to, double amount, String actionLabel,
            String fromWord, String toWord)
            throws InsufficientFundException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be positive",
                    "error.invalidAmount");
        }
        if (from == to) {
            throw new InvalidAmountException("Source and destination are the same",
                    "error.sameAccount");
        }
        if (amount > from.getBalance()) {
            throw new InsufficientFundException("Insufficient funds",
                    from.getBalance(), amount);
        }
        double convertedAmount = amount;
        if (!from.getCurrency().equals(to.getCurrency())) {
            ExchangeRateManager erm = ExchangeRateManager.getInstance();
            convertedAmount = erm.convert(amount, from.getCurrency(), to.getCurrency());
        }
        from.withdraw(amount);
        to.deposit(convertedAmount);
        TransactionRecord r = new TransactionRecord(
                "transfer", amount, from.getCurrency(),
                from.getAccountNumber(), to.getAccountNumber(),
                convertedAmount, to.getCurrency(), currentCardNumber);
        transactionHistory.add(r);
        try { 
            db.updateAccountBalance(from);
            db.updateAccountBalance(to);
            db.saveTransaction(r); 
        } catch (SQLException e) {}
        return r.format("", "", actionLabel, fromWord, toWord);
    }

    /**
     * Returns a formatted list of all transactions for the current user session.
     *
     * @param wLabel   the localized label for withdrawals
     * @param dLabel   the localized label for deposits
     * @param tLabel   the localized label for transfers
     * @param fromWord the localized word for "from"
     * @param toWord   the localized word for "to"
     * @return a list of formatted transaction strings
     */
    public List<String> getFormattedHistory(String wLabel, String dLabel,
            String tLabel, String fromWord, String toWord) {
        List<String> result = new ArrayList<>();
        for (TransactionRecord r : transactionHistory) {
            result.add(r.format(wLabel, dLabel, tLabel, fromWord, toWord));
        }
        return result;
    }
}
