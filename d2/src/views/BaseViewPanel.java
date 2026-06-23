import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public abstract class BaseViewPanel extends JPanel implements I18nController.I18nListener {
    protected Router router;
    protected I18nController i18n;
    
    protected JPanel leftPanel, rightPanel, centerPanel;
    protected JLabel centerTitle, centerText;
    protected JButton[] leftBtns, rightBtns;
    protected static final int BTN_COUNT = 5;
    protected NumpadPanel numpadPanel;
    
    public BaseViewPanel(Router router) {
        this.router = router;
        this.i18n = I18nController.getInstance();
        this.i18n.addListener(this);
        
        setLayout(new BorderLayout());
        
        add(leftSide(), BorderLayout.WEST);
        add(centerArea(), BorderLayout.CENTER);
        add(rightSide(), BorderLayout.EAST);
        
        numpadPanel = new NumpadPanel();
        add(numpadPanel, BorderLayout.SOUTH);
    }
    
    private JPanel leftSide() {
        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(0, 51, 102));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        leftBtns = new JButton[BTN_COUNT];
        for (int i = 0; i < BTN_COUNT; i++) {
            leftBtns[i] = sideBtn();
            leftPanel.add(leftBtns[i]);
            if (i < BTN_COUNT - 1) leftPanel.add(Box.createVerticalStrut(8));
        }
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.setVisible(false);
        return leftPanel;
    }
    
    private JPanel rightSide() {
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(0, 51, 102));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        rightBtns = new JButton[BTN_COUNT];
        for (int i = 0; i < BTN_COUNT; i++) {
            rightBtns[i] = sideBtn();
            rightPanel.add(rightBtns[i]);
            if (i < BTN_COUNT - 1) rightPanel.add(Box.createVerticalStrut(8));
        }
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.setVisible(false);
        return rightPanel;
    }
    
    protected JButton sideBtn() {
        JButton b = new JButton();
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(0, 80, 140));
        b.setFocusPainted(false);
        b.setMaximumSize(new Dimension(115, 50));
        b.setPreferredSize(new Dimension(115, 50));
        b.setMinimumSize(new Dimension(115, 50));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setVisible(false);
        return b;
    }
    
    private JPanel centerArea() {
        centerPanel = new JPanel(new BorderLayout(5, 0));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 190), 1));
        
        centerTitle = new JLabel("", SwingConstants.CENTER);
        centerTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        centerTitle.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        centerPanel.add(centerTitle, BorderLayout.NORTH);
        
        centerText = new JLabel("", SwingConstants.CENTER);
        centerText.setFont(new Font("SansSerif", Font.PLAIN, 16));
        centerText.setBorder(BorderFactory.createEmptyBorder(12, 15, 15, 15));
        centerPanel.add(centerText, BorderLayout.SOUTH);
        
        buildCenter(centerPanel);
        return centerPanel;
    }
    
    protected abstract void buildCenter(JPanel centerPanel);
    
    protected String h(String key, String def) {
        return "<html><center>" + i18n.get(key, def) + "</center></html>";
    }
    
    protected void clearListeners(JButton b) {
        for (ActionListener al : b.getActionListeners()) b.removeActionListener(al);
    }
    
    protected void resetSideColors(Color bg, Color btnBg) {
        leftPanel.setBackground(bg);
        rightPanel.setBackground(bg);
        for (int i = 0; i < BTN_COUNT; i++) {
            leftBtns[i].setBackground(btnBg);
            rightBtns[i].setBackground(btnBg);
        }
    }
    
    public abstract void onShow();
}
