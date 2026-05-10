package com.myproxy.ui;

import javax.swing.*;
import java.awt.*;

public class StatusPanel extends JPanel {

    private static final Color COLOR_RUNNING = new Color(46, 139, 87);
    private static final Color COLOR_STOPPED = new Color(178, 34, 34);

    private final JLabel statusIcon;
    private final JLabel statusLabel;

    public StatusPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("\u4ee3\u7406\u72b6\u6001"),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        statusIcon = new JLabel();
        statusIcon.setFont(statusIcon.getFont().deriveFont(Font.BOLD, 16f));

        statusLabel = new JLabel();
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));

        add(statusIcon);
        add(statusLabel);
        updateStatus(false);
    }

    public void updateStatus(boolean running) {
        if (running) {
            statusIcon.setText("\u25cf");
            statusIcon.setForeground(COLOR_RUNNING);
            statusLabel.setText("\u8fd0\u884c\u4e2d");
            statusLabel.setForeground(COLOR_RUNNING);
        } else {
            statusIcon.setText("\u25cb");
            statusIcon.setForeground(COLOR_STOPPED);
            statusLabel.setText("\u5df2\u505c\u6b62");
            statusLabel.setForeground(COLOR_STOPPED);
        }
    }
}
