package com.myproxy.ui;

import com.myproxy.config.ConfigManager;
import com.myproxy.config.ProxyConfig;
import com.myproxy.proxy.ProxyService;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {

    private final ProxyService proxyService;
    private final ConfigManager configManager;

    private final JButton startButton;
    private final JButton stopButton;
    private final JButton restartButton;
    private final JTextField portField;

    public ControlPanel(ProxyService proxyService, ConfigManager configManager) {
        this.proxyService = proxyService;
        this.configManager = configManager;

        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("\u63a7\u5236\u9762\u677f"),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        add(new JLabel("\u4ee3\u7406\u7aef\u53e3:"));
        portField = new JTextField(String.valueOf(configManager.getConfig().getPort()), 6);
        add(portField);

        JButton applyPortBtn = new JButton("\u5e94\u7528\u7aef\u53e3");
        applyPortBtn.addActionListener(e -> applyPort());
        add(applyPortBtn);

        add(Box.createHorizontalStrut(20));

        startButton = new JButton("\u542f\u52a8\u4ee3\u7406");
        startButton.addActionListener(e -> new Thread(() -> proxyService.start()).start());
        add(startButton);

        stopButton = new JButton("\u505c\u6b62\u4ee3\u7406");
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> new Thread(() -> proxyService.stop()).start());
        add(stopButton);

        restartButton = new JButton("\u91cd\u542f\u4ee3\u7406");
        restartButton.setEnabled(false);
        restartButton.addActionListener(e -> new Thread(() -> proxyService.restart()).start());
        add(restartButton);
    }

    private void applyPort() {
        String text = portField.getText().trim();
        try {
            int port = Integer.parseInt(text);
            if (port < 1 || port > 65535) {
                JOptionPane.showMessageDialog(this,
                        "\u7aef\u53e3\u5fc5\u987b\u5728 1-65535 \u8303\u56f4\u5185", "\u65e0\u6548\u7aef\u53e3",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            ProxyConfig config = configManager.getConfig();
            config.setPort(port);
            configManager.saveConfig();

            if (proxyService.isRunning()) {
                int opt = JOptionPane.showConfirmDialog(this,
                        "\u7aef\u53e3\u5df2\u66f4\u6539\uff0c\u9700\u8981\u91cd\u542f\u4ee3\u7406\u3002\u662f\u5426\u7acb\u5373\u91cd\u542f\uff1f",
                        "\u786e\u8ba4", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    new Thread(() -> proxyService.restart()).start();
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "\u8bf7\u8f93\u5165\u6709\u6548\u7684\u7aef\u53e3\u53f7", "\u8f93\u5165\u9519\u8bef",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateButtonState(boolean isRunning) {
        startButton.setEnabled(!isRunning);
        stopButton.setEnabled(isRunning);
        restartButton.setEnabled(isRunning);
        portField.setEnabled(!isRunning);
    }
}
