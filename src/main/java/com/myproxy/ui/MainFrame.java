package com.myproxy.ui;

import com.myproxy.config.ConfigManager;
import com.myproxy.proxy.ProxyService;
import com.myproxy.proxy.ReverseProxyService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private final ProxyService proxyService;
    private final ReverseProxyService reverseProxyService;
    private final LogPanel logPanel;

    public MainFrame(ProxyService proxyService, ReverseProxyService reverseProxyService, ConfigManager configManager) {
        super("MyProxy - HTTP \u6b63\u5411/\u53cd\u5411\u4ee3\u7406");
        this.proxyService = proxyService;
        this.reverseProxyService = reverseProxyService;

        setSize(860, 650);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(5, 5));
        main.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // shared log panel
        logPanel = new LogPanel();

        // === Forward Proxy Tab ===
        JPanel forwardPanel = new JPanel(new BorderLayout(5, 5));
        StatusPanel statusPanel = new StatusPanel();
        ControlPanel controlPanel = new ControlPanel(proxyService, configManager);
        WhitelistPanel whitelistPanel = new WhitelistPanel(configManager);

        JPanel forwardTop = new JPanel(new BorderLayout(5, 5));
        forwardTop.add(statusPanel, BorderLayout.NORTH);
        forwardTop.add(controlPanel, BorderLayout.CENTER);
        forwardPanel.add(forwardTop, BorderLayout.NORTH);
        forwardPanel.add(whitelistPanel, BorderLayout.CENTER);

        // wire forward proxy callbacks
        proxyService.setLogConsumer(logPanel::appendLog);
        proxyService.setStatusConsumer(running -> {
            statusPanel.updateStatus(running);
            controlPanel.updateButtonState(running);
        });

        // === Reverse Proxy Tab ===
        ReverseProxyPanel reversePanel = new ReverseProxyPanel(reverseProxyService, configManager);
        reverseProxyService.setLogConsumer(logPanel::appendLog);

        // === Tabbed Pane ===
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("\u6b63\u5411\u4ee3\u7406", forwardPanel);
        tabbedPane.addTab("\u53cd\u5411\u4ee3\u7406", reversePanel);

        // upper: tabs, lower: logs
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.55);
        split.setTopComponent(tabbedPane);
        split.setBottomComponent(logPanel);

        main.add(split, BorderLayout.CENTER);
        add(main);

        // window close
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (SystemTray.isSupported()) {
                    setVisible(false);
                } else {
                    exitApp();
                }
            }
        });
    }

    public void showWindow() {
        setVisible(true);
        setExtendedState(Frame.NORMAL);
        toFront();
        requestFocus();
    }

    public void exitApp() {
        if (proxyService.isRunning()) {
            proxyService.stop();
        }
        if (reverseProxyService.isRunning()) {
            reverseProxyService.stop();
        }
        System.exit(0);
    }
}
