import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class AdminDashboardPanel extends BaseViewPanel implements NumpadListener {
    private DefaultTableModel listModel;
    private JTable listTable;
    
    private String subState = "welcome"; // welcome, rate_select, rate_input, unlock
    private StringBuilder buf = new StringBuilder("0");
    private boolean dotEntered = false;
    private String selectedPair = null;
    private List<User> lockedClients;

    public AdminDashboardPanel(Router router) {
        super(router);
        numpadPanel.addNumpadListener(this);
    }

    @Override
    protected void buildCenter(JPanel centerPanel) {
        listModel = new DefaultTableModel() {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        listTable = new JTable(listModel) {
            protected void processMouseEvent(java.awt.event.MouseEvent e) {}
            protected void processMouseMotionEvent(java.awt.event.MouseEvent e) {}
        };
        listTable.setRowHeight(28);
        listTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
        listTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        listTable.setFillsViewportHeight(true);
        listTable.setShowGrid(false);
        listTable.setIntercellSpacing(new Dimension(0, 0));
        listTable.setRowSelectionAllowed(true);
        listTable.setColumnSelectionAllowed(false);
        
        JScrollPane sp = new JScrollPane(listTable);
        sp.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.add(sp, BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        resetSideColors(new Color(102, 0, 0), new Color(150, 30, 30));
        leftPanel.setVisible(true); rightPanel.setVisible(true);

        for (int i = 0; i < BTN_COUNT; i++) {
            leftBtns[i].setVisible(false); 
            rightBtns[i].setVisible(false); 
        }
        
        leftBtns[0].setVisible(true);
        leftBtns[0].setText(h("button.updateRate", "Rate"));
        leftBtns[1].setVisible(true);
        leftBtns[1].setText(h("button.unlock", "Unlock"));
        rightBtns[1].setVisible(true);
        rightBtns[1].setText(h("button.logout", "Logout"));
        rightBtns[4].setVisible(true);
        rightBtns[4].setText(i18n.getCurrentLanguage().toUpperCase());

        clearListeners(leftBtns[0]);
        leftBtns[0].addActionListener(e -> adminRateSelect());
        clearListeners(leftBtns[1]);
        leftBtns[1].addActionListener(e -> adminUnlock());
        clearListeners(rightBtns[1]);
        rightBtns[1].addActionListener(e -> {
            router.getAuthController().logout();
            router.navigateTo("login");
        });
        clearListeners(rightBtns[4]);
        rightBtns[4].addActionListener(e -> {
            router.showLanguageSelector("admin", null, null, null, false, false);
        });

        adminDashboard();
        numpadPanel.setOkText(i18n.get("button.ok", "OK"));
        numpadPanel.setCancelText(i18n.get("button.cancel", "Cancel"));
    }

    private void adminDashboard() {
        subState = "welcome";
        centerTitle.setText(i18n.get("admin.title", "Administrator Panel"));
        listModel.setColumnIdentifiers(new String[]{""});
        listModel.setRowCount(0);
        listModel.addRow(new Object[]{i18n.get("label.selectTransaction", "Please select a function.")});
        listModel.addRow(new Object[]{i18n.get("label.useButtons", "Use the buttons on the sides.")});
        centerText.setText("");
        rightBtns[2].setVisible(false);
        rightBtns[3].setVisible(false);
    }

    private void adminRateSelect() {
        subState = "rate_select";
        centerTitle.setText(i18n.get("admin.exchangeRates", "Exchange Rates"));
        listModel.setColumnIdentifiers(new String[]{
                i18n.get("table.currencyPair", "Pair"),
                i18n.get("table.rate", "Rate")});
        listModel.setRowCount(0);
        for (Map.Entry<String, Double> e : ExchangeRateManager.getInstance().getConfiguredRates().entrySet())
            listModel.addRow(new Object[]{e.getKey(), String.format("%.4f", e.getValue())});
        if (listTable.getRowCount() > 0) listTable.setRowSelectionInterval(0, 0);
        centerText.setText(i18n.get("label.useArrows", "Select rate, then OK"));

        setupArrowButtons();
    }

    private void adminRateInput(String pair) {
        subState = "rate_input";
        selectedPair = pair;
        String[] parts = pair.split(":");
        centerTitle.setText(i18n.get("label.newRate", "New rate") + ": " + pair);
        listModel.setColumnIdentifiers(new String[]{"", ""});
        listModel.setRowCount(0);
        listModel.addRow(new Object[]{"Pair", pair});
        listModel.addRow(new Object[]{"Current",
                String.format("%.4f", ExchangeRateManager.getInstance().getRate(parts[0], parts[1]))});
        centerText.setText(i18n.get("label.enterRate", "Enter new rate: ") + "0");
        
        buf.setLength(0); buf.append("0"); dotEntered = false;
        rightBtns[2].setVisible(false); rightBtns[3].setVisible(false);
    }

    private void adminUnlock() {
        subState = "unlock";
        centerTitle.setText(i18n.get("button.unlock", "Unlock User"));
        listModel.setColumnIdentifiers(new String[]{
                i18n.get("table.cardNumber", "Card #"),
                i18n.get("table.userName", "Name"),
                i18n.get("table.status", "Status")});
        listModel.setRowCount(0);
        lockedClients = new ArrayList<>();
        for (User u : router.getAuthController().getUserDatabase().values()) {
            if (u instanceof BankClient) {
                listModel.addRow(new Object[]{u.getCardNumber(), u.getUserName(),
                        u.isLocked() ? i18n.get("label.locked", "LOCKED")
                                : i18n.get("label.active", "Active")});
                lockedClients.add(u);
            }
        }
        centerText.setText(i18n.get("label.selectUserPrompt", "Select a user, then press OK"));
        if (listTable.getRowCount() > 0) listTable.setRowSelectionInterval(0, 0);
        setupArrowButtons();
    }

    private void setupArrowButtons() {
        rightBtns[2].setVisible(true); rightBtns[3].setVisible(true);
        rightBtns[2].setText(h("button.up", "\u25B2"));
        rightBtns[3].setText(h("button.down", "\u25BC"));
        clearListeners(rightBtns[2]);
        rightBtns[2].addActionListener(e -> {
            int r = listTable.getSelectedRow();
            if (r > 0) listTable.setRowSelectionInterval(r - 1, r - 1);
        });
        clearListeners(rightBtns[3]);
        rightBtns[3].addActionListener(e -> {
            int r = listTable.getSelectedRow();
            if (r < listTable.getRowCount() - 1) listTable.setRowSelectionInterval(r + 1, r + 1);
        });
    }

    @Override
    public void onNumber(int n) {
        if (subState.equals("rate_input")) {
            if (dotEntered && buf.indexOf(".") >= 0 && buf.length() - buf.indexOf(".") > 4) return;
            if (buf.toString().equals("0") && n != 0) { buf.setLength(0); buf.append(n); }
            else if (!buf.toString().equals("0")) buf.append(n);
            centerText.setText(i18n.get("label.enterRate", "Enter new rate: ") + buf.toString());
        }
    }

    @Override
    public void onDot() {
        if (subState.equals("rate_input")) {
            if (!dotEntered) { dotEntered = true; buf.append("."); }
            centerText.setText(i18n.get("label.enterRate", "Enter new rate: ") + buf.toString());
        }
    }

    @Override
    public void onClear() {
        if (subState.equals("rate_input")) {
            buf.setLength(0); buf.append("0"); dotEntered = false;
            centerText.setText(i18n.get("label.enterRate", "Enter new rate: ") + "0");
        }
    }

    @Override
    public void onDelete() {
        if (subState.equals("rate_input")) {
            if (buf.length() > 0) { 
                if (buf.charAt(buf.length()-1)=='.') dotEntered=false;
                buf.setLength(buf.length()-1); 
                if (buf.length()==0) buf.append("0"); 
            }
            centerText.setText(i18n.get("label.enterRate", "Enter new rate: ") + buf.toString());
        }
    }

    @Override
    public void onOk() {
        if (subState.equals("rate_select")) {
            int r = listTable.getSelectedRow();
            if (r >= 0) {
                String pair = (String) listModel.getValueAt(r, 0);
                adminRateInput(pair);
            }
        } else if (subState.equals("rate_input")) {
            try {
                double r = Double.parseDouble(buf.toString());
                if (r <= 0) { 
                    centerText.setText("<html><font color=red>" + i18n.get("error.positiveRate", "Must be > 0") + "</font></html>"); 
                    return; 
                }
                String[] parts = selectedPair.split(":");
                ExchangeRateManager.getInstance().setRate(parts[0], parts[1], r);
                centerText.setText("<html><font color=green>"
                        + i18n.get("info.rateUpdated", "Updated") + ": "
                        + selectedPair + " = " + r + "</font></html>");
                Timer t = new Timer(1500, ev -> adminRateSelect()); t.setRepeats(false); t.start();
            } catch (NumberFormatException ex) {
                centerText.setText("<html><font color=red>" + i18n.get("error.invalidRate", "Invalid") + "</font></html>");
            }
        } else if (subState.equals("unlock")) {
            int r = listTable.getSelectedRow();
            if (r < 0) { centerText.setText(i18n.get("error.selectUser", "Select a user")); return; }
            User u = lockedClients.get(r);
            if (u.isLocked()) { 
                u.unlock(); 
                router.getAuthController().persistUserState(u);
                centerText.setText("<html><font color=green>" + i18n.get("info.userUnlocked", "Unlocked") + ": " + u.getUserName() + "</font></html>"); 
            } else {
                centerText.setText(i18n.get("info.userNotLocked", "Already active"));
            }
            Timer t = new Timer(1500, ev -> adminUnlock()); t.setRepeats(false); t.start();
        }
    }

    @Override
    public void onCancel() {
        if (subState.equals("rate_input")) {
            adminRateSelect();
        } else if (subState.equals("rate_select") || subState.equals("unlock")) {
            adminDashboard();
        }
    }

    @Override
    public void onLanguageChanged(String language) {
        if (isVisible()) {
            if (rightBtns[4].isVisible()) {
                rightBtns[4].setText(language.toUpperCase());
            }
            numpadPanel.setOkText(i18n.get("button.ok", "OK"));
            numpadPanel.setCancelText(i18n.get("button.cancel", "Cancel"));
            
            leftBtns[0].setText(h("button.updateRate", "Rate"));
            leftBtns[1].setText(h("button.unlock", "Unlock"));
            rightBtns[1].setText(h("button.logout", "Logout"));
        }
    }
}
