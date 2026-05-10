package com.myproxy.ui;

import com.myproxy.config.ConfigManager;
import com.myproxy.config.ProxyConfig;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Set;
import java.util.regex.Pattern;

public class WhitelistPanel extends JPanel {

    private static final Pattern IPV4 = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");
    private static final Pattern IPV6_SIMPLE = Pattern.compile(
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

    private final ConfigManager configManager;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField ipInput;
    private final JCheckBox enableCb;

    public WhitelistPanel(ConfigManager configManager) {
        this.configManager = configManager;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("IP \u767d\u540d\u5355"),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        // top row
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        enableCb = new JCheckBox("\u542f\u7528\u767d\u540d\u5355\u8fc7\u6ee4",
                configManager.getConfig().isWhitelistEnabled());
        enableCb.addActionListener(e -> {
            configManager.getConfig().setWhitelistEnabled(enableCb.isSelected());
            configManager.saveConfig();
        });
        top.add(enableCb);

        top.add(new JLabel("  IP\u5730\u5740:"));
        ipInput = new JTextField(15);
        top.add(ipInput);

        JButton addBtn = new JButton("\u6dfb\u52a0");
        addBtn.addActionListener(e -> addIp());
        top.add(addBtn);

        JButton removeBtn = new JButton("\u5220\u9664");
        removeBtn.addActionListener(e -> removeIp());
        top.add(removeBtn);

        add(top, BorderLayout.NORTH);

        // table
        tableModel = new DefaultTableModel(new String[]{"\u5e8f\u53f7", "IP \u5730\u5740"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(80);

        add(new JScrollPane(table), BorderLayout.CENTER);
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        Set<String> ips = configManager.getConfig().getAllowedIps();
        int idx = 1;
        for (String ip : ips) {
            tableModel.addRow(new Object[]{idx++, ip});
        }
    }

    private void addIp() {
        String ip = ipInput.getText().trim();
        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "\u8bf7\u8f93\u5165 IP \u5730\u5740");
            return;
        }
        if (!isValidIp(ip)) {
            JOptionPane.showMessageDialog(this,
                    "\u65e0\u6548\u7684 IP \u5730\u5740\u683c\u5f0f", "\u9519\u8bef",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        ProxyConfig cfg = configManager.getConfig();
        if (cfg.getAllowedIps().contains(ip)) {
            JOptionPane.showMessageDialog(this, "\u8be5 IP \u5df2\u5b58\u5728");
            return;
        }
        cfg.addAllowedIp(ip);
        configManager.saveConfig();
        refreshTable();
        ipInput.setText("");
    }

    private void removeIp() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "\u8bf7\u5148\u9009\u62e9\u8981\u5220\u9664\u7684 IP");
            return;
        }
        String ip = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "\u786e\u5b9a\u5220\u9664 " + ip + " \uff1f", "\u786e\u8ba4",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            configManager.getConfig().removeAllowedIp(ip);
            configManager.saveConfig();
            refreshTable();
        }
    }

    private boolean isValidIp(String ip) {
        return IPV4.matcher(ip).matches()
                || IPV6_SIMPLE.matcher(ip).matches()
                || "0:0:0:0:0:0:0:1".equals(ip)
                || "::1".equals(ip);
    }
}
