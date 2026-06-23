import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class NumpadPanel extends JPanel implements I18nController.I18nListener {
    private JButton[] numBtns;
    private JButton clrBtn, delBtn, okBtn, cancelBtn, dotBtn;
    private List<NumpadListener> listeners = new ArrayList<>();
    private I18nController i18n;

    public NumpadPanel() {
        i18n = I18nController.getInstance();
        i18n.addListener(this);
        setLayout(new BorderLayout());
        setBackground(new Color(220, 220, 225));

        JPanel grid = new JPanel(new GridLayout(4, 4, 4, 4));
        grid.setBackground(new Color(220, 220, 225));
        grid.setBorder(BorderFactory.createEmptyBorder(6, 80, 4, 80));

        numBtns = new JButton[10];

        numBtns[1] = numBtn("1", false); grid.add(numBtns[1]);
        numBtns[2] = numBtn("2", false); grid.add(numBtns[2]);
        numBtns[3] = numBtn("3", false); grid.add(numBtns[3]);
        cancelBtn = actionBtn("X " + i18n.get("button.cancel", "Cancel"), new Color(200, 50, 40), Color.WHITE);
        cancelBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        grid.add(cancelBtn);

        numBtns[4] = numBtn("4", false); grid.add(numBtns[4]);
        numBtns[5] = numBtn("5", true);  grid.add(numBtns[5]);
        numBtns[6] = numBtn("6", false); grid.add(numBtns[6]);
        clrBtn = actionBtn("\u232B " + i18n.get("button.clear", "Clear"), new Color(240, 200, 40), Color.BLACK);
        clrBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        grid.add(clrBtn);

        numBtns[7] = numBtn("7", false); grid.add(numBtns[7]);
        numBtns[8] = numBtn("8", false); grid.add(numBtns[8]);
        numBtns[9] = numBtn("9", false); grid.add(numBtns[9]);
        okBtn = actionBtn("\u25EF " + i18n.get("button.enter", "Enter"), new Color(0, 140, 60), Color.WHITE);
        okBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        grid.add(okBtn);

        dotBtn = new JButton(".");
        dotBtn.setFont(new Font("SansSerif", Font.BOLD, 22));
        dotBtn.setBackground(Color.WHITE);
        dotBtn.setFocusPainted(false);
        grid.add(dotBtn);
        numBtns[0] = numBtn("0", false); grid.add(numBtns[0]);
        delBtn = actionBtn(i18n.get("button.del", "DEL"), new Color(180, 180, 190), Color.BLACK);
        grid.add(delBtn);
        JLabel blank = new JLabel();
        blank.setOpaque(true);
        blank.setBackground(new Color(220, 220, 225));
        grid.add(blank);

        add(grid, BorderLayout.CENTER);
        bindActions();
    }

    private JButton numBtn(String t, boolean raisedDot) {
        String html = "<html><center>"
                + "<span style='font-size:20pt;font-weight:bold'>" + t + "</span>"
                + (raisedDot ? "<br><span style='font-size:12pt;color:#c44'>\u25CF</span>" : "")
                + "</center></html>";
        JButton b = new JButton(html);
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private JButton actionBtn(String t, Color bg, Color fg) {
        JButton b = new JButton("<html><center>" + t + "</center></html>");
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        return b;
    }

    public void addNumpadListener(NumpadListener listener) {
        listeners.add(listener);
    }

    public void setOkText(String text) {
        okBtn.setText("<html><center>\u25EF " + text + "</center></html>");
    }

    public void setCancelText(String text) {
        cancelBtn.setText("<html><center>X " + text + "</center></html>");
    }

    private void bindActions() {
        for (int i = 0; i <= 9; i++) {
            final int d = i;
            numBtns[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    for (NumpadListener l : listeners) l.onNumber(d);
                }
            });
        }
        dotBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (NumpadListener l : listeners) l.onDot();
            }
        });
        clrBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (NumpadListener l : listeners) l.onClear();
            }
        });
        delBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (NumpadListener l : listeners) l.onDelete();
            }
        });
        okBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (NumpadListener l : listeners) l.onOk();
            }
        });
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (NumpadListener l : listeners) l.onCancel();
            }
        });
    }

    @Override
    public void onLanguageChanged(String language) {
        cancelBtn.setText("<html><center>X " + i18n.get("button.cancel", "Cancel") + "</center></html>");
        clrBtn.setText("<html><center>\u232B " + i18n.get("button.clear", "Clear") + "</center></html>");
        delBtn.setText("<html><center>" + i18n.get("button.del", "DEL") + "</center></html>");
        okBtn.setText("<html><center>\u25EF " + i18n.get("button.ok", "OK") + "</center></html>");
    }
}
