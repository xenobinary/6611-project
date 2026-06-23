import org.junit.*;
import static org.junit.Assert.*;

public class ModelTests {

    @Test
    public void testUserAuthenticateSuccess() {
        BankClient client = new BankClient("10001", "1234", "Alice");
        assertTrue(client.authenticate("1234"));
        assertFalse(client.isLocked());
        assertEquals(0, client.getFailedAttempts());
    }

    @Test
    public void testUserAuthenticateFail() {
        BankClient client = new BankClient("10001", "1234", "Alice");
        assertFalse(client.authenticate("0000"));
        assertEquals(1, client.getFailedAttempts());
        assertFalse(client.isLocked());
    }

    @Test
    public void testUserLockoutAfterThreeFails() {
        BankClient client = new BankClient("10001", "1234", "Alice");
        client.authenticate("0000");
        client.authenticate("0000");
        assertFalse(client.isLocked());
        client.authenticate("0000");
        assertTrue(client.isLocked());
        assertEquals(3, client.getFailedAttempts());
    }

    @Test
    public void testUserUnlock() {
        BankClient client = new BankClient("10001", "1234", "Alice");
        client.authenticate("0000");
        client.authenticate("0000");
        client.authenticate("0000");
        assertTrue(client.isLocked());
        client.unlock();
        assertFalse(client.isLocked());
        assertEquals(0, client.getFailedAttempts());
    }

    @Test
    public void testAccountWithdraw() {
        Account acc = new Account("1000101", 1000.0, "CAD", "chequing");
        assertTrue(acc.withdraw(200.0));
        assertEquals(800.0, acc.getBalance(), 0.01);
    }

    @Test
    public void testAccountWithdrawInsufficientFunds() {
        Account acc = new Account("1000101", 100.0, "CAD", "chequing");
        assertFalse(acc.withdraw(200.0));
        assertEquals(100.0, acc.getBalance(), 0.01);
    }

    @Test
    public void testAccountWithdrawNegative() {
        Account acc = new Account("1000101", 500.0, "CAD", "chequing");
        assertFalse(acc.withdraw(-50.0));
        assertEquals(500.0, acc.getBalance(), 0.01);
    }

    @Test
    public void testAccountDeposit() {
        Account acc = new Account("1000101", 500.0, "CAD", "chequing");
        acc.deposit(200.0);
        assertEquals(700.0, acc.getBalance(), 0.01);
    }

    @Test
    public void testAccountDepositNegative() {
        Account acc = new Account("1000101", 500.0, "CAD", "chequing");
        acc.deposit(-100.0);
        assertEquals(500.0, acc.getBalance(), 0.01);
    }

    @Test
    public void testBankClientAccounts() {
        BankClient client = new BankClient("10001", "1234", "Alice");
        assertEquals(0, client.getAccounts().size());
        Account acc = new Account("1000101", 1000.0, "CAD", "chequing");
        client.addAccount(acc);
        assertEquals(1, client.getAccounts().size());
        assertEquals(acc, client.findAccount("1000101"));
        assertNull(client.findAccount("9999999"));
    }

    @Test
    public void testCashBoxSingleton() {
        CashBox.resetInstance();
        CashBox cb = CashBox.getInstance();
        assertFalse(cb.isEmpty());
        assertEquals(20000.0, cb.getCurrentCash(), 0.01);
        assertEquals(50000.0, cb.getMaxCapacity(), 0.01);
    }

    @Test
    public void testCashBoxDispense() {
        CashBox.resetInstance();
        CashBox cb = CashBox.getInstance();
        assertTrue(cb.hasSufficientCash(500.0));
        cb.dispense(500.0);
        assertEquals(19500.0, cb.getCurrentCash(), 0.01);
    }

    @Test
    public void testCashBoxRefill() {
        CashBox.resetInstance();
        CashBox cb = CashBox.getInstance();
        cb.dispense(10000.0);
        cb.refill();
        assertEquals(50000.0, cb.getCurrentCash(), 0.01);
    }

    @Test
    public void testCashBoxLow() {
        CashBox.resetInstance();
        CashBox cb = CashBox.getInstance();
        assertFalse(cb.isLow());
        cb.dispense(19500.0);
        assertTrue(cb.isLow());
        cb.dispense(500.0);
        assertTrue(cb.isEmpty());
    }

    @Test
    public void testExchangeRateManager() {
        ExchangeRateManager.resetInstance();
        ExchangeRateManager erm = ExchangeRateManager.getInstance();
        assertEquals(0.73, erm.getRate("CAD", "USD"), 0.01);
        assertEquals(1.0 / 0.73, erm.getRate("USD", "CAD"), 0.01);
        assertEquals(1.0, erm.getRate("CAD", "CAD"), 0.01);
    }

    @Test
    public void testExchangeRateConversion() {
        ExchangeRateManager.resetInstance();
        ExchangeRateManager erm = ExchangeRateManager.getInstance();
        erm.setRate("CAD", "USD", 0.73);
        double converted = erm.convert(100.0, "CAD", "USD");
        assertEquals(73.0, converted, 0.01);
    }

    @Test
    public void testSetExchangeRate() {
        ExchangeRateManager.resetInstance();
        ExchangeRateManager erm = ExchangeRateManager.getInstance();
        erm.setRate("CAD", "USD", 0.80);
        assertEquals(0.80, erm.getRate("CAD", "USD"), 0.01);
        assertEquals(1.0 / 0.80, erm.getRate("USD", "CAD"), 0.01);
    }

    @Test
    public void testTransactionRecordFormat() {
        TransactionRecord r = new TransactionRecord(
                "withdraw", 100.0, "CAD", "1000101", null, 0, null, "10001");
        String result = r.format("Withdrawal", "Deposit", "Transfer", "from", "to");
        assertTrue(result.contains("Withdrawal"));
        assertTrue(result.contains("100"));
        assertTrue(result.contains("1000101"));
    }

    @Test
    public void testTransactionRecordFormatTransfer() {
        TransactionRecord r = new TransactionRecord(
                "transfer", 50.0, "CAD", "1000101", "1000102",
                50.0, "CAD", "10001");
        String result = r.format("W", "D", "T", "from", "to");
        assertTrue(result.contains("T"));
        assertTrue(result.contains("from 1000101"));
        assertTrue(result.contains("to 1000102"));
    }

    @Test
    public void testTransactionRecordDbRoundtrip() {
        TransactionRecord r = new TransactionRecord(
                "deposit", 200.0, "CAD", "1000101", null, 0, null, "10001");
        String dbStr = r.toDbString();
        TransactionRecord r2 = TransactionRecord.fromDbString(dbStr);
        assertEquals(r.type, r2.type);
        assertEquals(r.amount, r2.amount, 0.01);
        assertEquals(r.currency, r2.currency);
        assertEquals(r.cardNumber, r2.cardNumber);
    }

    @Test
    public void testPINHashing() {
        String hash1 = DatabaseManager.hash("1234");
        String hash2 = DatabaseManager.hash("1234");
        String hash3 = DatabaseManager.hash("5678");
        assertEquals(hash1, hash2);
        assertNotEquals(hash1, hash3);
        assertEquals(64, hash1.length());
    }

    @Test
    public void testSystemAdminRole() {
        SystemAdmin admin = new SystemAdmin("99999", "0000", "Admin");
        assertEquals("SystemAdmin", admin.getRole());
    }

    @Test
    public void testTechnicianRole() {
        Technician tech = new Technician("88888", "0000", "Technician");
        assertEquals("Technician", tech.getRole());
    }

    @Test
    public void testSystemAdminUnlockUser() {
        BankClient client = new BankClient("10001", "1234", "Alice");
        client.authenticate("wrong");
        client.authenticate("wrong");
        client.authenticate("wrong");
        assertTrue(client.isLocked());
        SystemAdmin admin = new SystemAdmin("99999", "0000", "Admin");
        admin.unlockUser(client);
        assertFalse(client.isLocked());
        assertEquals(0, client.getFailedAttempts());
    }
}
