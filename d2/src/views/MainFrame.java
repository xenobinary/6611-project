import javax.swing.*;
import java.awt.*;

/**
 * The main application window for the iBank ABM system.
 * Acts as the top-level container, manages navigation between views via CardLayout,
 * and holds references to shared controllers (authentication, transactions, i18n).
 */
public class MainFrame extends JFrame implements Router, I18nController.I18nListener {
    /** The internationalization controller for localized text. */
    private I18nController i18n;
    /** The authentication controller shared across views. */
    private AuthenticationController auth;
    /** The transaction controller shared across views. */
    private TransactionController txn;

    /** The central panel using CardLayout for view switching. */
    private JPanel centerPanel;
    /** The CardLayout used for switching between views. */
    private CardLayout centerCards;
    /** The title label in the header bar. */
    private JLabel titleLabel;
    
    /** The login view panel. */
    private LoginPanel loginPanel;
    /** The client dashboard view panel. */
    private ClientDashboardPanel clientPanel;
    /** The admin dashboard view panel. */
    private AdminDashboardPanel adminPanel;
    /** The technician view panel. */
    private TechnicianPanel techPanel;
    /** The language selector view panel. */
    private LanguageSelectorPanel languagePanel;

    /**
     * Constructs the main frame, initializes controllers, builds the UI,
     * and navigates to the login screen.
     */
    public MainFrame() {
        auth = new AuthenticationController();
        txn = new TransactionController();
        i18n = I18nController.getInstance();
        i18n.addListener(this);
        buildUI();
        navigateTo("login");
    }

    /** Builds the complete UI: header bar + card-layout center + all panels. */
    private void buildUI() {
        setTitle("iBank ABM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(740, 640);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel shell = new JPanel(new BorderLayout(0, 0));
        shell.add(header(), BorderLayout.NORTH);
        
        centerCards = new CardLayout();
        centerPanel = new JPanel(centerCards);
        
        loginPanel = new LoginPanel(this);
        clientPanel = new ClientDashboardPanel(this);
        adminPanel = new AdminDashboardPanel(this);
        techPanel = new TechnicianPanel(this);
        languagePanel = new LanguageSelectorPanel(this);
        
        centerPanel.add(loginPanel, "login");
        centerPanel.add(clientPanel, "client");
        centerPanel.add(adminPanel, "admin");
        centerPanel.add(techPanel, "tech");
        centerPanel.add(languagePanel, "language");

        shell.add(centerPanel, BorderLayout.CENTER);
        setContentPane(shell);
    }

    /** Creates the top header bar with the application title. */
    private JPanel header() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0, 51, 102));
        p.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        titleLabel = new JLabel("iBank ABM");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        p.add(titleLabel, BorderLayout.CENTER);
        return p;
    }

    @Override
    public void navigateTo(String viewName) {
        if ("client_restore".equals(viewName)) {
            centerCards.show(centerPanel, "client");
            clientPanel.restoreState(
                languagePanel.getPreLangAction(),
                languagePanel.getPreLangAcc(),
                languagePanel.getPreLangDest(),
                languagePanel.isPreLangHistory(),
                languagePanel.isPreLangBalance()
            );
            return;
        }
        
        centerCards.show(centerPanel, viewName);
        switch (viewName) {
            case "login": loginPanel.onShow(); break;
            case "client": clientPanel.onShow(); break;
            case "admin": adminPanel.onShow(); break;
            case "tech": techPanel.onShow(); break;
        }
    }

    @Override
    public void showLanguageSelector(String previousState, String action, Account selectedAcc, Account destAcc, boolean history, boolean balance) {
        languagePanel.configure(previousState, action, selectedAcc, destAcc, history, balance);
        centerCards.show(centerPanel, "language");
        languagePanel.onShow();
    }

    @Override
    public AuthenticationController getAuthController() {
        return auth;
    }

    @Override
    public TransactionController getTransactionController() {
        return txn;
    }

    @Override
    public void onLanguageChanged(String language) {
        titleLabel.setText(i18n.get("title.app", "iBank ABM"));
    }

    /**
     * Application entry point. Initializes the i18n controller and launches the GUI.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        I18nController.getInstance();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
