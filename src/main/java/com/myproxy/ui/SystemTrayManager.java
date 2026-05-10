package com.myproxy.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class SystemTrayManager {

    private final MainFrame mainFrame;
    private TrayIcon trayIcon;
    private SystemTray systemTray;
    private JPopupMenu popup;

    public SystemTrayManager(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void setup() {
        if (!SystemTray.isSupported()) {
            return;
        }
        systemTray = SystemTray.getSystemTray();

        Image image = createIcon();
        popup = createMenu();

        trayIcon = new TrayIcon(image, "MyProxy");
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    mainFrame.showWindow();
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    popup.setLocation(e.getX(), e.getY());
                    popup.setInvoker(popup);
                    popup.setVisible(true);
                }
            }
        });

        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public void remove() {
        if (systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
        }
    }

    private JPopupMenu createMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem showItem = new JMenuItem("\u663e\u793a\u7a97\u53e3");
        showItem.addActionListener(e -> mainFrame.showWindow());
        menu.add(showItem);

        menu.addSeparator();

        JMenuItem exitItem = new JMenuItem("\u9000\u51fa");
        exitItem.addActionListener(e -> {
            remove();
            mainFrame.exitApp();
        });
        menu.add(exitItem);

        return menu;
    }

    private Image createIcon() {
        int size = 16;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(46, 139, 87));
        g.fillOval(0, 0, size, size);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 11));
        g.drawString("P", 3, 13);
        g.dispose();
        return img;
    }
}
