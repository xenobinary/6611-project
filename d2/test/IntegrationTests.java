import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Map;

public class IntegrationTests {

    private DatabaseManager db;
    private AuthenticationController auth;
    private TransactionController txn;

    @Before
    public void setUp() throws Exception {
        new java.io.File(System.getProperty("user.dir") + "/ibank.db").delete();
        DatabaseManager.resetInstance();
        ExchangeRateManager.resetInstance();
        CashBox.resetInstance();
        I18nController.resetInstance();
        db = DatabaseManager.getInstance();
        auth = new AuthenticationController();
        txn = new TransactionController();
    }

    @Test
    public void testAuthSuccess() throws Exception {
        User user = auth.authenticate("10001", "1234");
        assertNotNull(user);
        assertEquals("Alice", user.getUserName());
        assertTrue(user instanceof BankClient);
    }

    @Test
    public void testAuthWrongPIN() throws Exception {
        User user = auth.authenticate("10001", "0000");
        assertNull(user);
        User u = auth.getUserDatabase().get("10001");
        assertEquals(1, u.getFailedAttempts());
    }

    @Test(expected = AccountLockedException.class)
    public void testAuthLockout() throws Exception {
        auth.authenticate("10001", "0000");
        auth.authenticate("10001", "0000");
        auth.authenticate("10001", "0000");
    }

    @Test
    public void testAuthInvalidCard() {
        try {
            auth.authenticate("00000", "1234");
            fail("Expected AccountLockedException");
        } catch (AccountLockedException e) {
            assertEquals("error.invalidCard", e.getErrorKey());
        }
    }

    @Test
    public void testLoadUsers() throws Exception {
        Map<String, User> users = auth.getUserDatabase();
        assertEquals(4, users.size());
        assertTrue(users.containsKey("10001"));
        assertTrue(users.containsKey("10002"));
        assertTrue(users.containsKey("99999"));
        assertTrue(users.containsKey("88888"));
    }

    @Test
    public void testLoadAccounts() throws Exception {
        BankClient client = (BankClient) auth.getUserDatabase().get("10001");
        assertEquals(4, client.getAccounts().size());
        boolean hasCADChequing = false;
        for (Account a : client.getAccounts()) {
            if (a.getAccountNumber().equals("1000101")
                    && a.getCurrency().equals("CAD")
                    && a.getAccountType().equals("chequing")) {
                hasCADChequing = true;
                assertEquals(1500.0, a.getBalance(), 0.01);
            }
        }
        assertTrue(hasCADChequing);
    }

    @Test
    public void testWithdrawSuccess() throws Exception {
        txn.setCurrentUser("10001");
        BankClient client = (BankClient) auth.getUserDatabase().get("10001");
        Account acc = client.findAccount("1000101");
        double before = acc.getBalance();
        String result = txn.withdraw(acc, 100.0, "Withdrawal", "WARN",
                "from", "to");
        assertTrue(result.contains("Withdrawal"));
        assertEquals(before - 100.0, acc.getBalance(), 0.01);
    }

    @Test
    public void testWithdrawNotMultipleOf20() throws Exception {
        txn.setCurrentUser("10001");
        BankClient client = (BankClient) auth.getUserDatabase().get("10001");
        Account acc = client.findAccount("1000101");
        try {
            txn.withdraw(acc, 25.0, "W", "", "from", "to");
            fail("Expected InvalidAmountException");
        } catch (InvalidAmountException e) {
            assertEquals("error.multipleOf20", e.getErrorKey());
        }
    }

    @Test
    public void testWithdrawInsufficientFunds() throws Exception {
        txn.setCurrentUser("10001");
        Account acc = new Account("test", 50.0, "CAD", "chequing");
        try {
            txn.withdraw(acc, 100.0, "W", "", "from", "to");
            fail("Expected InsufficientFundException");
        } catch (InsufficientFundException e) {
            assertEquals(50.0, e.getAvailable(), 0.01);
            assertEquals(100.0, e.getRequested(), 0.01);
        }
    }

    @Test
    public void testWithdrawExceedsPerTransactionLimit() throws Exception {
        txn.setCurrentUser("10001");
        BankClient client = (BankClient) auth.getUserDatabase().get("10001");
        Account acc = client.findAccount("1000101");
        try {
            txn.withdraw(acc, 600.0, "W", "", "from", "to");
            fail("Expected InvalidAmountException");
        } catch (InvalidAmountException e) {
            assertEquals("error.maxPerTxnW", e.getErrorKey());
        }
    }

    @Test
    public void testWithdrawNonCAD() throws Exception {
        txn.setCurrentUser("10001");
        BankClient client = (BankClient) auth.getUserDatabase().get("10001");
        Account usd = client.findAccount("1000103");
        try {
            txn.withdraw(usd, 20.0, "W", "", "from", "to");
            fail("Expected InvalidAmountException");
        } catch (InvalidAmountException e) {
            assertEquals("error.cadOnly", e.getErrorKey());
        }
    }

    @Test
    public void testDepositSuccess() throws Exception {
        txn.setCurrentUser("10001");
        BankClient client = (BankClient) auth.getUserDatabase().get("10001");
        Account acc = client.findAccount("1000101");
        double before = acc.getBalance();
        String result = txn.deposit(acc, 200.0, "Deposit", "from", "to");
        assertTrue(result.contains("Deposit"));
        assertEquals(before + 200.0, acc.getBalance(), 0.01);
    }

    @Test
    public void testTransferSuccess() throws Exception {
        txn.setCurrentUser("10001");
        BankClient client = (BankClient) auth.getUserDatabase().get("10001");
        Account from = client.findAccount("1000101");
        Account to = client.findAccount("1000102");
        double fromBefore = from.getBalance();
        double toBefore = to.getBalance();
        String result = txn.transfer(from, to, 100.0, "Transfer", "from", "to");
        assertTrue(result.contains("Transfer"));
        assertEquals(fromBefore - 100.0, from.getBalance(), 0.01);
        assertEquals(toBefore + 100.0, to.getBalance(), 0.01);
    }

    @Test
    public void testTransferCrossCurrency() throws Exception {
        txn.setCurrentUser("10001");
        BankClient client = (BankClient) auth.getUserDatabase().get("10001");
        Account cad = client.findAccount("1000101");
        Account usd = client.findAccount("1000103");
        double cadBefore = cad.getBalance();
        double usdBefore = usd.getBalance();
        txn.transfer(cad, usd, 100.0, "Transfer", "from", "to");
        assertEquals(cadBefore - 100.0, cad.getBalance(), 0.01);
        assertTrue(usd.getBalance() > usdBefore);
    }

    @Test
    public void testTransferSameAccount() throws Exception {
        txn.setCurrentUser("10001");
        BankClient client = (BankClient) auth.getUserDatabase().get("10001");
        Account acc = client.findAccount("1000101");
        try {
            txn.transfer(acc, acc, 100.0, "T", "from", "to");
            fail("Expected InvalidAmountException");
        } catch (InvalidAmountException e) {
            assertEquals("error.sameAccount", e.getErrorKey());
        }
    }

    @Test
    public void testTransactionPersistence() throws Exception {
        txn.setCurrentUser("10001");
        BankClient client = (BankClient) auth.getUserDatabase().get("10001");
        Account acc = client.findAccount("1000101");
        txn.withdraw(acc, 20.0, "Withdrawal", "", "from", "to");
        List<String> history = txn.getFormattedHistory(
                "Withdrawal", "Deposit", "Transfer", "from", "to");
        assertTrue(history.size() > 0);
        assertTrue(history.get(history.size() - 1).contains("Withdrawal"));
    }

    @Test
    public void testTransactionPerUser() throws Exception {
        txn.setCurrentUser("10001");
        BankClient alice = (BankClient) auth.getUserDatabase().get("10001");
        txn.withdraw(alice.findAccount("1000101"), 20.0, "W", "", "from", "to");
        List<String> aliceHistory = txn.getFormattedHistory(
                "W", "D", "T", "from", "to");
        assertTrue(aliceHistory.size() > 0);
        txn.setCurrentUser("10002");
        List<String> bobHistory = txn.getFormattedHistory(
                "W", "D", "T", "from", "to");
        assertEquals(0, bobHistory.size());
    }

    @Test
    public void testAdminAuthenticate() throws Exception {
        User admin = auth.authenticate("99999", "0000");
        assertNotNull(admin);
        assertTrue(admin instanceof SystemAdmin);
    }

    @Test
    public void testTechnicianAuthenticate() throws Exception {
        User tech = auth.authenticate("88888", "0000");
        assertNotNull(tech);
        assertTrue(tech instanceof Technician);
    }

    @Test
    public void testExchangeRateUpdate() throws Exception {
        ExchangeRateManager erm = ExchangeRateManager.getInstance();
        erm.setRate("CAD", "USD", 0.85);
        assertEquals(0.85, erm.getRate("CAD", "USD"), 0.01);
    }

    @Test
    public void testCashBoxWithdrawBlock() throws Exception {
        CashBox.resetInstance();
        CashBox cb = CashBox.getInstance();
        cb.dispense(20000.0);
        assertTrue(cb.isEmpty());
        txn.setCurrentUser("10001");
        BankClient client = (BankClient) auth.getUserDatabase().get("10001");
        Account acc = client.findAccount("1000101");
        try {
            txn.withdraw(acc, 20.0, "W", "", "from", "to");
            fail("Expected InvalidAmountException for empty cash box");
        } catch (InvalidAmountException e) {
            assertEquals("error.cashInsufficient", e.getErrorKey());
        }
    }

    @Test
    public void testAccountBalancePersistence() throws Exception {
        BankClient client = (BankClient) auth.getUserDatabase().get("10001");
        Account acc = client.findAccount("1000101");
        double before = acc.getBalance();
        txn.setCurrentUser("10001");
        txn.withdraw(acc, 20.0, "W", "", "from", "to");
        auth.persistAccount(acc);
        DatabaseManager.resetInstance();
        DatabaseManager db2 = DatabaseManager.getInstance();
        Map<String, User> users2 = db2.loadUsers();
        BankClient client2 = (BankClient) users2.get("10001");
        Account acc2 = client2.findAccount("1000101");
        assertEquals(before - 20.0, acc2.getBalance(), 0.01);
    }
}
