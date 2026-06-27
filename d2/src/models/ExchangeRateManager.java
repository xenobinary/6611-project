import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton manager for currency exchange rates.
 * Loads rates from the database and provides conversion between currencies.
 * Supports forward rates (e.g., CAD:USD) and automatically computes reverse rates.
 */
public class ExchangeRateManager {
    /** The singleton instance. */
    private static ExchangeRateManager instance;
    /** Map of currency pair keys (e.g., "CAD:USD") to exchange rates. */
    private Map<String, Double> rates;
    /** Reference to the database manager for persistence. */
    private DatabaseManager db;

    /** Private constructor; loads rates from the database. */
    private ExchangeRateManager() {
        db = DatabaseManager.getInstance();
        rates = new HashMap<>();
        try {
            Map<String, Double> loaded = db.loadExchangeRates();
            if (!loaded.isEmpty()) {
                rates.putAll(loaded);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the singleton ExchangeRateManager instance.
     *
     * @return the instance
     */
    public static ExchangeRateManager getInstance() {
        if (instance == null) {
            instance = new ExchangeRateManager();
        }
        return instance;
    }

    /**
     * Returns the exchange rate from one currency to another.
     * If a reverse rate exists but no forward rate, computes the reciprocal.
     * Returns 1.0 for same-currency or unknown pairs.
     *
     * @param fromCurrency the source currency code
     * @param toCurrency   the target currency code
     * @return the exchange rate
     */
    public double getRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return 1.0;
        }
        String key = fromCurrency + ":" + toCurrency;
        Double rate = rates.get(key);
        if (rate == null || rate == 0.0) {
            String reverseKey = toCurrency + ":" + fromCurrency;
            Double reverseRate = rates.get(reverseKey);
            if (reverseRate != null && reverseRate > 0) {
                return 1.0 / reverseRate;
            }
            return 1.0;
        }
        return rate;
    }

    /**
     * Sets or updates the exchange rate for a currency pair and persists it.
     * Also sets the reverse pair rate to 0.0 to indicate it should be computed.
     *
     * @param fromCurrency the source currency code
     * @param toCurrency   the target currency code
     * @param rate         the new exchange rate
     */
    public void setRate(String fromCurrency, String toCurrency, double rate) {
        String key = fromCurrency + ":" + toCurrency;
        rates.put(key, rate);
        String reverseKey = toCurrency + ":" + fromCurrency;
        rates.put(reverseKey, 0.0);
        try {
            db.updateExchangeRate(key, rate);
            db.updateExchangeRate(reverseKey, 0.0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts an amount from one currency to another using the stored exchange rate.
     *
     * @param amount        the amount in the source currency
     * @param fromCurrency  the source currency code
     * @param toCurrency    the target currency code
     * @return the converted amount
     */
    public double convert(double amount, String fromCurrency, String toCurrency) {
        return amount * getRate(fromCurrency, toCurrency);
    }

    /**
     * Returns all configured exchange rates (pairs with a positive rate).
     *
     * @return a map of currency pair keys to rates
     */
    public Map<String, Double> getConfiguredRates() {
        Map<String, Double> configured = new HashMap<>();
        for (Map.Entry<String, Double> entry : rates.entrySet()) {
            if (entry.getValue() > 0.0) {
                configured.put(entry.getKey(), entry.getValue());
            }
        }
        return configured;
    }

    /**
     * Resets the singleton instance (mainly for testing).
     */
    public static void resetInstance() {
        instance = null;
    }
}
