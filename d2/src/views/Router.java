/**
 * Interface for navigating between views and accessing shared controllers.
 * Implemented by the main application frame to allow panels to trigger
 * navigation and retrieve authentication/transaction controllers.
 */
public interface Router {
    /**
     * Navigates to the specified view.
     *
     * @param viewName the view to show ("login", "client", "admin", "tech", "language", or "client_restore")
     */
    void navigateTo(String viewName);

    /**
     * Returns the shared authentication controller.
     *
     * @return the AuthenticationController instance
     */
    AuthenticationController getAuthController();

    /**
     * Returns the shared transaction controller.
     *
     * @return the TransactionController instance
     */
    TransactionController getTransactionController();

    /**
     * Shows the language selector, preserving pre-navigation state so the user
     * can return to where they were after changing the language.
     *
     * @param previousState the view state to return to
     * @param action        the action in progress (null if none)
     * @param selectedAcc   the selected account (null if none)
     * @param destAcc       the destination account (null if none)
     * @param history       whether the user was viewing history
     * @param balance       whether the user was viewing balances
     */
    void showLanguageSelector(String previousState, String action, Account selectedAcc, Account destAcc, boolean history, boolean balance);
}
