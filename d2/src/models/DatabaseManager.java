import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton database manager for the iBank ABM system.
 * Handles SQLite database connection, schema creation, data seeding,
 * and all CRUD operations for users, accounts, exchange rates, and transactions.
 */
public class DatabaseManager {
    /** The singleton instance. */
    private static DatabaseManager instance;
    /** The active JDBC connection. */
    private Connection conn;
    /** The filesystem path to the SQLite database file. */
    private static final String DB_PATH = System.getProperty("user.dir") + "/ibank.db";

    /**
     * Private constructor. Initializes the SQLite connection and creates
     * the schema/seed data if the database file does not already exist.
     */
    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            boolean isNew = !new File(DB_PATH).exists();
            conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            conn.createStatement().execute("PRAGMA foreign_keys = ON");
            if (isNew) {
                createSchema();
                seedData();
            }
        } catch (Exception e) {
            throw new RuntimeException("Database init failed", e);
        }
    }

    /**
     * Returns the singleton DatabaseManager instance.
     *
     * @return the instance
     */
    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    /**
     * Resets the singleton instance (mainly for testing).
     */
    public static void resetInstance() { instance = null; }

    /** Creates the database schema (users, accounts, exchange_rates, transactions tables). */
    private void createSchema() throws SQLException {
        Statement s = conn.createStatement();
        s.execute("CREATE TABLE users ("
                + "card_number TEXT PRIMARY KEY,"
                + "pin_hash TEXT NOT NULL,"
                + "user_name TEXT NOT NULL,"
                + "role TEXT NOT NULL,"
                + "locked INTEGER DEFAULT 0,"
                + "failed_attempts INTEGER DEFAULT 0)");
        s.execute("CREATE TABLE accounts ("
                + "account_number TEXT PRIMARY KEY,"
                + "card_number TEXT NOT NULL,"
                + "balance REAL NOT NULL,"
                + "currency TEXT NOT NULL,"
                + "account_type TEXT NOT NULL,"
                + "FOREIGN KEY(card_number) REFERENCES users(card_number))");
        s.execute("CREATE TABLE exchange_rates ("
                + "pair TEXT PRIMARY KEY,"
                + "rate REAL NOT NULL)");
        s.execute("CREATE TABLE transactions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "card_number TEXT NOT NULL,"
                + "record TEXT NOT NULL,"
                + "created_at TEXT NOT NULL)");
    }

    /** Seeds the database with initial user, account, and exchange rate data. */
    private void seedData() throws SQLException {
        String aliceHash = hash("1234");
        String bobHash = hash("5678");
        String adminHash = hash("0000");
        String techHash = hash("0000");

        String[][] users = {
                {"10001", aliceHash, "Alice", "BankClient"},
                {"10002", bobHash, "Bob", "BankClient"},
                {"99999", adminHash, "Admin", "SystemAdmin"},
                {"88888", techHash, "Technician", "Technician"},
        };
        PreparedStatement pu = conn.prepareStatement(
                "INSERT INTO users VALUES (?,?,?,?,0,0)");
        for (String[] u : users) {
            pu.setString(1, u[0]); pu.setString(2, u[1]);
            pu.setString(3, u[2]); pu.setString(4, u[3]);
            pu.executeUpdate();
        }

        Object[][] accounts = {
                {"1000101", "10001", 1500.00, "CAD", "chequing"},
                {"1000102", "10001", 8500.00, "CAD", "savings"},
                {"1000103", "10001", 2000.00, "USD", "chequing"},
                {"1000104", "10001", 1500.00, "EUR", "savings"},
                {"1000201", "10002", 3200.00, "CAD", "chequing"},
                {"1000202", "10002", 10000.00, "CAD", "savings"},
                {"1000203", "10002", 500.00, "USD", "chequing"},
        };
        PreparedStatement pa = conn.prepareStatement(
                "INSERT INTO accounts VALUES (?,?,?,?,?)");
        for (Object[] a : accounts) {
            pa.setString(1, (String) a[0]); pa.setString(2, (String) a[1]);
            pa.setDouble(3, (Double) a[2]); pa.setString(4, (String) a[3]);
            pa.setString(5, (String) a[4]);
            pa.executeUpdate();
        }

        String[][] rates = {
                {"CAD:USD", "0.73"}, {"CAD:EUR", "0.68"}, {"EUR:USD", "1.08"}
        };
        PreparedStatement pr = conn.prepareStatement(
                "INSERT INTO exchange_rates VALUES (?,?)");
        for (String[] r : rates) {
            pr.setString(1, r[0]); pr.setDouble(2, Double.parseDouble(r[1]));
            pr.executeUpdate();
        }
    }

    /**
     * Computes the SHA-256 hash of the given input string.
     *
     * @param input the string to hash
     * @return the hexadecimal hash string
     */
    public static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads all users from the database, including their accounts for BankClients.
     *
     * @return a map of card numbers to User objects
     * @throws SQLException if a database error occurs
     */
    public Map<String, User> loadUsers() throws SQLException {
        Map<String, User> map = new HashMap<>();
        Statement s = conn.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM users");
        while (rs.next()) {
            String cn = rs.getString("card_number");
            String name = rs.getString("user_name");
            String role = rs.getString("role");
            User u;
            if ("BankClient".equals(role)) {
                u = new BankClient(cn, "", name);
                loadAccounts((BankClient) u);
            } else if ("SystemAdmin".equals(role)) {
                u = new SystemAdmin(cn, "", name);
            } else {
                u = new Technician(cn, "", name);
            }
            if (rs.getInt("locked") != 0) {
                for (int i = 0; i < 3; i++) u.authenticate("wrong");
            }
            map.put(cn, u);
        }
        return map;
    }

    /** Loads all accounts belonging to the given BankClient. */
    private void loadAccounts(BankClient client) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM accounts WHERE card_number = ?");
        ps.setString(1, client.getCardNumber());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            client.addAccount(new Account(
                    rs.getString("account_number"),
                    rs.getDouble("balance"),
                    rs.getString("currency"),
                    rs.getString("account_type")));
        }
    }

    /**
     * Verifies a PIN against the stored hash for the given card number.
     *
     * @param cardNumber the card number to check
     * @param pin        the plaintext PIN to verify
     * @return true if the PIN matches
     * @throws SQLException if a database error occurs
     */
    public boolean verifyPin(String cardNumber, String pin) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT pin_hash FROM users WHERE card_number = ?");
        ps.setString(1, cardNumber);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return hash(pin).equals(rs.getString("pin_hash"));
        }
        return false;
    }

    /**
     * Persists the locked status and failed attempt count for a user.
     *
     * @param user the user whose state to persist
     * @throws SQLException if a database error occurs
     */
    public void updateUserState(User user) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET locked=?, failed_attempts=? WHERE card_number=?");
        ps.setInt(1, user.isLocked() ? 1 : 0);
        ps.setInt(2, user.getFailedAttempts());
        ps.setString(3, user.getCardNumber());
        ps.executeUpdate();
    }

    /**
     * Persists the balance for an account.
     *
     * @param account the account whose balance to persist
     * @throws SQLException if a database error occurs
     */
    public void updateAccountBalance(Account account) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE accounts SET balance=? WHERE account_number=?");
        ps.setDouble(1, account.getBalance());
        ps.setString(2, account.getAccountNumber());
        ps.executeUpdate();
    }

    /**
     * Inserts or replaces an exchange rate for a currency pair.
     *
     * @param pair the currency pair key (e.g., "CAD:USD")
     * @param rate the exchange rate
     * @throws SQLException if a database error occurs
     */
    public void updateExchangeRate(String pair, double rate) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "INSERT OR REPLACE INTO exchange_rates VALUES (?,?)");
        ps.setString(1, pair);
        ps.setDouble(2, rate);
        ps.executeUpdate();
    }

    /**
     * Loads all exchange rates from the database.
     *
     * @return a map of currency pair keys to rates
     * @throws SQLException if a database error occurs
     */
    public Map<String, Double> loadExchangeRates() throws SQLException {
        Map<String, Double> rates = new HashMap<>();
        Statement s = conn.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM exchange_rates");
        while (rs.next()) {
            rates.put(rs.getString("pair"), rs.getDouble("rate"));
        }
        return rates;
    }

    /**
     * Saves a transaction record to the database.
     *
     * @param r the transaction record to save
     * @throws SQLException if a database error occurs
     */
    public void saveTransaction(TransactionRecord r) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO transactions (card_number, record, created_at) VALUES (?,?,?)");
        ps.setString(1, r.cardNumber);
        ps.setString(2, r.toDbString());
        ps.setString(3, java.time.LocalDateTime.now().toString());
        ps.executeUpdate();
    }

    /**
     * Loads all transaction records for a given card number.
     *
     * @param cardNumber the card number to load transactions for
     * @return a list of transaction records, ordered by insertion
     * @throws SQLException if a database error occurs
     */
    public List<TransactionRecord> loadTransactions(String cardNumber) throws SQLException {
        List<TransactionRecord> list = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT record FROM transactions WHERE card_number=? ORDER BY id");
        ps.setString(1, cardNumber);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(TransactionRecord.fromDbString(rs.getString("record")));
        }
        return list;
    }

    /**
     * Calculates the total amount for a given transaction type today for a user.
     *
     * @param cardNumber the card number to query
     * @param type       the transaction type (e.g., "withdraw")
     * @return the total amount for that type today
     * @throws SQLException if a database error occurs
     */
    public double getDailyTotal(String cardNumber, String type) throws SQLException {
        double total = 0;
        String today = java.time.LocalDate.now().toString();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT record FROM transactions WHERE card_number=? AND created_at LIKE ?");
        ps.setString(1, cardNumber);
        ps.setString(2, today + "%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            TransactionRecord r = TransactionRecord.fromDbString(rs.getString("record"));
            if (type.equals(r.type)) total += r.amount;
        }
        return total;
    }
}
