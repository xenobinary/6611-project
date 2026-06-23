import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ClientDashboardPanel extends BaseViewPanel implements NumpadListener {
    private DefaultTableModel listModel;
    private JTable listTable;
    
    private String action;
    private Account selectedAcc, destAcc;
    private List<Account> accList;
    private int accIdx;
    
    private String subState = "welcome"; // welcome, balances, acc_select, dest_select, amount, confirm, history
    private StringBuilder amountBuf = new StringBuilder("0");
    private boolean dotEntered = false;

    public ClientDashboardPanel(Router router) {
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
        
        centerTitle.setVisible(true);
        centerText.setVisible(true);
    }

    @Override
    public void onShow() {
        resetSideColors(new Color(0, 51, 102), new Color(0, 80, 140));
        leftPanel.setVisible(true); 
        rightPanel.setVisible(true);

        for (int i = 0; i < BTN_COUNT; i++) { 
            leftBtns[i].setVisible(true); 
            rightBtns[i].setVisible(true); 
        }

        leftBtns[0].setText(h("menu.withdraw", "Withdraw"));
        leftBtns[1].setText(h("menu.deposit", "Deposit"));
        leftBtns[2].setText(h("menu.transfer", "Transfer"));
        leftBtns[3].setText(h("menu.balance", "Balance"));
        rightBtns[0].setText(h("button.history", "History"));
        rightBtns[1].setText(h("button.logout", "Logout"));
        rightBtns[2].setVisible(false);
        rightBtns[3].setVisible(false);
        rightBtns[4].setText(i18n.getCurrentLanguage().toUpperCase());
        leftBtns[4].setVisible(false);

        clearListeners(leftBtns[0]);
        leftBtns[0].addActionListener(e -> { action = "withdraw"; highlightSideBtn("withdraw"); toAccountSelect(); });
        clearListeners(leftBtns[1]);
        leftBtns[1].addActionListener(e -> { action = "deposit"; highlightSideBtn("deposit"); toAccountSelect(); });
        clearListeners(leftBtns[2]);
        leftBtns[2].addActionListener(e -> { action = "transfer"; highlightSideBtn("transfer"); toAccountSelect(); });
        clearListeners(leftBtns[3]);
        leftBtns[3].addActionListener(e -> { highlightSideBtn("balance"); showBalances(); });
        clearListeners(rightBtns[0]);
        rightBtns[0].addActionListener(e -> { highlightSideBtn("history"); showHistory(); });
        clearListeners(rightBtns[1]);
        rightBtns[1].addActionListener(e -> { 
            router.getTransactionController().setCurrentUser("");
            router.getAuthController().logout(); 
            router.navigateTo("login");
        });
        clearListeners(rightBtns[4]);
        rightBtns[4].addActionListener(e -> {
            boolean isHist = subState.equals("history");
            boolean isBal = subState.equals("balances");
            router.showLanguageSelector("client", action, selectedAcc, destAcc, isHist, isBal);
        });

        showWelcome();
        numpadPanel.setOkText(i18n.get("button.ok", "OK"));
        numpadPanel.setCancelText(i18n.get("button.cancel", "Cancel"));
    }

    public void restoreState(String act, Account acc, Account dest, boolean isHistory, boolean isBalance) {
        onShow();
        this.action = act;
        this.selectedAcc = acc;
        this.destAcc = dest;
        if (isHistory) {
            showHistory();
            highlightSideBtn("history");
        } else if (isBalance) {
            showBalances();
            highlightSideBtn("balance");
        } else if (act != null && acc != null) {
            toAmountEntry();
            highlightSideBtn(act);
        } else if (act != null) {
            toAccountSelect();
            highlightSideBtn(act);
        }
    }

    private void highlightSideBtn(String which) {
        Color base = new Color(0, 80, 140);
        Color hl = new Color(255, 180, 40);
        for (int i = 0; i < BTN_COUNT; i++) {
            leftBtns[i].setBackground(base);
            rightBtns[i].setBackground(base);
        }
        if (which == null) return;
        switch (which) {
            case "withdraw": leftBtns[0].setBackground(hl); break;
            case "deposit":  leftBtns[1].setBackground(hl); break;
            case "transfer": leftBtns[2].setBackground(hl); break;
            case "balance":  leftBtns[3].setBackground(hl); break;
            case "history":  rightBtns[0].setBackground(hl); break;
        }
    }

    private void showWelcome() {
        subState = "welcome";
        highlightSideBtn(null);
        User u = router.getAuthController().getCurrentUser();
        centerTitle.setText(i18n.get("label.welcome", "Welcome") + ", " + (u != null ? u.getUserName() : ""));
        listModel.setColumnIdentifiers(new String[]{""});
        listModel.setRowCount(0);
        listModel.addRow(new Object[]{i18n.get("label.selectTransaction", "Please select a transaction.")});
        listModel.addRow(new Object[]{i18n.get("label.useButtons", "Use the buttons on the sides.")});
        centerText.setText("");
        rightBtns[2].setVisible(false);
        rightBtns[3].setVisible(false);
    }

    private void showBalances() {
        subState = "balances";
        rightBtns[2].setVisible(false);
        rightBtns[3].setVisible(false);
        User u = router.getAuthController().getCurrentUser();
        centerTitle.setText(i18n.get("label.accounts", "Account Balances"));
        listModel.setColumnIdentifiers(new String[]{
                i18n.get("table.account", "Account"),
                i18n.get("table.accountType", "Type"),
                i18n.get("table.balance", "Balance")});
        listModel.setRowCount(0);
        BankClient c = (BankClient) u;
        for (Account a : c.getAccounts())
            listModel.addRow(new Object[]{a.getAccountNumber(), t(a.getAccountType()),
                    String.format("%.2f %s", a.getBalance(), a.getCurrency())});
        centerText.setText("");
    }

    private void showHistory() {
        subState = "history";
        rightBtns[2].setVisible(false);
        rightBtns[3].setVisible(false);
        centerTitle.setText(i18n.get("menu.history", "Transaction History"));
        listModel.setColumnIdentifiers(new String[]{""});
        listModel.setRowCount(0);
        List<String> h = router.getTransactionController().getFormattedHistory(
                i18n.get("history.withdraw", "Withdrawal"),
                i18n.get("history.deposit", "Deposit"),
                i18n.get("history.transfer", "Transfer"),
                i18n.get("word.from", "from"),
                i18n.get("word.to", "to"));
        if (h.isEmpty()) listModel.addRow(new Object[]{i18n.get("info.noHistory", "No transactions yet.")});
        else for (String r : h) listModel.addRow(new Object[]{r});
        centerText.setText("");
    }

    private void toAccountSelect() {
        subState = "acc_select";
        BankClient c = (BankClient) router.getAuthController().getCurrentUser();
        if ("withdraw".equals(action) || "deposit".equals(action)) {
            accList = new java.util.ArrayList<>();
            for (Account a : c.getAccounts())
                if ("CAD".equals(a.getCurrency())) accList.add(a);
        } else {
            accList = c.getAccounts();
        }
        if (accList.isEmpty()) return;
        accIdx = 0; selectedAcc = null; destAcc = null;

        centerTitle.setText(action.equals("transfer")
                ? i18n.get("label.fromAccount", "From Account")
                : i18n.get("label.selectAccount", "Select Account"));
        centerText.setText(i18n.get("label.useArrows", "Use arrows or number keys, then OK"));
        refreshAccList();

        setupArrowButtons(() -> {
            if (accIdx > 0) { accIdx--; refreshAccList(); }
        }, () -> {
            if (accIdx < accList.size() - 1) { accIdx++; refreshAccList(); }
        });
        
        numpadPanel.setOkText(i18n.get("button.ok", "OK")); 
        numpadPanel.setCancelText(i18n.get("button.cancel", "Cancel"));
    }

    private void refreshAccList() {
        listModel.setColumnIdentifiers(new String[]{
                i18n.get("table.accountNumber", "Account #"),
                i18n.get("table.accountType", "Type"),
                i18n.get("table.balance", "Balance")});
        listModel.setRowCount(0);
        for (int i = 0; i < accList.size(); i++) {
            Account a = accList.get(i);
            String prefix = (i == accIdx) ? "> " : "  ";
            listModel.addRow(new Object[]{a.getAccountNumber(), t(a.getAccountType()),
                    prefix + String.format("%.2f %s", a.getBalance(), a.getCurrency())});
        }
        listTable.setRowSelectionInterval(accIdx, accIdx);
        if (accList.size() > 1) {
            rightBtns[2].setVisible(true);
            rightBtns[3].setVisible(true);
        } else {
            rightBtns[2].setVisible(false);
            rightBtns[3].setVisible(false);
        }
    }

    private void toDestSelect() {
        subState = "dest_select";
        BankClient c = (BankClient) router.getAuthController().getCurrentUser();
        accList = c.getAccounts();
        accIdx = 0; destAcc = null;
        centerTitle.setText(i18n.get("label.toAccount", "To Account"));
        centerText.setText(i18n.get("label.useArrows", "Use arrows or number keys, then OK"));
        refreshDestList();

        setupArrowButtons(() -> {
            if (accIdx > 0) { accIdx--; refreshDestList(); }
        }, () -> {
            if (accIdx < accList.size() - 1) { accIdx++; refreshDestList(); }
        });
    }

    private void refreshDestList() {
        listModel.setColumnIdentifiers(new String[]{
                i18n.get("table.accountNumber", "Account #"),
                i18n.get("table.accountType", "Type"),
                i18n.get("table.balance", "Balance")});
        listModel.setRowCount(0);
        for (int i = 0; i < accList.size(); i++) {
            Account a = accList.get(i);
            if (a == selectedAcc) continue;
            String prefix = (a == accList.get(accIdx)) ? "> " : "  ";
            listModel.addRow(new Object[]{a.getAccountNumber(), t(a.getAccountType()),
                    prefix + String.format("%.2f %s", a.getBalance(), a.getCurrency())});
        }
        if (accList.size() > 2) {
            rightBtns[2].setVisible(true); 
            rightBtns[3].setVisible(true);
        } else {
            rightBtns[2].setVisible(false); 
            rightBtns[3].setVisible(false);
        }
    }

    private void toAmountEntry() {
        subState = "amount";
        centerTitle.setText(action.equals("transfer")
                ? i18n.get("menu.transfer", "Transfer") + ": " + selectedAcc.getAccountNumber()
                + " \u2192 " + destAcc.getAccountNumber()
                : i18n.get("menu." + action, action) + ": " + selectedAcc.getAccountNumber());

        listModel.setColumnIdentifiers(new String[]{
                i18n.get("table.accountNumber", "Account #"),
                i18n.get("table.accountType", "Type"),
                i18n.get("table.balance", "Balance")});
        listModel.setRowCount(0);
        listModel.addRow(new Object[]{selectedAcc.getAccountNumber(), t(selectedAcc.getAccountType()),
                String.format("%.2f %s", selectedAcc.getBalance(), selectedAcc.getCurrency())});
        centerText.setText(i18n.get("label.enterAmount", "Enter amount: ") + "0 " + selectedAcc.getCurrency());

        rightBtns[2].setVisible(false); 
        rightBtns[3].setVisible(false);

        amountBuf.setLength(0);
        amountBuf.append("0");
        dotEntered = false;
        numpadPanel.setOkText(i18n.get("button.ok", "OK"));
    }

    private void toConfirm(double amount) {
        subState = "confirm";
        centerTitle.setText(i18n.get("label.confirm", "Confirm") + " " + i18n.get("menu."+action, action) + "?");
        listModel.setRowCount(0);
        listModel.addRow(new Object[]{i18n.get("label.account", "Account") + ":",
                selectedAcc.getAccountNumber()});
        listModel.addRow(new Object[]{i18n.get("label.amount", "Amount") + ":",
                String.format("%.2f %s", amount, selectedAcc.getCurrency())});
        if (destAcc != null)
            listModel.addRow(new Object[]{i18n.get("label.to", "To") + ":",
                    destAcc.getAccountNumber() + " " + destAcc.getCurrency()});
        centerText.setText(i18n.get("label.pressConfirm", "Press OK to confirm, Cancel to abort"));
    }

    private void showResult(String result) {
        subState = "result";
        centerTitle.setText(i18n.get("label.transactionComplete", "Transaction Complete"));
        listModel.setRowCount(0);
        listModel.addRow(new Object[]{result});
        centerText.setText("<html><font color=green>" + i18n.get("label.success", "Success") + "</font></html>");
        Timer t = new Timer(2500, e -> {
            selectedAcc = null; destAcc = null; 
            onShow();
        });
        t.setRepeats(false); t.start();
    }

    private void setupArrowButtons(Runnable upAction, Runnable downAction) {
        rightBtns[2].setVisible(true); rightBtns[3].setVisible(true);
        rightBtns[2].setText(h("button.up", "\u25B2"));
        rightBtns[3].setText(h("button.down", "\u25BC"));
        clearListeners(rightBtns[2]);
        rightBtns[2].addActionListener(e -> upAction.run());
        clearListeners(rightBtns[3]);
        rightBtns[3].addActionListener(e -> downAction.run());
    }

    private String t(String key) { return i18n.get("account." + key, key); }

    // --- Numpad Listener ---

    @Override
    public void onNumber(int n) {
        if (subState.equals("acc_select")) {
            if (n >= 1 && n <= accList.size()) {
                accIdx = n - 1; refreshAccList();
            }
        } else if (subState.equals("dest_select")) {
            if (n >= 1 && n <= accList.size()) {
                Account a = accList.get(n - 1);
                if (a != selectedAcc) { accIdx = n - 1; refreshDestList(); }
            }
        } else if (subState.equals("amount")) {
            if (dotEntered && amountBuf.indexOf(".") >= 0 && amountBuf.length() - amountBuf.indexOf(".") > 2) return;
            if (amountBuf.toString().equals("0") && n != 0) { amountBuf.setLength(0); amountBuf.append(n); }
            else if (!amountBuf.toString().equals("0")) amountBuf.append(n);
            centerText.setText(i18n.get("label.enterAmount", "Enter amount: ") + amountBuf.toString() + " " + selectedAcc.getCurrency());
        }
    }

    @Override
    public void onDot() {
        if (subState.equals("amount")) {
            if (!dotEntered) { dotEntered = true; amountBuf.append("."); }
            centerText.setText(i18n.get("label.enterAmount", "Enter amount: ") + amountBuf.toString() + " " + selectedAcc.getCurrency());
        }
    }

    @Override
    public void onClear() {
        if (subState.equals("amount")) {
            amountBuf.setLength(0); amountBuf.append("0"); dotEntered = false;
            centerText.setText(i18n.get("label.enterAmount", "Enter amount: ") + "0 " + selectedAcc.getCurrency());
        } else if (subState.equals("acc_select") || subState.equals("dest_select")) {
            onShow();
        }
    }

    @Override
    public void onDelete() {
        if (subState.equals("amount")) {
            if (amountBuf.length() > 0) { 
                if (amountBuf.charAt(amountBuf.length()-1)=='.') dotEntered=false;
                amountBuf.setLength(amountBuf.length()-1); 
                if (amountBuf.length()==0) amountBuf.append("0"); 
            }
            centerText.setText(i18n.get("label.enterAmount", "Enter amount: ") + amountBuf.toString() + " " + selectedAcc.getCurrency());
        } else if (subState.equals("acc_select") || subState.equals("dest_select")) {
            onShow();
        }
    }

    @Override
    public void onOk() {
        if (subState.equals("acc_select")) {
            selectedAcc = accList.get(accIdx);
            if (action.equals("transfer")) toDestSelect();
            else toAmountEntry();
        } else if (subState.equals("dest_select")) {
            destAcc = accList.get(accIdx);
            if (destAcc == selectedAcc) return;
            toAmountEntry();
        } else if (subState.equals("amount")) {
            try {
                double amt = Double.parseDouble(amountBuf.toString());
                toConfirm(amt);
            } catch (NumberFormatException ex) {
                centerText.setText("<html><font color=red>" + i18n.get("error.invalidAmount", "Invalid amount") + "</font></html>");
            }
        } else if (subState.equals("confirm")) {
            double amount = Double.parseDouble(amountBuf.toString());
            TransactionController txn = router.getTransactionController();
            try {
                String result;
                String fw = i18n.get("word.from", "from");
                String tw = i18n.get("word.to", "to");
                if ("withdraw".equals(action)) {
                    result = txn.withdraw(selectedAcc, amount,
                            i18n.get("history.withdraw", "Withdrawal"),
                            i18n.get("history.cashLow", "[WARNING: Cash Low]"),
                            fw, tw);
                } else if ("deposit".equals(action)) {
                    result = txn.deposit(selectedAcc, amount,
                            i18n.get("history.deposit", "Deposit"),
                            fw, tw);
                } else {
                    result = txn.transfer(selectedAcc, destAcc, amount,
                            i18n.get("history.transfer", "Transfer"),
                            fw, tw);
                }
                showResult(result);
            } catch (InsufficientFundException ex) {
                centerText.setText("<html><font color=red>" + i18n.get("error.insufficientFunds", "Insufficient funds") + "</font></html>");
                Timer t = new Timer(1500, ev -> toAmountEntry()); t.setRepeats(false); t.start();
            } catch (InvalidAmountException ex) {
                centerText.setText("<html><font color=red>" + i18n.get(ex.getErrorKey(), ex.getMessage()) + "</font></html>");
                Timer t = new Timer(1500, ev -> toAmountEntry()); t.setRepeats(false); t.start();
            }
        }
    }

    @Override
    public void onCancel() {
        if (!subState.equals("welcome") && !subState.equals("balances") && !subState.equals("history") && !subState.equals("result")) {
            selectedAcc = null; destAcc = null;
            onShow();
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
            
            leftBtns[0].setText(h("menu.withdraw", "Withdraw"));
            leftBtns[1].setText(h("menu.deposit", "Deposit"));
            leftBtns[2].setText(h("menu.transfer", "Transfer"));
            leftBtns[3].setText(h("menu.balance", "Balance"));
            rightBtns[0].setText(h("button.history", "History"));
            rightBtns[1].setText(h("button.logout", "Logout"));
        }
    }
}
