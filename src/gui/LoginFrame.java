package gui;

import service.AuthenticationService;
import model.User;
import util.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

/**
 * LoginFrame - The login screen for the Digital Evidence Chain Manager.
 *
 * Swing Components Used:
 *   - JFrame, JPanel, JLabel, JTextField, JPasswordField, JButton
 *   - ActionListener for button events
 *   - KeyAdapter for Enter key support
 *
 * Design: Dark professional theme with credential fields.
 */
public class LoginFrame extends JFrame {

    // UI Components
    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JButton        loginButton;
    private JLabel         statusLabel;
    private JLabel         connectionLabel;

    private final AuthenticationService authService = new AuthenticationService();

    // Color scheme
    private static final Color BG_DARK      = new Color(18, 22, 36);
    private static final Color BG_PANEL     = new Color(26, 32, 50);
    private static final Color BG_FIELD     = new Color(35, 43, 65);
    private static final Color ACCENT_BLUE  = new Color(64, 158, 255);
    private static final Color TEXT_WHITE   = new Color(220, 230, 255);
    private static final Color TEXT_GRAY    = new Color(140, 155, 190);
    private static final Color SUCCESS_GREEN= new Color(80, 200, 120);
    private static final Color ERROR_RED    = new Color(255, 80, 80);

    public LoginFrame() {
        initComponents();
        checkDatabaseConnection();
    }

    private void initComponents() {
        setTitle("Digital Evidence Chain Manager - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);

        // Main layout
        setLayout(new BorderLayout());

        // Center panel
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

        // Footer
        JPanel footer = createFooterPanel();
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createMainPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_DARK);
        outer.setBorder(new EmptyBorder(40, 40, 20, 40));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_PANEL);
        card.setBorder(new CompoundBorder(
                new LineBorder(ACCENT_BLUE, 1, true),
                new EmptyBorder(40, 40, 40, 40)
        ));

        // ---- LOGO / TITLE ----
        JLabel iconLabel = new JLabel("🔐", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setForeground(ACCENT_BLUE);

        JLabel titleLabel = new JLabel("Digital Evidence Chain Manager", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Secure Login", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---- USERNAME ----
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setForeground(TEXT_GRAY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = createTextField("Enter username");
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ---- PASSWORD ----
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLabel.setForeground(TEXT_GRAY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        stylePasswordField(passwordField, "Enter password");
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ---- STATUS LABEL ----
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(ERROR_RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---- LOGIN BUTTON ----
        loginButton = createLoginButton();
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---- DB CONNECTION STATUS ----
        connectionLabel = new JLabel("Checking database connection...", SwingConstants.CENTER);
        connectionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        connectionLabel.setForeground(TEXT_GRAY);
        connectionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---- DEMO CREDENTIALS ----
        JLabel demoLabel = new JLabel("Demo: admin / password123", SwingConstants.CENTER);
        demoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        demoLabel.setForeground(TEXT_GRAY);
        demoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---- Assemble ----
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(28));
        card.add(userLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(16));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(8));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(16));
        card.add(connectionLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(demoLabel);

        outer.add(card);
        return outer;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(BG_DARK);
        JLabel footerLabel = new JLabel("© 2024 Digital Evidence Chain Manager | B.Tech CSE Project");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footerLabel.setForeground(TEXT_GRAY);
        footer.add(footerLabel);
        return footer;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    g.setColor(TEXT_GRAY);
                    g.setFont(getFont().deriveFont(Font.ITALIC));
                    g.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        styleTextComponent(field);
        return field;
    }

    private void stylePasswordField(JPasswordField field, String placeholder) {
        field.setBackground(BG_FIELD);
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(ACCENT_BLUE);
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(60, 80, 120), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    }

    private void styleTextComponent(JTextField field) {
        field.setBackground(BG_FIELD);
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(ACCENT_BLUE);
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(60, 80, 120), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    }

    private JButton createLoginButton() {
        JButton btn = new JButton("LOGIN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(ACCENT_BLUE.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(ACCENT_BLUE.brighter());
                } else {
                    g2.setColor(ACCENT_BLUE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };

        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setPreferredSize(new Dimension(300, 45));

        // ActionListener for login button
        btn.addActionListener(e -> performLogin());

        // Enter key support on password field
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        });

        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) passwordField.requestFocus();
            }
        });

        return btn;
    }

    /**
     * Performs login when the button is clicked or Enter is pressed.
     * Uses SwingWorker to avoid blocking the UI thread (basic threading).
     */
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Please enter username and password.", false);
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("Authenticating...");
        statusLabel.setForeground(TEXT_GRAY);

        // Use SwingWorker to perform authentication on a background thread
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                try {
                    User user = get();
                    if (user != null) {
                        setStatus("Login successful! Welcome, " + user.getFullName(), true);
                        // Open the dashboard
                        openDashboard(user);
                    } else {
                        setStatus("Invalid username or password.", false);
                        passwordField.setText("");
                    }
                } catch (Exception e) {
                    setStatus("Error: " + e.getCause().getMessage(), false);
                }
            }
        };
        worker.execute();
    }

    private void setStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setForeground(success ? SUCCESS_GREEN : ERROR_RED);
    }

    private void openDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            DashboardFrame dashboard = new DashboardFrame(user);
            dashboard.setVisible(true);
            dispose();  // Close login window
        });
    }

    /**
     * Checks database connectivity and shows status.
     */
    private void checkDatabaseConnection() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return DBConnection.testConnection();
            }

            @Override
            protected void done() {
                try {
                    boolean connected = get();
                    if (connected) {
                        connectionLabel.setText("✓ Database connected");
                        connectionLabel.setForeground(SUCCESS_GREEN);
                    } else {
                        connectionLabel.setText("✗ Database not connected - Check MySQL settings");
                        connectionLabel.setForeground(ERROR_RED);
                    }
                } catch (Exception e) {
                    connectionLabel.setText("✗ Connection error");
                    connectionLabel.setForeground(ERROR_RED);
                }
            }
        };
        worker.execute();
    }
}
