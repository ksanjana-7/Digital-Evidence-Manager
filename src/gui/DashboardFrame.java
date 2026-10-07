package gui;

import model.User;
import service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * DashboardFrame - Main application window after login.
 *
 * Uses a JTabbedPane to switch between functional panels.
 * Role-based panel visibility controls what each user can see.
 *
 * Swing Components:
 *   - JFrame, JTabbedPane, JMenuBar, JMenu, JMenuItem
 *   - JLabel for header information
 */
public class DashboardFrame extends JFrame {

    private final User         currentUser;
    private JTabbedPane        tabbedPane;

    // Color constants
    public static final Color BG_DARK      = new Color(18, 22, 36);
    public static final Color BG_PANEL     = new Color(26, 32, 50);
    public static final Color BG_SIDEBAR   = new Color(22, 28, 45);
    public static final Color ACCENT_BLUE  = new Color(64, 158, 255);
    public static final Color ACCENT_TEAL  = new Color(0, 210, 190);
    public static final Color TEXT_WHITE   = new Color(220, 230, 255);
    public static final Color TEXT_GRAY    = new Color(140, 155, 190);
    public static final Color SUCCESS_GREEN= new Color(80, 200, 120);
    public static final Color ERROR_RED    = new Color(255, 80, 80);
    public static final Color WARNING_ORANGE = new Color(255, 170, 50);
    public static final Color TABLE_ALT    = new Color(32, 40, 62);

    public DashboardFrame(User user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("Digital Evidence Chain Manager - " + currentUser.getRole() +
                ": " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));
        getContentPane().setBackground(BG_DARK);

        // Confirm before closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        // Menu Bar
        setJMenuBar(createMenuBar());

        // Main layout
        setLayout(new BorderLayout());
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTabbedPane(), BorderLayout.CENTER);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(BG_SIDEBAR);
        menuBar.setBorder(new EmptyBorder(2, 5, 2, 5));

        JMenu fileMenu = createMenu("File");
        JMenuItem refreshItem = createMenuItem("Refresh All");
        JMenuItem logoutItem  = createMenuItem("Logout");
        JMenuItem exitItem    = createMenuItem("Exit");
        refreshItem.addActionListener(e -> refreshAllPanels());
        logoutItem.addActionListener(e -> logout());
        exitItem.addActionListener(e -> confirmExit());
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(logoutItem);
        fileMenu.add(exitItem);

        JMenu helpMenu = createMenu("Help");
        JMenuItem aboutItem = createMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private JMenu createMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setForeground(TEXT_WHITE);
        menu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return menu;
    }

    private JMenuItem createMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setBackground(BG_PANEL);
        item.setForeground(TEXT_WHITE);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return item;
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_SIDEBAR);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 2, 0, ACCENT_BLUE),
                new EmptyBorder(10, 20, 10, 20)
        ));

        // Left: App title
        JLabel titleLabel = new JLabel("🔐 Digital Evidence Chain Manager");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_BLUE);

        // Right: User info
        String roleDisplay = currentUser.getRole().replace("_", " ");
        JLabel userLabel = new JLabel(currentUser.getFullName() + " | " +
                roleDisplay + " | Badge: " + currentUser.getBadgeNumber());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(TEXT_GRAY);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(60, 30, 30));
        logoutBtn.setForeground(ERROR_RED);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.setBorder(new LineBorder(ERROR_RED, 1, true));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> logout());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(BG_SIDEBAR);
        rightPanel.add(userLabel);
        rightPanel.add(logoutBtn);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JTabbedPane createTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BG_DARK);
        tabbedPane.setForeground(TEXT_WHITE);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("TabbedPane.selected", BG_PANEL);

        // Add panels based on role
        tabbedPane.addTab("📊 Dashboard",    createDashboardPanel());
        tabbedPane.addTab("📁 Cases",         new CasePanel(currentUser));
        tabbedPane.addTab("🔍 Evidence",      new EvidencePanel(currentUser));
        tabbedPane.addTab("🔄 Transfers",     new TransferPanel(currentUser));
        tabbedPane.addTab("✅ Verification",  new VerificationPanel(currentUser));
        tabbedPane.addTab("📋 Access Log",    new AccessLogPanel(currentUser));
        tabbedPane.addTab("📈 Reports",       new ReportPanel(currentUser));

        return tabbedPane;
    }

    /**
     * Creates the overview dashboard tab showing summary cards.
     */
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Welcome section
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(BG_DARK);

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(TEXT_WHITE);

        JLabel roleLabel = new JLabel(currentUser.getPermissionsDescription());
        roleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        roleLabel.setForeground(TEXT_GRAY);

        welcomePanel.add(welcomeLabel, BorderLayout.NORTH);
        welcomePanel.add(roleLabel, BorderLayout.SOUTH);

        // Info cards
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        cardsPanel.setBackground(BG_DARK);
        cardsPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        cardsPanel.add(createInfoCard("📁", "Cases", "View & Manage Cases", ACCENT_BLUE));
        cardsPanel.add(createInfoCard("🔍", "Evidence", "Register & Search Evidence", ACCENT_TEAL));
        cardsPanel.add(createInfoCard("🔄", "Transfers", "Chain of Custody", WARNING_ORANGE));
        cardsPanel.add(createInfoCard("✅", "Verification", "Hash Integrity Check", SUCCESS_GREEN));
        cardsPanel.add(createInfoCard("📋", "Audit Log", "Access History", new Color(200, 100, 255)));
        cardsPanel.add(createInfoCard("📈", "Reports", "Summary Reports", new Color(255, 150, 100)));

        panel.add(welcomePanel, BorderLayout.NORTH);
        panel.add(cardsPanel, BorderLayout.CENTER);

        // Quick tip panel
        JPanel tipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tipPanel.setBackground(BG_DARK);
        JLabel tipLabel = new JLabel("💡 Tip: Use the tabs above to navigate. "
                + "Transfer evidence from the Transfers tab using JDBC transactions.");
        tipLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        tipLabel.setForeground(TEXT_GRAY);
        tipPanel.add(tipLabel);
        panel.add(tipPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createInfoCard(String icon, String title, String subtitle, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_PANEL);
        card.setBorder(new CompoundBorder(
                new LineBorder(accent.darker(), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(accent);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel(subtitle, SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLabel.setForeground(TEXT_GRAY);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subLabel);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private void refreshAllPanels() {
        // Refresh by rebuilding the tabbed pane
        remove(tabbedPane);
        add(createTabbedPane(), BorderLayout.CENTER);
        revalidate();
        repaint();
        JOptionPane.showMessageDialog(this, "All panels refreshed.", "Refresh", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            new AuthenticationService().logout();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?", "Confirm Exit",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            new AuthenticationService().logout();
            System.exit(0);
        }
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "Digital Evidence Chain Manager\n" +
                "Version 1.0\n\n" +
                "B.Tech CSE Course Based Project\n" +
                "Demonstrates Java OOP + DBMS concepts\n\n" +
                "Technology Stack:\n" +
                "• Java 11+ with Swing GUI\n" +
                "• JDBC for MySQL connectivity\n" +
                "• MySQL 8.0+ database\n" +
                "• SHA-256 for evidence integrity",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }
}
