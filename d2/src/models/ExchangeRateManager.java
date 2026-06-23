import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ExchangeRateManager {
    private static ExchangeRateManager instance;
    private Map<String, Double> rates;
    private DatabaseManager db;

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

    public static ExchangeRateManager getInstance() {
        if (instance == null) {
            instance = new ExchangeRateManager();
        }
        return instance;
    }

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

    public double convert(double amount, String fromCurrency, String toCurrency) {
        return amount * getRate(fromCurrency, toCurrency);
    }

    public Map<String, Double> getConfiguredRates() {
        Map<String, Double> configured = new HashMap<>();
        for (Map.Entry<String, Double> entry : rates.entrySet()) {
            if (entry.getValue() > 0.0) {
                configured.put(entry.getKey(), entry.getValue());
            }
        }
        return configured;
    }

    public static void resetInstance() {
        instance = null;
    }
}
