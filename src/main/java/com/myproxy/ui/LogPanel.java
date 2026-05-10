package com.myproxy.ui;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogPanel extends JPanel {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_LINES = 2000;

    private final JTextArea logArea;
    private final JCheckBox autoScroll;

    public LogPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("\u8fde\u63a5\u65e5\u5fd7"),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 13));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(220, 220, 220));
        logArea.setCaretColor(new Color(220, 220, 220));

        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane sp = new JScrollPane(logArea);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 3));
        autoScroll = new JCheckBox("\u81ea\u52a8\u6eda\u52a8", true);
        bottom.add(autoScroll);

        JButton clearBtn = new JButton("\u6e05\u7a7a\u65e5\u5fd7");
        clearBtn.addActionListener(e -> logArea.setText(""));
        bottom.add(clearBtn);

        add(bottom, BorderLayout.SOUTH);
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String line = "[" + LocalDateTime.now().format(DT_FMT) + "] " + message + "\n";
            logArea.append(line);

            // trim old lines
            if (logArea.getLineCount() > MAX_LINES) {
                try {
                    int end = logArea.getLineStartOffset(logArea.getLineCount() - MAX_LINES);
                    logArea.replaceRange("", 0, end);
                } catch (Exception ignored) {
                }
            }

            if (autoScroll.isSelected()) {
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }
}
