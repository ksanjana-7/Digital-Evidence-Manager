package gui;

import model.Evidence;
import model.Verification;
import model.User;
import service.EvidenceService;
import service.VerificationService;
import util.FileUtil;
import exception.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * VerificationPanel - Panel for verifying evidence integrity using SHA-256.
 *
 * Demonstrates:
 *   - SHA-256 hash comparison
 *   - File selection via JFileChooser
 *   - DB trigger auto-logs verification (trg_after_verification_insert)
 */
public class VerificationPanel extends JPanel {

    private final User               currentUser;
    private final EvidenceService    evidenceService    = new EvidenceService();
    private final VerificationService verificationService = new VerificationService();

    private JTable            historyTable;
    private DefaultTableModel tableModel;
    private JLabel            statusBar;

    private static final DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public VerificationPanel(User currentUser) {
        this.currentUser = currentUser;
        initComponents();
        loadVerificationHistory();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(DashboardFrame.BG_DARK);
        add(createTopSection(), BorderLayout.NORTH);
        add(createHistoryTable(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createTopSection() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(DashboardFrame.BG_SIDEBAR);
        top.setBorder(new MatteBorder(0, 0, 1, 0, DashboardFrame.ACCENT_BLUE));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setBackground(DashboardFrame.BG_SIDEBAR);

        JButton verifyBtn = UIHelper.createButton("✅ Verify Evidence", DashboardFrame.SUCCESS_GREEN);
        verifyBtn.addActionListener(e -> showVerifyDialog());

        JButton refreshBtn = UIHelper.createButton("↻ Refresh History", DashboardFrame.TEXT_GRAY);
        refreshBtn.addActionListener(e -> loadVerificationHistory());

        JButton historyForEvidenceBtn = UIHelper.createButton("📋 History for Evidence", DashboardFrame.ACCENT_TEAL);
        historyForEvidenceBtn.addActionListener(e -> showHistoryForEvidence());

        toolbar.add(verifyBtn);
        toolbar.add(refreshBtn);
        toolbar.add(historyForEvidenceBtn);
        top.add(toolbar, BorderLayout.WEST);

        return top;
    }

    private JScrollPane createHistoryTable() {
        String[] columns = {"Ver. ID", "Evidence No.", "Verified By",
                            "Result", "Verification Date", "Original Hash (first 20)", "Current Hash (first 20)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        historyTable = new JTable(tableModel);
        UIHelper.styleTable(historyTable);

        // Color rows by result
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    String result = (String) table.getValueAt(row, 3);
                    if ("VERIFIED".equals(result)) c.setBackground(new Color(20, 50, 30));
                    else if ("TAMPERED".equals(result)) c.setBackground(new Color(60, 15, 15));
                    else c.setBackground(row % 2 == 0 ? DashboardFrame.BG_DARK : DashboardFrame.TABLE_ALT);
                    c.setForeground(DashboardFrame.TEXT_WHITE);
                }
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(historyTable);
        sp.getViewport().setBackground(DashboardFrame.BG_DARK);
        return sp;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bar.setBackground(DashboardFrame.BG_SIDEBAR);
        statusBar = new JLabel("Loading verification history...");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.setForeground(DashboardFrame.TEXT_GRAY);
        bar.add(statusBar);
        return bar;
    }

    private void loadVerificationHistory() {
        tableModel.setRowCount(0);
        SwingWorker<List<Verification>, Void> worker = new SwingWorker<>() {
            @Override protected List<Verification> doInBackground() throws Exception {
                return verificationService.getAllVerifications();
            }
            @Override protected void done() {
                try {
                    List<Verification> verifs = get();
                    for (Verification v : verifs) {
                        String origHash = v.getOriginalHash() != null ?
                                v.getOriginalHash().substring(0, Math.min(20, v.getOriginalHash().length())) + "..." : "N/A";
                        String currHash = v.getCurrentHash() != null ?
                                v.getCurrentHash().substring(0, Math.min(20, v.getCurrentHash().length())) + "..." : "N/A";
                        tableModel.addRow(new Object[]{
                                v.getVerificationId(),
                                v.getEvidenceNumber(),
                                v.getVerifiedByName(),
                                v.getResult(),
                                v.getVerificationDate() != null ? v.getVerificationDate().format(DT_FORMAT) : "",
                                origHash,
                                currHash
                        });
                    }
                    statusBar.setText("Showing " + verifs.size() + " verification records.");
                } catch (Exception e) {
                    statusBar.setText("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Shows the verification dialog.
     * User selects an evidence item and provides the file to re-hash and compare.
     */
    private void showVerifyDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Verify Evidence Integrity", true);
        dialog.setSize(560, 480);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(DashboardFrame.BG_PANEL);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(DashboardFrame.BG_PANEL);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLbl = new JLabel("Evidence Integrity Verification");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLbl.setForeground(DashboardFrame.SUCCESS_GREEN);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoLbl = new JLabel("SHA-256 hash of the file will be compared to the stored original hash.");
        infoLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        infoLbl.setForeground(DashboardFrame.TEXT_GRAY);
        infoLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Evidence ID input
        JTextField evidenceIdField = UIHelper.createTextField("Enter Evidence ID (numeric)");
        evidenceIdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        evidenceIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton lookupBtn = UIHelper.createButton("Lookup", DashboardFrame.ACCENT_BLUE);
        JPanel lookupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lookupPanel.setBackground(DashboardFrame.BG_PANEL);
        lookupPanel.add(evidenceIdField);
        lookupPanel.add(Box.createHorizontalStrut(8));
        lookupPanel.add(lookupBtn);
        lookupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel evidenceInfoLabel = new JLabel("—");
        evidenceInfoLabel.setForeground(DashboardFrame.ACCENT_TEAL);
        evidenceInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        evidenceInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel storedHashLabel = new JLabel("Stored Hash: —");
        storedHashLabel.setForeground(DashboardFrame.TEXT_GRAY);
        storedHashLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        storedHashLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final Evidence[] selectedEvidence = {null};

        lookupBtn.addActionListener(e -> {
            try {
                int evId = Integer.parseInt(evidenceIdField.getText().trim());
                Evidence ev = evidenceService.getEvidenceById(evId);
                selectedEvidence[0] = ev;
                evidenceInfoLabel.setText(ev.getEvidenceNumber() + " | " +
                        ev.getEvidenceType() + " | Status: " + ev.getCurrentStatus());
                storedHashLabel.setText("Stored Hash: " +
                        (ev.getOriginalHash() != null ? ev.getOriginalHash() : "N/A"));
            } catch (NumberFormatException ex) {
                evidenceInfoLabel.setText("Invalid ID.");
                evidenceInfoLabel.setForeground(DashboardFrame.ERROR_RED);
            } catch (Exception ex) {
                evidenceInfoLabel.setText("Not found: " + ex.getMessage());
                evidenceInfoLabel.setForeground(DashboardFrame.ERROR_RED);
            }
        });

        // File chooser
        final File[] fileToVerify = {null};
        JLabel fileLabel = new JLabel("No file selected");
        fileLabel.setForeground(DashboardFrame.TEXT_GRAY);
        fileLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        JButton chooseBtn = UIHelper.createButton("📁 Select File to Verify", DashboardFrame.ACCENT_BLUE);
        chooseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Current Evidence File");
            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                fileToVerify[0] = chooser.getSelectedFile();
                fileLabel.setText(fileToVerify[0].getName() +
                        " (" + FileUtil.formatFileSize(fileToVerify[0].length()) + ")");
            }
        });

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filePanel.setBackground(DashboardFrame.BG_PANEL);
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filePanel.add(chooseBtn);
        filePanel.add(fileLabel);

        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton verifyBtn = UIHelper.createButton("▶ Run Verification", DashboardFrame.SUCCESS_GREEN);
        verifyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        verifyBtn.addActionListener(e -> {
            if (selectedEvidence[0] == null) {
                JOptionPane.showMessageDialog(dialog, "Please lookup an evidence ID first.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (fileToVerify[0] == null) {
                JOptionPane.showMessageDialog(dialog, "Please select the current evidence file.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            verifyBtn.setEnabled(false);
            resultLabel.setText("Verifying...");
            resultLabel.setForeground(DashboardFrame.TEXT_GRAY);

            int evId = selectedEvidence[0].getEvidenceId();
            SwingWorker<Verification, Void> worker = new SwingWorker<>() {
                @Override protected Verification doInBackground() throws Exception {
                    return evidenceService.verifyEvidence(evId,
                            fileToVerify[0].getAbsolutePath(),
                            currentUser.getUserId());
                }
                @Override protected void done() {
                    verifyBtn.setEnabled(true);
                    try {
                        Verification v = get();
                        resultLabel.setText("✅ VERIFIED - File integrity confirmed.");
                        resultLabel.setForeground(DashboardFrame.SUCCESS_GREEN);
                        JOptionPane.showMessageDialog(dialog,
                                "✅ VERIFICATION SUCCESSFUL\n\n" +
                                "Evidence: " + selectedEvidence[0].getEvidenceNumber() + "\n" +
                                "Result: VERIFIED\n" +
                                "Hash matches original. File has not been tampered with.\n\n" +
                                "Original Hash: " + v.getOriginalHash() + "\n" +
                                "Current Hash:  " + v.getCurrentHash(),
                                "Verified", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadVerificationHistory();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        if (cause instanceof HashMismatchException) {
                            HashMismatchException hme = (HashMismatchException) cause;
                            resultLabel.setText("⚠ TAMPERING DETECTED!");
                            resultLabel.setForeground(DashboardFrame.ERROR_RED);
                            JOptionPane.showMessageDialog(dialog,
                                    "⚠ TAMPERING DETECTED!\n\n" +
                                    "Evidence: " + selectedEvidence[0].getEvidenceNumber() + "\n" +
                                    "The file has been MODIFIED since it was registered.\n\n" +
                                    "Original Hash:\n" + hme.getOriginalHash() + "\n\n" +
                                    "Current Hash:\n" + hme.getCurrentHash(),
                                    "TAMPERING DETECTED", JOptionPane.ERROR_MESSAGE);
                        } else {
                            resultLabel.setText("Error: " + cause.getMessage());
                            resultLabel.setForeground(DashboardFrame.ERROR_RED);
                            JOptionPane.showMessageDialog(dialog, "Verification error: " + cause.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        loadVerificationHistory();
                    }
                }
            };
            worker.execute();
        });

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(infoLbl);
        panel.add(Box.createVerticalStrut(20));
        panel.add(UIHelper.createFieldLabel("Evidence ID *"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(lookupPanel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(evidenceInfoLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(storedHashLabel);
        panel.add(Box.createVerticalStrut(16));
        panel.add(UIHelper.createFieldLabel("Current Evidence File *"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(filePanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(verifyBtn);
        panel.add(Box.createVerticalStrut(12));
        panel.add(resultLabel);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showHistoryForEvidence() {
        String input = JOptionPane.showInputDialog(this,
                "Enter Evidence ID to view verification history:", "Verification History",
                JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;
        try {
            int evId = Integer.parseInt(input.trim());
            List<Verification> history = verificationService.getVerificationHistory(evId);
            if (history.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No verification records found for Evidence ID: " + evId);
                return;
            }
            tableModel.setRowCount(0);
            for (Verification v : history) {
                String origHash = v.getOriginalHash() != null ?
                        v.getOriginalHash().substring(0, Math.min(20, v.getOriginalHash().length())) + "..." : "N/A";
                String currHash = v.getCurrentHash() != null ?
                        v.getCurrentHash().substring(0, Math.min(20, v.getCurrentHash().length())) + "..." : "N/A";
                tableModel.addRow(new Object[]{
                        v.getVerificationId(), v.getEvidenceNumber(), v.getVerifiedByName(),
                        v.getResult(),
                        v.getVerificationDate() != null ? v.getVerificationDate().format(DT_FORMAT) : "",
                        origHash, currHash
                });
            }
            statusBar.setText("Verification history for Evidence ID " + evId + ": " + history.size() + " records.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid ID.", "Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
