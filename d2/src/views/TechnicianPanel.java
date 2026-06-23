import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TechnicianPanel extends BaseViewPanel implements NumpadListener {
    private DefaultTableModel listModel;
    private JTable listTable;

    public TechnicianPanel(Router router) {
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
        resetSideColors(new Color(0, 80, 60), new Color(0, 120, 80));
        leftPanel.setVisible(true); rightPanel.setVisible(true);

        for (int i = 0; i < BTN_COUNT; i++) {
            leftBtns[i].setVisible(false); rightBtns[i].setVisible(false);
        }
        
        leftBtns[0].setVisible(true);
        leftBtns[0].setText(h("button.refill", "Refill"));
        rightBtns[1].setVisible(true);
        rightBtns[1].setText(h("button.logout", "Logout"));
        rightBtns[4].setVisible(true);
        rightBtns[4].setText(i18n.getCurrentLanguage().toUpperCase());

        clearListeners(leftBtns[0]);
        leftBtns[0].addActionListener(e -> {
            CashBox.getInstance().refill();
            refreshTech();
            centerText.setText("<html><font color=green>"
                    + i18n.get("info.cashRefilled", "Cash refilled") + "</font></html>");
        });
        clearListeners(rightBtns[1]);
        rightBtns[1].addActionListener(e -> {
            router.getAuthController().logout();
            router.navigateTo("login");
        });
        clearListeners(rightBtns[4]);
        rightBtns[4].addActionListener(e -> {
            router.showLanguageSelector("tech", null, null, null, false, false);
        });

        refreshTech();
        
        numpadPanel.setOkText(i18n.get("button.ok", "OK"));
        numpadPanel.setCancelText(i18n.get("button.cancel", "Cancel"));
    }

    private void refreshTech() {
        centerTitle.setText(i18n.get("tech.title", "Technician Panel"));
        CashBox cb = CashBox.getInstance();
        listModel.setColumnIdentifiers(new String[]{"", ""});
        listModel.setRowCount(0);
        listModel.addRow(new Object[]{i18n.get("tech.cashLevel", "Cash"),
                String.format("%.2f / %.2f", cb.getCurrentCash(), cb.getMaxCapacity())});
        listModel.addRow(new Object[]{i18n.get("tech.maxCapacity", "Max"),
                String.format("%.2f", cb.getMaxCapacity())});
        listModel.addRow(new Object[]{i18n.get("tech.lowThreshold", "Low"),
                String.format("%.2f", cb.getLowThreshold())});
        String s = cb.isEmpty() ? i18n.get("status.empty", "EMPTY")
                : cb.isLow() ? i18n.get("status.low", "LOW")
                : i18n.get("status.normal", "Normal");
        listModel.addRow(new Object[]{i18n.get("table.status", "Status"), s});
        centerText.setText(i18n.get("label.pressRefill", "Press Refill to restore to max"));
    }

    @Override
    public void onNumber(int n) {}
    @Override
    public void onDot() {}
    @Override
    public void onClear() {}
    @Override
    public void onDelete() {}
    @Override
    public void onOk() {}
    @Override
    public void onCancel() {}

    @Override
    public void onLanguageChanged(String language) {
        if (isVisible()) {
            if (rightBtns[4].isVisible()) {
                rightBtns[4].setText(language.toUpperCase());
            }
            numpadPanel.setOkText(i18n.get("button.ok", "OK"));
            numpadPanel.setCancelText(i18n.get("button.cancel", "Cancel"));
            
            leftBtns[0].setText(h("button.refill", "Refill"));
            rightBtns[1].setText(h("button.logout", "Logout"));
        }
    }
}
