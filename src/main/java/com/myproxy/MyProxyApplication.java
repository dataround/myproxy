package com.myproxy;

import com.myproxy.config.ConfigManager;
import com.myproxy.proxy.ProxyService;
import com.myproxy.proxy.ReverseProxyService;
import com.myproxy.ui.MainFrame;
import com.myproxy.ui.SystemTrayManager;

import javax.swing.*;
import java.awt.*;

public class MyProxyApplication {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            try {
                ConfigManager configManager = new ConfigManager();
                configManager.loadConfig();

                ProxyService proxyService = new ProxyService(configManager);
                ReverseProxyService reverseProxyService = new ReverseProxyService(configManager);

                MainFrame mainFrame = new MainFrame(proxyService, reverseProxyService, configManager);

                if (SystemTray.isSupported()) {
                    SystemTrayManager tray = new SystemTrayManager(mainFrame);
                    tray.setup();
                }

                mainFrame.setVisible(true);

                // 启动时自动开启正向代理
                new Thread(() -> proxyService.start()).start();

                // 如果反向代理配置了规则且启用，也自动启动
                if (configManager.getConfig().isReverseProxyEnabled()
                        && !configManager.getConfig().getReverseProxyRules().isEmpty()) {
                    new Thread(() -> reverseProxyService.start()).start();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "\u542f\u52a8\u5931\u8d25: " + e.getMessage(),
                        "\u9519\u8bef", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
