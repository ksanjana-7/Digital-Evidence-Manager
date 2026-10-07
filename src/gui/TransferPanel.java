package gui;

import model.Evidence;
import model.EvidenceTransfer;
import model.User;
import service.EvidenceService;
import service.TransferService;
import dao.UserDAO;
import exception.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * TransferPanel - Panel for evidence chain-of-custody transfers.
 *
 * Demonstrates:
 *   - JDBC transactions (commit/rollback in TransferService)
 *   - DB triggers (trg_after_transfer_insert auto-logs)
 *   - Chain of custody display using JTable
 */
public class TransferPanel extends JPanel {

    private final User            currentUser;
    private final TransferService transferService = new TransferService();
    private final EvidenceService evidenceService = new EvidenceService();
    private final UserDAO         userDAO         = new UserDAO();

    private JTable            transferTable;
    private DefaultTableModel tableModel;
    private JLabel            statusBar;

    private static final DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TransferPanel(User currentUser) {
        this.currentUser = currentUser;
        initComponents();
        loadTransfers();
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

        JButton refreshBtn = UIHelper.createButton("↻ Refresh", DashboardFrame.TEXT_GRAY);
        refreshBtn.addActionListener(e -> loadTransfers());

        JButton chainBtn = UIHelper.createButton("🔗 Chain of Custody", DashboardFrame.ACCENT_TEAL);
        chainBtn.addActionListener(e -> showChainOfCustody());

        JButton transferBtn = UIHelper.createButton("🔄 Transfer Evidence", DashboardFrame.WARNING_ORANGE);
        transferBtn.addActionListener(e -> showTransferDialog());

        toolbar.add(refreshBtn);
        toolbar.add(chainBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(transferBtn);

        return toolbar;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"Transfer ID", "Evidence No.", "From", "To",
                            "Date & Time", "Reason", "Location", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        transferTable = new JTable(tableModel);
        UIHelper.styleTable(transferTable);

        JScrollPane scrollPane = new JScrollPane(transferTable);
        scrollPane.getViewport().setBackground(DashboardFrame.BG_DARK);
        return scrollPane;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bar.setBackground(DashboardFrame.BG_SIDEBAR);
        statusBar = new JLabel("Loading transfers...");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.setForeground(DashboardFrame.TEXT_GRAY);
        bar.add(statusBar);
        return bar;
    }

    private void loadTransfers() {
        tableModel.setRowCount(0);
        SwingWorker<List<EvidenceTransfer>, Void> worker = new SwingWorker<>() {
            @Override protected List<EvidenceTransfer> doInBackground() throws Exception {
                if ("ADMIN".equals(currentUser.getRole())) {
                    return transferService.getAllTransfers();
                } else {
                    return transferService.getTransfersByUser(currentUser.getUserId());
                }
            }
            @Override protected void done() {
                try {
                    List<EvidenceTransfer> transfers = get();
                    for (EvidenceTransfer t : transfers) {
                        tableModel.addRow(new Object[]{
                                t.getTransferId(),
                                t.getEvidenceNumber(),
                                t.getFromUserName(),
                                t.getToUserName(),
                                t.getTransferDate() != null ? t.getTransferDate().format(DT_FORMAT) : "",
                                t.getReason(),
                                t.getLocation(),
                                t.getStatus()
                        });
                    }
                    statusBar.setText("Showing " + transfers.size() + " transfers.");
                } catch (Exception e) {
                    statusBar.setText("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Shows a dialog to perform an evidence transfer.
     * The actual transfer uses a JDBC transaction in TransferService.
     */
    private void showTransferDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Transfer Evidence", true);
        dialog.setSize(550, 500);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(DashboardFrame.BG_PANEL);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(DashboardFrame.BG_PANEL);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLbl = new JLabel("Transfer Evidence Custody");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLbl.setForeground(DashboardFrame.WARNING_ORANGE);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel warningLbl = new JLabel("⚠ This action is recorded in the chain of custody.");
        warningLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        warningLbl.setForeground(DashboardFrame.TEXT_GRAY);
        warningLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Evidence selection - load evidence held by current user
        List<Evidence> heldEvidence;
        try {
            heldEvidence = evidenceService.getEvidenceByHolder(currentUser.getUserId());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading evidence: " + ex.getMessage());
            return;
        }

        String[] evOptions = heldEvidence.stream()
                .map(e -> e.getEvidenceId() + " | " + e.getEvidenceNumber() + " - " + e.getEvidenceType())
                .toArray(String[]::new);

        if (evOptions.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "You are not currently holding any evidence to transfer.\n" +
                    "Evidence must be in your custody to transfer it.",
                    "No Evidence", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<String> evidenceCombo = UIHelper.createComboBox(evOptions);
        evidenceCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        evidenceCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Receiver selection
        List<User> allUsers;
        try {
            allUsers = userDAO.getActiveUsers();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + ex.getMessage());
            return;
        }

        List<User> receivers = allUsers.stream()
                .filter(u -> u.getUserId() != currentUser.getUserId())
                .collect(java.util.stream.Collectors.toList());

        String[] userOptions = receivers.stream()
                .map(u -> u.getUserId() + " | " + u.getFullName() + " (" + u.getRole() + ")")
                .toArray(String[]::new);

        JComboBox<String> receiverCombo = UIHelper.createComboBox(userOptions);
        receiverCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        receiverCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Reason
        JTextArea reasonArea = new JTextArea(3, 30);
        reasonArea.setBackground(DashboardFrame.BG_DARK);
        reasonArea.setForeground(DashboardFrame.TEXT_WHITE);
        reasonArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reasonArea.setBorder(new LineBorder(new Color(60, 80, 120), 1));
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Location
        JTextField locationField = UIHelper.createTextField("Transfer location");
        locationField.setAlignmentX(Component.LEFT_ALIGNMENT);
        locationField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Transfer Button
        List<Evidence> finalEvidence = heldEvidence;
        List<User>     finalReceivers = receivers;

        JButton transferBtn = UIHelper.createButton("Confirm Transfer", DashboardFrame.WARNING_ORANGE);
        transferBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        transferBtn.addActionListener(e -> {
            int evIdx  = evidenceCombo.getSelectedIndex();
            int usrIdx = receiverCombo.getSelectedIndex();

            if (evIdx < 0 || usrIdx < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select evidence and receiver.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String reason = reasonArea.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Transfer reason is required.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Evidence  selectedEvidence = finalEvidence.get(evIdx);
            User      receiver         = finalReceivers.get(usrIdx);
            String    location         = locationField.getText().trim();

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Confirm transfer of:\n" +
                    "Evidence: " + selectedEvidence.getEvidenceNumber() + "\n" +
                    "To:       " + receiver.getFullName() + " (" + receiver.getRole() + ")\n" +
                    "Reason:   " + reason,
                    "Confirm Transfer", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) return;

            // Execute transfer in background thread
            transferBtn.setEnabled(false);
            SwingWorker<EvidenceTransfer, Void> worker = new SwingWorker<>() {
                @Override protected EvidenceTransfer doInBackground() throws Exception {
                    return transferService.transferEvidence(
                            selectedEvidence.getEvidenceId(),
                            currentUser.getUserId(),
                            receiver.getUserId(),
                            reason, location
                    );
                }
                @Override protected void done() {
                    transferBtn.setEnabled(true);
                    try {
                        EvidenceTransfer t = get();
                        JOptionPane.showMessageDialog(dialog,
                                "Transfer completed!\n" +
                                "Transfer ID: " + t.getTransferId() + "\n" +
                                "Evidence: " + selectedEvidence.getEvidenceNumber() + "\n" +
                                "Now held by: " + receiver.getFullName() + "\n\n" +
                                "The audit log has been updated automatically by the database trigger.",
                                "Transfer Complete", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadTransfers();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(dialog,
                                "Transfer failed (transaction rolled back):\n" + cause.getMessage(),
                                "Transfer Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        });

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(warningLbl);
        panel.add(Box.createVerticalStrut(16));
        panel.add(UIHelper.createFieldLabel("Select Evidence (held by you) *"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(evidenceCombo);
        panel.add(Box.createVerticalStrut(12));
        panel.add(UIHelper.createFieldLabel("Transfer To (Receiver) *"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(receiverCombo);
        panel.add(Box.createVerticalStrut(12));
        panel.add(UIHelper.createFieldLabel("Reason for Transfer *"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(reasonScroll);
        panel.add(Box.createVerticalStrut(12));
        panel.add(UIHelper.createFieldLabel("Transfer Location"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(locationField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(transferBtn);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * Shows the complete chain of custody for a selected evidence item.
     */
    private void showChainOfCustody() {
        // Ask user to enter evidence ID or select from list
        String input = JOptionPane.showInputDialog(this,
                "Enter Evidence ID to view chain of custody:", "Chain of Custody",
                JOptionPane.QUESTION_MESSAGE);

        if (input == null || input.trim().isEmpty()) return;

        try {
            int evidenceId = Integer.parseInt(input.trim());
            List<EvidenceTransfer> chain = transferService.getChainOfCustody(evidenceId);

            if (chain.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No transfers found for Evidence ID: " + evidenceId,
                        "Chain of Custody", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Build chain display
            StringBuilder sb = new StringBuilder();
            sb.append("Chain of Custody - Evidence ID: ").append(evidenceId).append("\n");
            sb.append("─".repeat(70)).append("\n\n");

            Evidence ev = evidenceService.getEvidenceById(evidenceId);
            sb.append("Evidence: ").append(ev.getEvidenceNumber())
              .append(" | Type: ").append(ev.getEvidenceType()).append("\n");
            sb.append("Collected By: ").append(ev.getCollectedByName()).append("\n");
            sb.append("Collection Date: ").append(ev.getCollectionDate() != null ?
                    ev.getCollectionDate().format(DT_FORMAT) : "N/A").append("\n\n");

            sb.append("TRANSFER HISTORY:\n");
            sb.append("─".repeat(70)).append("\n");

            for (int i = 0; i < chain.size(); i++) {
                EvidenceTransfer t = chain.get(i);
                sb.append(String.format("\nStep %d: [%s]\n", i+1,
                        t.getTransferDate() != null ? t.getTransferDate().format(DT_FORMAT) : "N/A"));
                sb.append("  From: ").append(t.getFromUserName()).append("\n");
                sb.append("    ↓\n");
                sb.append("  To:   ").append(t.getToUserName()).append("\n");
                sb.append("  Reason: ").append(t.getReason()).append("\n");
                if (t.getLocation() != null && !t.getLocation().isEmpty())
                    sb.append("  Location: ").append(t.getLocation()).append("\n");
            }

            sb.append("\n").append("─".repeat(70)).append("\n");
            sb.append("Current Holder: ").append(ev.getCurrentHolderName()).append("\n");
            sb.append("Current Status: ").append(ev.getCurrentStatus());

            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            ta.setBackground(DashboardFrame.BG_PANEL);
            ta.setForeground(DashboardFrame.TEXT_WHITE);
            ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
            JScrollPane sp = new JScrollPane(ta);
            sp.setPreferredSize(new Dimension(600, 450));
            JOptionPane.showMessageDialog(this, sp,
                    "Chain of Custody - Evidence " + ev.getEvidenceNumber(),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric Evidence ID.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
