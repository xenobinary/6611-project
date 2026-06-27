import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel for selecting the display language (English, French, Chinese).
 * Preserves the pre-selection view state so the user returns to their
 * previous screen after changing the language.
 */
public class LanguageSelectorPanel extends BaseViewPanel implements NumpadListener {
    /** Table model for the language options. */
    private DefaultTableModel listModel;
    /** Table displaying the language choices. */
    private JTable listTable;

    /** The view state to return to after language selection. */
    private String preLangState;
    /** The action the user was performing (null if none). */
    private String preLangAction;
    /** The selected account before opening the language selector. */
    private Account preLangAcc;
    /** The destination account before opening the language selector. */
    private Account preLangDest;
    /** Whether the user was viewing history. */
    private boolean preLangHistory;
    /** Whether the user was viewing balances. */
    private boolean preLangBalance;

    /**
     * Constructs a LanguageSelectorPanel.
     *
     * @param router the router for navigation and controller access
     */
    public LanguageSelectorPanel(Router router) {
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

    /**
     * Configures the pre-language-selection state so the user can return
     * to the correct view after choosing a language.
     *
     * @param preLangState   the view state to return to
     * @param preLangAction  the action in progress
     * @param preLangAcc     the selected account (null if none)
     * @param preLangDest    the destination account (null if none)
     * @param preLangHistory whether the user was viewing history
     * @param preLangBalance whether the user was viewing balances
     */
    public void configure(String preLangState, String preLangAction, Account preLangAcc, Account preLangDest, boolean preLangHistory, boolean preLangBalance) {
        this.preLangState = preLangState;
        this.preLangAction = preLangAction;
        this.preLangAcc = preLangAcc;
        this.preLangDest = preLangDest;
        this.preLangHistory = preLangHistory;
        this.preLangBalance = preLangBalance;
    }

    @Override
    public void onShow() {
        Color bg = new Color(0, 51, 102);
        Color btnBg = new Color(0, 80, 140);
        if ("admin".equals(preLangState)) {
            bg = new Color(102, 0, 0); btnBg = new Color(150, 30, 30);
        } else if ("tech".equals(preLangState)) {
            bg = new Color(0, 80, 60); btnBg = new Color(0, 120, 80);
        }
        resetSideColors(bg, btnBg);
        
        leftPanel.setVisible(true); rightPanel.setVisible(true);
        for (int i = 0; i < BTN_COUNT; i++) {
            leftBtns[i].setVisible(false);
            rightBtns[i].setVisible(false);
        }

        centerTitle.setText(i18n.get("label.language", "Language"));
        listModel.setColumnIdentifiers(new String[]{"#", ""});
        listModel.setRowCount(0);
        listModel.addRow(new Object[]{"1", "English"});
        listModel.addRow(new Object[]{"2", "Fran\u00E7ais"});
        listModel.addRow(new Object[]{"3", "\u4E2D\u6587"});
        centerText.setText(i18n.get("label.useArrows", "Press 1, 2, or 3 on keypad then OK"));
        listTable.setRowSelectionInterval(0, 0);

        rightBtns[2].setVisible(true);
        rightBtns[3].setVisible(true);
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

        numpadPanel.setOkText(i18n.get("button.ok", "OK"));
        numpadPanel.setCancelText(i18n.get("button.cancel", "Cancel"));
    }

    /** Navigates back to the view the user was on before opening the language selector. */
    private void restorePreLangState() {
        if ("client".equals(preLangState)) {
            router.navigateTo("client_restore");
        } else if ("admin".equals(preLangState)) {
            router.navigateTo("admin");
        } else if ("tech".equals(preLangState)) {
            router.navigateTo("tech");
        } else {
            router.navigateTo("login");
        }
    }

    @Override
    public void onNumber(int n) {
        if (n >= 1 && n <= 3) listTable.setRowSelectionInterval(n - 1, n - 1);
    }

    @Override
    public void onDot() {}

    @Override
    public void onClear() { restorePreLangState(); }

    @Override
    public void onDelete() { restorePreLangState(); }

    @Override
    public void onOk() {
        int r = listTable.getSelectedRow();
        String[] langs = {"en", "fr", "zh"};
        if (r >= 0 && r < langs.length) {
            i18n.setLanguage(langs[r]);
            restorePreLangState();
        }
    }

    @Override
    public void onCancel() { restorePreLangState(); }

    @Override
    public void onLanguageChanged(String language) {
        if (isVisible()) {
            numpadPanel.setOkText(i18n.get("button.ok", "OK"));
            numpadPanel.setCancelText(i18n.get("button.cancel", "Cancel"));
        }
    }

    /** @return the action the user was performing before opening the language selector */
    public String getPreLangAction() { return preLangAction; }
    /** @return the selected account before opening the language selector */
    public Account getPreLangAcc() { return preLangAcc; }
    /** @return the destination account before opening the language selector */
    public Account getPreLangDest() { return preLangDest; }
    /** @return whether the user was viewing history */
    public boolean isPreLangHistory() { return preLangHistory; }
    /** @return whether the user was viewing balances */
    public boolean isPreLangBalance() { return preLangBalance; }
}
