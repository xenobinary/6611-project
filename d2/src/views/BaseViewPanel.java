import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Abstract base class for all view panels in the ABM system.
 * Provides a common layout with left/right side button panels,
 * a central content area, and a numpad at the bottom.
 * Also handles i18n listener registration and utility methods.
 */
public abstract class BaseViewPanel extends JPanel implements I18nController.I18nListener {
    /** The router for navigation between views. */
    protected Router router;
    /** The i18n controller for localized text. */
    protected I18nController i18n;
    
    /** Left-side vertical button panel. */
    protected JPanel leftPanel;
    /** Right-side vertical button panel. */
    protected JPanel rightPanel;
    /** Center content panel. */
    protected JPanel centerPanel;
    /** Title label in the center area. */
    protected JLabel centerTitle;
    /** Text/message label in the center area. */
    protected JLabel centerText;
    /** Left-side action buttons. */
    protected JButton[] leftBtns;
    /** Right-side action buttons. */
    protected JButton[] rightBtns;
    /** Number of side buttons. */
    protected static final int BTN_COUNT = 5;
    /** The shared numpad panel. */
    protected NumpadPanel numpadPanel;
    
    /**
     * Constructs a BaseViewPanel with the given router.
     * Builds the standard layout: left sidebar, center area, right sidebar, numpad.
     *
     * @param router the router for navigation and controller access
     */
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
    
    /** Builds the left sidebar with vertical button slots. */
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
    
    /** Builds the right sidebar with vertical button slots. */
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
    
    /**
     * Creates a standardized sidebar button.
     *
     * @return a pre-styled JButton
     */
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
    
    /** Builds the center area with a title, content (subclass-defined), and a text footer. */
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
    
    /**
     * Called during construction to let subclasses add their content to the center area.
     *
     * @param centerPanel the center panel to add content to
     */
    protected abstract void buildCenter(JPanel centerPanel);
    
    /**
     * Wraps a localized string in HTML for use in button text.
     *
     * @param key the i18n key
     * @param def the default fallback value
     * @return an HTML-wrapped string
     */
    protected String h(String key, String def) {
        return "<html><center>" + i18n.get(key, def) + "</center></html>";
    }
    
    /**
     * Removes all action listeners from a button (to prevent duplicate handlers).
     *
     * @param b the button to clear
     */
    protected void clearListeners(JButton b) {
        for (ActionListener al : b.getActionListeners()) b.removeActionListener(al);
    }
    
    /**
     * Resets the background colors of the left/right side panels and their buttons.
     *
     * @param bg    the background color for the panels
     * @param btnBg the background color for the buttons
     */
    protected void resetSideColors(Color bg, Color btnBg) {
        leftPanel.setBackground(bg);
        rightPanel.setBackground(bg);
        for (int i = 0; i < BTN_COUNT; i++) {
            leftBtns[i].setBackground(btnBg);
            rightBtns[i].setBackground(btnBg);
        }
    }
    
    /**
     * Called when this panel becomes visible. Subclasses should configure
     * their buttons and refresh their content.
     */
    public abstract void onShow();
}
