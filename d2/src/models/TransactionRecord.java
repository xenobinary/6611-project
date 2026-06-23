public class TransactionRecord {
    public String type;
    public double amount;
    public String currency;
    public String fromAccount;
    public String toAccount;
    public double convertedAmount;
    public String convertedCurrency;
    public String cardNumber;

    public TransactionRecord(String type, double amount, String currency,
            String fromAccount, String toAccount,
            double convertedAmount, String convertedCurrency,
            String cardNumber) {
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.convertedAmount = convertedAmount;
        this.convertedCurrency = convertedCurrency;
        this.cardNumber = cardNumber;
    }

    public String toDbString() {
        return type + "|" + amount + "|" + currency + "|" + fromAccount + "|"
                + (toAccount != null ? toAccount : "") + "|"
                + convertedAmount + "|"
                + (convertedCurrency != null ? convertedCurrency : "") + "|"
                + cardNumber;
    }

    public static TransactionRecord fromDbString(String s) {
        String[] parts = s.split("\\|", -1);
        return new TransactionRecord(
                parts[0],
                Double.parseDouble(parts[1]),
                parts[2],
                parts[3],
                parts[4].isEmpty() ? null : parts[4],
                Double.parseDouble(parts[5]),
                parts[6].isEmpty() ? null : parts[6],
                parts.length > 7 ? parts[7] : "");
    }

    public String format(String wLabel, String dLabel, String tLabel,
            String fromWord, String toWord) {
        String label;
        if ("withdraw".equals(type)) label = wLabel;
        else if ("deposit".equals(type)) label = dLabel;
        else label = tLabel;

        if (toAccount != null) {
            return label + ": " + amount + " " + currency
                    + " " + fromWord + " " + fromAccount
                    + " " + toWord + " " + toAccount
                    + " (" + String.format("%.2f", convertedAmount) + " "
                    + convertedCurrency + ")";
        }
        return label + ": " + amount + " " + currency
                + " " + (type.equals("deposit") ? toWord : fromWord)
                + " " + fromAccount;
    }
}
