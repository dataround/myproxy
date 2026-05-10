package com.myproxy.ui;

import com.myproxy.config.ConfigManager;
import com.myproxy.config.ProxyConfig;
import com.myproxy.proxy.ReverseProxyService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ReverseProxyPanel extends JPanel {

    private final ConfigManager configManager;
    private final ReverseProxyService reverseProxyService;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField domainInput;
    private final JTextField targetInput;
    private final JTextField portField;
    private final JButton startBtn;
    private final JButton stopBtn;
    private final JLabel statusLabel;

    public ReverseProxyPanel(ReverseProxyService reverseProxyService, ConfigManager configManager) {
        this.configManager = configManager;
        this.reverseProxyService = reverseProxyService;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // === Top: port + control ===
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("\u53cd\u5411\u4ee3\u7406\u63a7\u5236"));

        statusLabel = new JLabel("\u25cb \u5df2\u505c\u6b62");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        statusLabel.setForeground(new Color(178, 34, 34));
        controlPanel.add(statusLabel);
        controlPanel.add(Box.createHorizontalStrut(15));

        controlPanel.add(new JLabel("\u76d1\u542c\u7aef\u53e3:"));
        portField = new JTextField(String.valueOf(configManager.getConfig().getReverseProxyPort()), 6);
        controlPanel.add(portField);

        startBtn = new JButton("\u542f\u52a8");
        startBtn.addActionListener(e -> startReverseProxy());
        controlPanel.add(startBtn);

        stopBtn = new JButton("\u505c\u6b62");
        stopBtn.setEnabled(false);
        stopBtn.addActionListener(e -> new Thread(() -> reverseProxyService.stop()).start());
        controlPanel.add(stopBtn);

        add(controlPanel, BorderLayout.NORTH);

        // === Center: rules table ===
        JPanel rulesPanel = new JPanel(new BorderLayout(5, 5));
        rulesPanel.setBorder(BorderFactory.createTitledBorder("\u8def\u7531\u89c4\u5219 (\u57df\u540d -> \u540e\u7aef\u5730\u5740)"));

        // input row
        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        inputRow.add(new JLabel("\u57df\u540d:"));
        domainInput = new JTextField(15);
        inputRow.add(domainInput);
        inputRow.add(new JLabel("\u540e\u7aef\u5730\u5740:"));
        targetInput = new JTextField(20);
        inputRow.add(targetInput);

        JButton addBtn = new JButton("\u6dfb\u52a0");
        addBtn.addActionListener(e -> addRule());
        inputRow.add(addBtn);

        JButton removeBtn = new JButton("\u5220\u9664");
        removeBtn.addActionListener(e -> removeRule());
        inputRow.add(removeBtn);

        rulesPanel.add(inputRow, BorderLayout.NORTH);

        // table
        tableModel = new DefaultTableModel(new String[]{"\u5e8f\u53f7", "\u57df\u540d", "\u540e\u7aef\u5730\u5740"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(280);

        rulesPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // hint
        JLabel hint = new JLabel("  \u793a\u4f8b: \u57df\u540d=api.example.com  \u540e\u7aef\u5730\u5740=http://192.168.1.100:8080");
        hint.setForeground(Color.GRAY);
        rulesPanel.add(hint, BorderLayout.SOUTH);

        add(rulesPanel, BorderLayout.CENTER);

        // wire status callback
        reverseProxyService.setStatusConsumer(this::updateStatus);

        refreshTable();
    }

    private void startReverseProxy() {
        // save port first
        String portText = portField.getText().trim();
        try {
            int port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                JOptionPane.showMessageDialog(this, "\u7aef\u53e3\u5fc5\u987b\u5728 1-65535 \u8303\u56f4\u5185");
                return;
            }
            configManager.getConfig().setReverseProxyPort(port);
            configManager.saveConfig();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "\u8bf7\u8f93\u5165\u6709\u6548\u7684\u7aef\u53e3\u53f7");
            return;
        }
        new Thread(() -> reverseProxyService.start()).start();
    }

    private void addRule() {
        String domain = domainInput.getText().trim();
        String target = targetInput.getText().trim();
        if (domain.isEmpty() || target.isEmpty()) {
            JOptionPane.showMessageDialog(this, "\u57df\u540d\u548c\u540e\u7aef\u5730\u5740\u4e0d\u80fd\u4e3a\u7a7a");
            return;
        }
        // ensure target has scheme
        if (!target.startsWith("http://") && !target.startsWith("https://")) {
            target = "http://" + target;
            targetInput.setText(target);
        }

        List<ProxyConfig.ReverseProxyRule> rules = configManager.getConfig().getReverseProxyRules();
        // check duplicate
        for (ProxyConfig.ReverseProxyRule r : rules) {
            if (r.getDomain().equalsIgnoreCase(domain)) {
                JOptionPane.showMessageDialog(this, "\u57df\u540d " + domain + " \u5df2\u5b58\u5728");
                return;
            }
        }
        rules.add(new ProxyConfig.ReverseProxyRule(domain, target));
        configManager.saveConfig();
        refreshTable();
        domainInput.setText("");
        targetInput.setText("");
    }

    private void removeRule() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "\u8bf7\u5148\u9009\u62e9\u8981\u5220\u9664\u7684\u89c4\u5219");
            return;
        }
        List<ProxyConfig.ReverseProxyRule> rules = configManager.getConfig().getReverseProxyRules();
        rules.remove(row);
        configManager.saveConfig();
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<ProxyConfig.ReverseProxyRule> rules = configManager.getConfig().getReverseProxyRules();
        int idx = 1;
        for (ProxyConfig.ReverseProxyRule rule : rules) {
            tableModel.addRow(new Object[]{idx++, rule.getDomain(), rule.getTarget()});
        }
    }

    private void updateStatus(boolean isRunning) {
        SwingUtilities.invokeLater(() -> {
            if (isRunning) {
                statusLabel.setText("\u25cf \u8fd0\u884c\u4e2d");
                statusLabel.setForeground(new Color(46, 139, 87));
            } else {
                statusLabel.setText("\u25cb \u5df2\u505c\u6b62");
                statusLabel.setForeground(new Color(178, 34, 34));
            }
            startBtn.setEnabled(!isRunning);
            stopBtn.setEnabled(isRunning);
            portField.setEnabled(!isRunning);
        });
    }
}
