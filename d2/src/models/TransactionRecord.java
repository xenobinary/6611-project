/**
 * Represents a single financial transaction record.
 * Supports serialization to/from a database string format and formatting
 * for display in the transaction history.
 */
public class TransactionRecord {
    /** The transaction type ("withdraw", "deposit", or "transfer"). */
    public String type;
    /** The transaction amount in the source currency. */
    public double amount;
    /** The source currency code. */
    public String currency;
    /** The source account number. */
    public String fromAccount;
    /** The destination account number (null for withdraw/deposit). */
    public String toAccount;
    /** The converted amount in the target currency (for transfers). */
    public double convertedAmount;
    /** The target currency code (null for withdraw/deposit). */
    public String convertedCurrency;
    /** The card number of the user who performed the transaction. */
    public String cardNumber;

    /**
     * Constructs a new TransactionRecord.
     *
     * @param type              the transaction type
     * @param amount            the source amount
     * @param currency          the source currency
     * @param fromAccount       the source account number
     * @param toAccount         the destination account number (nullable)
     * @param convertedAmount   the converted amount (0 for non-transfer)
     * @param convertedCurrency the target currency (nullable)
     * @param cardNumber        the user's card number
     */
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

    /**
     * Serializes this record to a pipe-delimited database string.
     *
     * @return the serialized string
     */
    public String toDbString() {
        return type + "|" + amount + "|" + currency + "|" + fromAccount + "|"
                + (toAccount != null ? toAccount : "") + "|"
                + convertedAmount + "|"
                + (convertedCurrency != null ? convertedCurrency : "") + "|"
                + cardNumber;
    }

    /**
     * Deserializes a TransactionRecord from a pipe-delimited database string.
     *
     * @param s the serialized record string
     * @return a new TransactionRecord instance
     */
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

    /**
     * Formats this record as a human-readable string using localized labels.
     *
     * @param wLabel   the localized label for withdrawals
     * @param dLabel   the localized label for deposits
     * @param tLabel   the localized label for transfers
     * @param fromWord the localized word for "from"
     * @param toWord   the localized word for "to"
     * @return the formatted transaction string
     */
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
