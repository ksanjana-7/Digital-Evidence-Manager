package gui;

import model.Evidence;
import model.Case;
import model.User;
import service.EvidenceService;
import service.CaseService;
import util.FileUtil;
import util.HashUtil;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * EvidencePanel - Panel for registering and searching digital evidence.
 *
 * Features:
 *   - View all accessible evidence
 *   - Register new evidence with file attachment
 *   - File hash computed automatically
 *   - Search evidence
 *   - View evidence details
 */
public class EvidencePanel extends JPanel {

    private final User            currentUser;
    private final EvidenceService evidenceService = new EvidenceService();
    private final CaseService     caseService     = new CaseService();

    private JTable            evidenceTable;
    private DefaultTableModel tableModel;
    private JTextField        searchField;
    private JLabel            statusBar;

    private static final DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EvidencePanel(User currentUser) {
        this.currentUser = currentUser;
        initComponents();
        loadEvidence();
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

        searchField = UIHelper.createTextField("Search evidence...");
        searchField.setPreferredSize(new Dimension(220, 32));
        JButton searchBtn = UIHelper.createButton("🔍 Search", DashboardFrame.ACCENT_BLUE);
        searchBtn.addActionListener(e -> searchEvidence());

        JButton refreshBtn = UIHelper.createButton("↻ Refresh", DashboardFrame.TEXT_GRAY);
        refreshBtn.addActionListener(e -> loadEvidence());

        JButton viewAllBtn = UIHelper.createButton("📋 View All", DashboardFrame.ACCENT_TEAL);
        viewAllBtn.addActionListener(e -> loadAllEvidence());

        toolbar.add(new JLabel("  "));
        toolbar.add(searchField);
        toolbar.add(searchBtn);
        toolbar.add(refreshBtn);
        toolbar.add(viewAllBtn);

        // Register evidence (Admin + Investigators)
        if (!"FORENSIC_OFFICER".equals(currentUser.getRole())) {
            JButton registerBtn = UIHelper.createButton("+ Register Evidence", DashboardFrame.SUCCESS_GREEN);
            registerBtn.addActionListener(e -> showRegisterDialog());
            toolbar.add(Box.createHorizontalStrut(20));
            toolbar.add(registerBtn);
        }

        // View details
        JButton detailsBtn = UIHelper.createButton("🔍 View Details", DashboardFrame.WARNING_ORANGE);
        detailsBtn.addActionListener(e -> showEvidenceDetails());
        toolbar.add(detailsBtn);

        return toolbar;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"ID", "Ev. Number", "Case", "Type", "Description",
                            "Collected By", "Status", "Current Holder", "Registered At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        evidenceTable = new JTable(tableModel);
        UIHelper.styleTable(evidenceTable);
        // Row renderer for status-based coloring
        evidenceTable.setDefaultRenderer(Object.class, new EvidenceStatusRenderer());

        evidenceTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showEvidenceDetails();
            }
        });

        JScrollPane scrollPane = new JScrollPane(evidenceTable);
        scrollPane.getViewport().setBackground(DashboardFrame.BG_DARK);
        return scrollPane;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bar.setBackground(DashboardFrame.BG_SIDEBAR);
        statusBar = new JLabel("Loading evidence...");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.setForeground(DashboardFrame.TEXT_GRAY);
        bar.add(statusBar);
        return bar;
    }

    private void loadEvidence() {
        tableModel.setRowCount(0);
        SwingWorker<List<Evidence>, Void> worker = new SwingWorker<>() {
            @Override protected List<Evidence> doInBackground() throws Exception {
                if ("ADMIN".equals(currentUser.getRole())) {
                    return evidenceService.getAllEvidence();
                } else {
                    return evidenceService.getEvidenceByHolder(currentUser.getUserId());
                }
            }
            @Override protected void done() {
                try {
                    populateTable(get());
                } catch (Exception e) {
                    statusBar.setText("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadAllEvidence() {
        tableModel.setRowCount(0);
        SwingWorker<List<Evidence>, Void> worker = new SwingWorker<>() {
            @Override protected List<Evidence> doInBackground() throws Exception {
                return evidenceService.getAllEvidence();
            }
            @Override protected void done() {
                try { populateTable(get()); }
                catch (Exception e) { statusBar.setText("Error: " + e.getMessage()); }
            }
        };
        worker.execute();
    }

    private void searchEvidence() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { loadEvidence(); return; }
        tableModel.setRowCount(0);
        SwingWorker<List<Evidence>, Void> worker = new SwingWorker<>() {
            @Override protected List<Evidence> doInBackground() throws Exception {
                return evidenceService.searchEvidence(keyword);
            }
            @Override protected void done() {
                try { populateTable(get()); }
                catch (Exception e) { statusBar.setText("Error: " + e.getMessage()); }
            }
        };
        worker.execute();
    }

    private void populateTable(List<Evidence> evidenceList) {
        tableModel.setRowCount(0);
        for (Evidence e : evidenceList) {
            tableModel.addRow(new Object[]{
                    e.getEvidenceId(),
                    e.getEvidenceNumber(),
                    e.getCaseNumber(),
                    e.getEvidenceType(),
                    e.getDescription(),
                    e.getCollectedByName(),
                    e.getCurrentStatus(),
                    e.getCurrentHolderName(),
                    e.getRegisteredAt() != null ? e.getRegisteredAt().format(DT_FORMAT) : ""
            });
        }
        statusBar.setText("Showing " + evidenceList.size() + " evidence items.");
    }

    /**
     * Shows the Register Evidence dialog with file chooser and hash computation.
     */
    private void showRegisterDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Register New Evidence", true);
        dialog.setSize(580, 620);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(DashboardFrame.BG_PANEL);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(DashboardFrame.BG_PANEL);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Title
        JLabel titleLbl = new JLabel("Register Digital Evidence");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLbl.setForeground(DashboardFrame.ACCENT_BLUE);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Case selection
        JComboBox<String> caseCombo;
        List<Case> cases;
        try {
            cases = caseService.getCasesForUser(currentUser);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading cases: " + ex.getMessage());
            return;
        }
        String[] caseOptions = cases.stream()
                .map(c -> c.getCaseId() + " | " + c.getCaseNumber() + " - " + c.getTitle())
                .toArray(String[]::new);
        caseCombo = UIHelper.createComboBox(caseOptions);
        caseCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        caseCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Evidence type
        String[] types = {Evidence.TYPE_LAPTOP, Evidence.TYPE_MOBILE, Evidence.TYPE_USB,
                Evidence.TYPE_HARD_DISK, Evidence.TYPE_IMAGE, Evidence.TYPE_VIDEO,
                Evidence.TYPE_DOCUMENT, Evidence.TYPE_LOG_FILE, Evidence.TYPE_OTHER};
        JComboBox<String> typeCombo = UIHelper.createComboBox(types);
        typeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Description
        JTextArea descArea = new JTextArea(3, 30);
        descArea.setBackground(DashboardFrame.BG_DARK);
        descArea.setForeground(DashboardFrame.TEXT_WHITE);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setBorder(new LineBorder(new Color(60, 80, 120), 1));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Location
        JTextField locationField = UIHelper.createTextField("Collection location");
        locationField.setAlignmentX(Component.LEFT_ALIGNMENT);
        locationField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // File chooser
        final File[] selectedFile = {null};
        final String[] computedHash = {null};

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filePanel.setBackground(DashboardFrame.BG_PANEL);
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel fileLabel = new JLabel("No file selected");
        fileLabel.setForeground(DashboardFrame.TEXT_GRAY);
        fileLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        JLabel hashLabel = new JLabel("SHA-256: —");
        hashLabel.setForeground(DashboardFrame.ACCENT_TEAL);
        hashLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hashLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton chooseBtn = UIHelper.createButton("📁 Choose File", DashboardFrame.ACCENT_BLUE);
        chooseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Evidence File");
            int result = chooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = chooser.getSelectedFile();
                fileLabel.setText(selectedFile[0].getName() +
                        " (" + FileUtil.formatFileSize(selectedFile[0].length()) + ")");
                // Compute hash in background
                hashLabel.setText("Computing SHA-256...");
                SwingWorker<String, Void> hashWorker = new SwingWorker<>() {
                    @Override protected String doInBackground() throws Exception {
                        return HashUtil.computeHash(selectedFile[0].getAbsolutePath());
                    }
                    @Override protected void done() {
                        try {
                            computedHash[0] = get();
                            hashLabel.setText("SHA-256: " + computedHash[0].substring(0, 32) + "...");
                        } catch (Exception ex) {
                            hashLabel.setText("Hash error: " + ex.getMessage());
                        }
                    }
                };
                hashWorker.execute();
            }
        });

        filePanel.add(chooseBtn);
        filePanel.add(fileLabel);

        // Register button
        List<Case> finalCases = cases;
        JButton registerBtn = UIHelper.createButton("Register Evidence", DashboardFrame.SUCCESS_GREEN);
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> {
            int caseIdx = caseCombo.getSelectedIndex();
            if (caseIdx < 0 || finalCases.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please select a case.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (selectedFile[0] == null) {
                JOptionPane.showMessageDialog(dialog, "Please select an evidence file.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (locationField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Collection location is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Case selectedCase = finalCases.get(caseIdx);
            try {
                String evNumber = evidenceService.generateEvidenceNumber(selectedCase.getCaseNumber());

                Evidence evidence = new Evidence(
                        evNumber,
                        selectedCase.getCaseId(),
                        (String) typeCombo.getSelectedItem(),
                        descArea.getText().trim(),
                        currentUser.getUserId(),
                        locationField.getText().trim(),
                        LocalDateTime.now()
                );
                evidence.setCaseNumber(selectedCase.getCaseNumber());

                Evidence registered = evidenceService.registerEvidence(evidence, selectedFile[0].getAbsolutePath());

                JOptionPane.showMessageDialog(dialog,
                        "Evidence registered successfully!\n" +
                        "Evidence Number: " + registered.getEvidenceNumber() + "\n" +
                        "SHA-256 Hash: " + registered.getOriginalHash(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadEvidence();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error registering evidence: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(16));
        panel.add(UIHelper.createFieldLabel("Case *"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(caseCombo);
        panel.add(Box.createVerticalStrut(12));
        panel.add(UIHelper.createFieldLabel("Evidence Type *"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(typeCombo);
        panel.add(Box.createVerticalStrut(12));
        panel.add(UIHelper.createFieldLabel("Description"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(descScroll);
        panel.add(Box.createVerticalStrut(12));
        panel.add(UIHelper.createFieldLabel("Collection Location *"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(locationField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(UIHelper.createFieldLabel("Evidence File *"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(filePanel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(hashLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(registerBtn);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private void showEvidenceDetails() {
        int row = evidenceTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an evidence item.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int evidenceId = (Integer) tableModel.getValueAt(row, 0);
        try {
            Evidence e = evidenceService.getEvidenceById(evidenceId);
            evidenceService.logView(currentUser.getUserId(), evidenceId, "Viewed evidence details.");

            String details = String.format(
                    "Evidence ID:       %d\n" +
                    "Evidence Number:   %s\n" +
                    "Case:              %s\n" +
                    "Type:              %s\n" +
                    "Description:       %s\n" +
                    "Collected By:      %s\n" +
                    "Collection Date:   %s\n" +
                    "Location:          %s\n" +
                    "Status:            %s\n" +
                    "Current Holder:    %s\n\n" +
                    "File Name:         %s\n" +
                    "File Path:         %s\n" +
                    "File Size:         %s\n" +
                    "Original Hash:     %s\n" +
                    "Current Hash:      %s",
                    e.getEvidenceId(), e.getEvidenceNumber(), e.getCaseNumber(),
                    e.getEvidenceType(), e.getDescription(), e.getCollectedByName(),
                    e.getCollectionDate() != null ? e.getCollectionDate().format(DT_FORMAT) : "N/A",
                    e.getCollectionLocation(), e.getCurrentStatus(), e.getCurrentHolderName(),
                    e.getFileName(), e.getFilePath(),
                    FileUtil.formatFileSize(e.getFileSize()),
                    e.getOriginalHash(), e.getCurrentHash()
            );
            JTextArea ta = new JTextArea(details);
            ta.setEditable(false);
            ta.setBackground(DashboardFrame.BG_PANEL);
            ta.setForeground(DashboardFrame.TEXT_WHITE);
            ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane sp = new JScrollPane(ta);
            sp.setPreferredSize(new Dimension(600, 380));
            JOptionPane.showMessageDialog(this, sp, "Evidence Details: " + e.getEvidenceNumber(),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Custom renderer to color-code evidence rows by status.
     */
    private static class EvidenceStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String status = (String) table.getValueAt(row, 6);
                if (status == null) status = "";
                switch (status) {
                    case "VERIFIED":          c.setBackground(new Color(20, 50, 30)); break;
                    case "COMPROMISED":       c.setBackground(new Color(60, 15, 15)); break;
                    case "TRANSFERRED":       c.setBackground(new Color(20, 35, 60)); break;
                    case "UNDER_ANALYSIS":    c.setBackground(new Color(40, 30, 10)); break;
                    default:                  c.setBackground(row % 2 == 0 ? DashboardFrame.BG_DARK : DashboardFrame.TABLE_ALT);
                }
                c.setForeground(DashboardFrame.TEXT_WHITE);
            }
            return c;
        }
    }
}
