package gui;

import model.Case;
import model.User;
import service.CaseService;
import exception.UnauthorizedAccessException;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CasePanel - Panel for managing investigation cases.
 *
 * Features:
 *   - View all cases in a JTable
 *   - Create new case (Investigators and Admins)
 *   - Search cases by keyword
 *   - Update case status
 *   - Close case (Admins only)
 */
public class CasePanel extends JPanel {

    private final User        currentUser;
    private final CaseService caseService = new CaseService();

    private JTable      caseTable;
    private DefaultTableModel tableModel;
    private JTextField  searchField;
    private JLabel      statusBar;

    private static final DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CasePanel(User currentUser) {
        this.currentUser = currentUser;
        initComponents();
        loadCases();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(DashboardFrame.BG_DARK);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setBackground(DashboardFrame.BG_SIDEBAR);
        toolbar.setBorder(new MatteBorder(0, 0, 1, 0, DashboardFrame.ACCENT_BLUE));

        // Search
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(DashboardFrame.TEXT_GRAY);
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        searchField = UIHelper.createTextField("");
        searchField.setPreferredSize(new Dimension(200, 32));
        JButton searchBtn = UIHelper.createButton("🔍 Search", DashboardFrame.ACCENT_BLUE);
        searchBtn.addActionListener(e -> searchCases());

        JButton refreshBtn = UIHelper.createButton("↻ Refresh", DashboardFrame.TEXT_GRAY);
        refreshBtn.addActionListener(e -> loadCases());

        toolbar.add(searchLabel);
        toolbar.add(searchField);
        toolbar.add(searchBtn);
        toolbar.add(refreshBtn);

        // Create Case (Admins + Investigators)
        if (!"FORENSIC_OFFICER".equals(currentUser.getRole())) {
            JButton createBtn = UIHelper.createButton("+ New Case", DashboardFrame.SUCCESS_GREEN);
            createBtn.addActionListener(e -> showCreateCaseDialog());
            toolbar.add(Box.createHorizontalStrut(20));
            toolbar.add(createBtn);
        }

        // Close Case (Admins only)
        if ("ADMIN".equals(currentUser.getRole())) {
            JButton closeBtn = UIHelper.createButton("Close Case", DashboardFrame.ERROR_RED);
            closeBtn.addActionListener(e -> closeCaseAction());
            toolbar.add(closeBtn);
        }

        return toolbar;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"Case ID", "Case Number", "Title", "Status", "Priority",
                            "Created By", "Created At", "Evidence Count"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        caseTable = new JTable(tableModel);
        UIHelper.styleTable(caseTable);

        // Double-click to view details
        caseTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showCaseDetails();
            }
        });

        JScrollPane scrollPane = new JScrollPane(caseTable);
        scrollPane.setBackground(DashboardFrame.BG_DARK);
        scrollPane.getViewport().setBackground(DashboardFrame.BG_DARK);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        return scrollPane;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bar.setBackground(DashboardFrame.BG_SIDEBAR);
        statusBar = new JLabel("Loading cases...");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.setForeground(DashboardFrame.TEXT_GRAY);
        bar.add(statusBar);
        return bar;
    }

    private void loadCases() {
        tableModel.setRowCount(0);
        statusBar.setText("Loading...");

        SwingWorker<List<Case>, Void> worker = new SwingWorker<>() {
            @Override protected List<Case> doInBackground() throws Exception {
                return caseService.getCasesForUser(currentUser);
            }
            @Override protected void done() {
                try {
                    List<Case> cases = get();
                    for (Case c : cases) {
                        tableModel.addRow(new Object[]{
                                c.getCaseId(),
                                c.getCaseNumber(),
                                c.getTitle(),
                                c.getStatus(),
                                c.getPriority(),
                                c.getCreatedByName(),
                                c.getCreatedAt() != null ? c.getCreatedAt().format(DT_FORMAT) : "",
                                ""
                        });
                    }
                    statusBar.setText("Loaded " + cases.size() + " cases.");
                } catch (Exception e) {
                    statusBar.setText("Error: " + e.getMessage());
                    JOptionPane.showMessageDialog(CasePanel.this,
                            "Error loading cases: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void searchCases() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { loadCases(); return; }

        tableModel.setRowCount(0);
        SwingWorker<List<Case>, Void> worker = new SwingWorker<>() {
            @Override protected List<Case> doInBackground() throws Exception {
                return caseService.searchCases(keyword);
            }
            @Override protected void done() {
                try {
                    List<Case> cases = get();
                    for (Case c : cases) {
                        tableModel.addRow(new Object[]{
                                c.getCaseId(), c.getCaseNumber(), c.getTitle(),
                                c.getStatus(), c.getPriority(), c.getCreatedByName(),
                                c.getCreatedAt() != null ? c.getCreatedAt().format(DT_FORMAT) : "", ""
                        });
                    }
                    statusBar.setText("Found " + cases.size() + " cases for: " + keyword);
                } catch (Exception e) {
                    statusBar.setText("Search error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showCreateCaseDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Create New Case", true);
        dialog.setSize(500, 420);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(DashboardFrame.BG_PANEL);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(DashboardFrame.BG_PANEL);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLbl = new JLabel("Create New Investigation Case");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(DashboardFrame.ACCENT_BLUE);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField titleField = UIHelper.createTextField("Case title");
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JTextArea descArea = new JTextArea(4, 30);
        descArea.setBackground(DashboardFrame.BG_DARK);
        descArea.setForeground(DashboardFrame.TEXT_WHITE);
        descArea.setCaretColor(DashboardFrame.ACCENT_BLUE);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setBorder(new LineBorder(new Color(60, 80, 120), 1));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] priorities = {Case.PRIORITY_LOW, Case.PRIORITY_MEDIUM,
                               Case.PRIORITY_HIGH, Case.PRIORITY_CRITICAL};
        JComboBox<String> priorityCombo = UIHelper.createComboBox(priorities);
        priorityCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        priorityCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton createBtn = UIHelper.createButton("Create Case", DashboardFrame.SUCCESS_GREEN);
        createBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        createBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String desc  = descArea.getText().trim();
            String prio  = (String) priorityCombo.getSelectedItem();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Case title is required.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Case c = caseService.createCase(currentUser, title, desc, prio);
                JOptionPane.showMessageDialog(dialog,
                        "Case created successfully!\nCase Number: " + c.getCaseNumber(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadCases();
            } catch (UnauthorizedAccessException ex) {
                JOptionPane.showMessageDialog(dialog, "Access denied: " + ex.getMessage(),
                        "Unauthorized", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(16));
        panel.add(UIHelper.createFieldLabel("Case Title *"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(titleField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(UIHelper.createFieldLabel("Description"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(descScroll);
        panel.add(Box.createVerticalStrut(12));
        panel.add(UIHelper.createFieldLabel("Priority"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(priorityCombo);
        panel.add(Box.createVerticalStrut(20));
        panel.add(createBtn);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showCaseDetails() {
        int row = caseTable.getSelectedRow();
        if (row < 0) return;
        int caseId = (Integer) tableModel.getValueAt(row, 0);
        try {
            Case c = caseService.findCaseById(caseId);
            if (c == null) return;
            String details = String.format(
                    "Case ID:       %d\n" +
                    "Case Number:   %s\n" +
                    "Title:         %s\n" +
                    "Status:        %s\n" +
                    "Priority:      %s\n" +
                    "Created By:    %s\n" +
                    "Created At:    %s\n\n" +
                    "Description:\n%s",
                    c.getCaseId(), c.getCaseNumber(), c.getTitle(),
                    c.getStatus(), c.getPriority(), c.getCreatedByName(),
                    c.getCreatedAt() != null ? c.getCreatedAt().format(DT_FORMAT) : "N/A",
                    c.getDescription()
            );
            JOptionPane.showMessageDialog(this, details, "Case Details", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading case: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void closeCaseAction() {
        int row = caseTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a case first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int caseId = (Integer) tableModel.getValueAt(row, 0);
        String caseNumber = (String) tableModel.getValueAt(row, 1);
        int choice = JOptionPane.showConfirmDialog(this,
                "Close case " + caseNumber + "? This action changes case status to CLOSED.",
                "Confirm Close", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                caseService.closeCase(currentUser, caseId);
                JOptionPane.showMessageDialog(this, "Case closed successfully.", "Closed",
                        JOptionPane.INFORMATION_MESSAGE);
                loadCases();
            } catch (UnauthorizedAccessException ex) {
                JOptionPane.showMessageDialog(this, "Access denied: " + ex.getMessage(),
                        "Unauthorized", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
