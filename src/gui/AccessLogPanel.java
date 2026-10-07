package gui;

import model.AccessLog;
import model.User;
import dao.AccessLogDAO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * AccessLogPanel - Displays the audit/access history log.
 *
 * Shows all audit events: login, transfers, verifications, registrations, etc.
 * Most entries are created automatically by DB triggers.
 */
public class AccessLogPanel extends JPanel {

    private final User         currentUser;
    private final AccessLogDAO accessLogDAO = new AccessLogDAO();

    private JTable            logTable;
    private DefaultTableModel tableModel;
    private JLabel            statusBar;
    private JComboBox<String> filterCombo;

    private static final DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AccessLogPanel(User currentUser) {
        this.currentUser = currentUser;
        initComponents();
        loadLogs();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(DashboardFrame.BG_DARK);
        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setBackground(DashboardFrame.BG_SIDEBAR);
        toolbar.setBorder(new MatteBorder(0, 0, 1, 0, DashboardFrame.ACCENT_BLUE));

        JLabel filterLabel = new JLabel("Filter by Action:");
        filterLabel.setForeground(DashboardFrame.TEXT_GRAY);
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String[] actions = {"ALL", "LOGIN", "LOGOUT", "VIEW", "REGISTER",
                "UPDATE", "TRANSFER", "VERIFY", "DOWNLOAD", "CREATE_CASE",
                "CLOSE_CASE", "ASSIGN_INVESTIGATOR"};
        filterCombo = UIHelper.createComboBox(actions);
        filterCombo.setPreferredSize(new Dimension(160, 32));

        JButton applyBtn = UIHelper.createButton("Apply Filter", DashboardFrame.ACCENT_BLUE);
        applyBtn.addActionListener(e -> loadLogs());

        JButton refreshBtn = UIHelper.createButton("↻ Refresh", DashboardFrame.TEXT_GRAY);
        refreshBtn.addActionListener(e -> { filterCombo.setSelectedIndex(0); loadLogs(); });

        JButton evidenceLogBtn = UIHelper.createButton("Evidence Log", DashboardFrame.ACCENT_TEAL);
        evidenceLogBtn.addActionListener(e -> showEvidenceLog());

        toolbar.add(filterLabel);
        toolbar.add(filterCombo);
        toolbar.add(applyBtn);
        toolbar.add(refreshBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(evidenceLogBtn);

        return toolbar;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"Log ID", "User", "Action", "Evidence No.",
                            "Case No.", "Description", "Timestamp"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        logTable = new JTable(tableModel);
        UIHelper.styleTable(logTable);

        // Color by action
        logTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    String action = (String) table.getValueAt(row, 2);
                    if (action == null) action = "";
                    switch (action) {
                        case "TRANSFER": c.setBackground(new Color(30, 40, 70)); break;
                        case "VERIFY":   c.setBackground(new Color(20, 50, 30)); break;
                        case "LOGIN":    c.setBackground(new Color(30, 50, 20)); break;
                        case "REGISTER": c.setBackground(new Color(30, 40, 60)); break;
                        default:         c.setBackground(row % 2 == 0 ? DashboardFrame.BG_DARK : DashboardFrame.TABLE_ALT);
                    }
                    c.setForeground(DashboardFrame.TEXT_WHITE);
                }
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(logTable);
        sp.getViewport().setBackground(DashboardFrame.BG_DARK);
        return sp;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bar.setBackground(DashboardFrame.BG_SIDEBAR);
        statusBar = new JLabel("Loading audit log...");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.setForeground(DashboardFrame.TEXT_GRAY);
        bar.add(statusBar);
        return bar;
    }

    private void loadLogs() {
        tableModel.setRowCount(0);
        String selectedAction = (String) filterCombo.getSelectedItem();

        SwingWorker<List<AccessLog>, Void> worker = new SwingWorker<>() {
            @Override protected List<AccessLog> doInBackground() throws Exception {
                if ("ADMIN".equals(currentUser.getRole())) {
                    return accessLogDAO.getAllLogs();
                } else {
                    return accessLogDAO.getLogsByUser(currentUser.getUserId());
                }
            }
            @Override protected void done() {
                try {
                    List<AccessLog> logs = get();
                    for (AccessLog log : logs) {
                        if (!"ALL".equals(selectedAction) && !selectedAction.equals(log.getAction())) {
                            continue;
                        }
                        tableModel.addRow(new Object[]{
                                log.getLogId(),
                                log.getUserFullName(),
                                log.getAction(),
                                log.getEvidenceNumber() != null ? log.getEvidenceNumber() : "—",
                                log.getCaseNumber() != null ? log.getCaseNumber() : "—",
                                log.getDescription(),
                                log.getLoggedAt() != null ? log.getLoggedAt().format(DT_FORMAT) : ""
                        });
                    }
                    statusBar.setText("Showing " + tableModel.getRowCount() + " log entries.");
                } catch (Exception e) {
                    statusBar.setText("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showEvidenceLog() {
        String input = JOptionPane.showInputDialog(this,
                "Enter Evidence ID to view its access history:", "Evidence Access Log",
                JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int evId = Integer.parseInt(input.trim());
            List<AccessLog> logs = accessLogDAO.getLogsByEvidence(evId);
            tableModel.setRowCount(0);
            for (AccessLog log : logs) {
                tableModel.addRow(new Object[]{
                        log.getLogId(), log.getUserFullName(), log.getAction(),
                        log.getEvidenceNumber() != null ? log.getEvidenceNumber() : "—",
                        log.getCaseNumber() != null ? log.getCaseNumber() : "—",
                        log.getDescription(),
                        log.getLoggedAt() != null ? log.getLoggedAt().format(DT_FORMAT) : ""
                });
            }
            statusBar.setText("Access history for Evidence ID " + evId + ": " + logs.size() + " entries.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid ID.", "Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
