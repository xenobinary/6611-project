import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame implements Router, I18nController.I18nListener {
    private I18nController i18n;
    private AuthenticationController auth;
    private TransactionController txn;

    private JPanel centerPanel;
    private CardLayout centerCards;
    private JLabel titleLabel;
    
    private LoginPanel loginPanel;
    private ClientDashboardPanel clientPanel;
    private AdminDashboardPanel adminPanel;
    private TechnicianPanel techPanel;
    private LanguageSelectorPanel languagePanel;

    public MainFrame() {
        auth = new AuthenticationController();
        txn = new TransactionController();
        i18n = I18nController.getInstance();
        i18n.addListener(this);
        buildUI();
        navigateTo("login");
    }

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

    public static void main(String[] args) {
        I18nController.getInstance();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
