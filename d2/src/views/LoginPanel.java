import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Login panel displaying a card selector dropdown and PIN entry via the numpad.
 * Authenticates users and routes them to the appropriate dashboard
 * (client, admin, or technician) based on their role.
 */
public class LoginPanel extends BaseViewPanel implements NumpadListener {
    /** Dropdown for selecting a bank card. */
    private JComboBox<String> cardCombo;
    /** Label showing the entered PIN as masked characters. */
    private JLabel pinLbl;
    /** Label for displaying login error messages. */
    private JLabel loginMsg;
    /** Label for the card selector instruction. */
    private JLabel cardLbl;
    /** Label for the PIN entry instruction. */
    private JLabel pinPromptLbl;
    /** Buffer for the entered PIN digits. */
    private String pinBuf = "";
    
    /** Display strings for the card combo options. */
    private String[] cardOpts;
    /** Internal card number values corresponding to combo options. */
    private String[] cardVals;

    /**
     * Constructs a LoginPanel.
     *
     * @param router the router for navigation and controller access
     */
    public LoginPanel(Router router) {
        super(router);
        numpadPanel.addNumpadListener(this);
    }

    @Override
    protected void buildCenter(JPanel centerPanel) {
        cardOpts = new String[]{
                "4532 7891 2345 1001", "5214 6987 3456 1002",
                "6011 5544 3322 9999", "3782 8224 6310 8888"
        };
        cardVals = new String[]{"10001", "10002", "99999", "88888"};

        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createEmptyBorder(35, 55, 20, 55));
        
        cardLbl = new JLabel(i18n.get("label.selectCard", "Select Card"));
        cardLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cardLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(cardLbl); c.add(Box.createVerticalStrut(8));
        
        cardCombo = new JComboBox<>(cardOpts);
        cardCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        cardCombo.setFont(new Font("Monospaced", Font.PLAIN, 16));
        cardCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(cardCombo); c.add(Box.createVerticalStrut(20));
        
        pinPromptLbl = new JLabel(i18n.get("label.pin", "Enter PIN") + ":");
        pinPromptLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        pinPromptLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(pinPromptLbl); c.add(Box.createVerticalStrut(8));
        
        pinLbl = new JLabel(i18n.get("label.pinPlaceholder", "_ _ _ _"));
        pinLbl.setFont(new Font("Monospaced", Font.BOLD, 24));
        pinLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(pinLbl); c.add(Box.createVerticalStrut(15));
        
        loginMsg = new JLabel("", SwingConstants.CENTER);
        loginMsg.setFont(new Font("SansSerif", Font.BOLD, 13));
        loginMsg.setForeground(new Color(180, 40, 40));
        loginMsg.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(loginMsg);
        
        centerPanel.add(c, BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        leftPanel.setVisible(false);
        rightPanel.setVisible(true);
        for(int i = 0; i < 4; i++) {
            rightBtns[i].setVisible(false);
        }
        rightBtns[4].setVisible(true);
        rightBtns[4].setText(i18n.getCurrentLanguage().toUpperCase());
        
        clearListeners(rightBtns[4]);
        rightBtns[4].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                router.showLanguageSelector("login", null, null, null, false, false);
            }
        });
        
        cardCombo.setSelectedIndex(0); 
        pinBuf = "";
        cardLbl.setText(i18n.get("label.selectCard", "Select Card"));
        pinPromptLbl.setText(i18n.get("label.pin", "Enter PIN") + ":");
        pinLbl.setText(i18n.get("label.pinPlaceholder", "_ _ _ _"));
        loginMsg.setText("");
        loginMsg.setForeground(new Color(180, 40, 40));
        
        numpadPanel.setOkText(i18n.get("button.ok", "OK"));
        numpadPanel.setCancelText(i18n.get("button.cancel", "Cancel"));
        
        centerTitle.setText("");
        centerTitle.setVisible(false);
        centerText.setText("");
        centerText.setVisible(false);
    }

    @Override
    public void onLanguageChanged(String language) {
        if (isVisible()) {
            rightBtns[4].setText(language.toUpperCase());
            cardLbl.setText(i18n.get("label.selectCard", "Select Card"));
            pinPromptLbl.setText(i18n.get("label.pin", "Enter PIN") + ":");
            if (pinBuf.isEmpty()) {
                pinLbl.setText(i18n.get("label.pinPlaceholder", "_ _ _ _"));
            }
            numpadPanel.setOkText(i18n.get("button.ok", "OK"));
            numpadPanel.setCancelText(i18n.get("button.cancel", "Cancel"));
        }
    }

    @Override
    public void onNumber(int n) {
        if (pinBuf.length() < 6) { 
            pinBuf += n; 
            refreshPin(); 
        }
    }

    @Override
    public void onDot() {}

    @Override
    public void onClear() {
        pinBuf = ""; 
        pinLbl.setText(i18n.get("label.pinPlaceholder", "_ _ _ _")); 
    }

    @Override
    public void onDelete() {
        if (pinBuf.length() > 0) { 
            pinBuf = pinBuf.substring(0, pinBuf.length() - 1); 
            refreshPin(); 
        }
    }

    @Override
    public void onOk() {
        int idx = cardCombo.getSelectedIndex();
        if (idx < 0) { 
            loginMsg.setText(i18n.get("error.emptyCard", "Select a card")); 
            return; 
        }
        tryLogin(cardVals[idx]);
    }

    @Override
    public void onCancel() {
        pinBuf = ""; 
        pinLbl.setText(i18n.get("label.pinPlaceholder", "_ _ _ _")); 
        loginMsg.setText(""); 
    }

    /** Updates the PIN display with asterisks for entered digits and underscores for remaining. */
    private void refreshPin() {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < pinBuf.length(); j++) sb.append("* ");
        for (int j = pinBuf.length(); j < 4; j++) sb.append("_ ");
        pinLbl.setText(sb.toString().trim());
    }

    /**
     * Attempts to authenticate the user and navigates to the appropriate dashboard.
     *
     * @param cn the card number to authenticate with
     */
    private void tryLogin(String cn) {
        AuthenticationController auth = router.getAuthController();
        try {
            User u = auth.authenticate(cn, pinBuf);
            if (u == null) {
                User x = auth.getUserDatabase().get(cn);
                if (x != null && !x.canBeLocked()) {
                    loginMsg.setText(i18n.get("error.invalidPin", "Invalid PIN"));
                } else {
                    int rem = 3;
                    if (x != null) rem = 3 - x.getFailedAttempts();
                    loginMsg.setText(i18n.get("error.invalidPin", "Invalid PIN") + " (" + rem + " "
                            + i18n.get("label.attemptsLeft", "left") + ")");
                }
                pinBuf = ""; pinLbl.setText(i18n.get("label.pinPlaceholder", "_ _ _ _"));
                return;
            }
            pinBuf = "";
            router.getTransactionController().setCurrentUser(u.getCardNumber());
            
            if (u instanceof SystemAdmin) {
                router.navigateTo("admin");
            } else if (u instanceof Technician) {
                router.navigateTo("tech");
            } else {
                router.navigateTo("client");
            }
        } catch (AccountLockedException ex) {
            loginMsg.setText(i18n.get(ex.getErrorKey(), ex.getMessage()));
        }
    }
}
