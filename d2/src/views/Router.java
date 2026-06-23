public interface Router {
    void navigateTo(String viewName);
    AuthenticationController getAuthController();
    TransactionController getTransactionController();
    void showLanguageSelector(String previousState, String action, Account selectedAcc, Account destAcc, boolean history, boolean balance);
}
