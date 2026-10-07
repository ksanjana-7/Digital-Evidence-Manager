package gui;

import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ReportPanel extends JPanel {

    public ReportPanel(User currentUser) {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(24, 24, 32));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Reports & Statistics");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Evidence management and chain-of-custody reports"
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(Color.LIGHT_GRAY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        JPanel reportPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        reportPanel.setOpaque(false);

        reportPanel.add(createReportCard(
                "Evidence Report",
                "View registered evidence records."
        ));

        reportPanel.add(createReportCard(
                "Transfer Report",
                "View evidence chain-of-custody transfers."
        ));

        reportPanel.add(createReportCard(
                "Verification Report",
                "View evidence hash verification results."
        ));

        reportPanel.add(createReportCard(
                "Access Log Report",
                "View user access and activity history."
        ));

        add(header, BorderLayout.NORTH);
        add(reportPanel, BorderLayout.CENTER);
    }

    private JPanel createReportCard(String title, String description) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(new Color(38, 38, 48));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 85)),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel descriptionLabel = new JLabel(
                "<html>" + description + "</html>"
        );
        descriptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descriptionLabel.setForeground(Color.LIGHT_GRAY);

        JButton viewButton = new JButton("View Report");
        viewButton.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        title + " will be generated from the database.",
                        "Report",
                        JOptionPane.INFORMATION_MESSAGE
                )
        );

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descriptionLabel, BorderLayout.CENTER);
        card.add(viewButton, BorderLayout.SOUTH);

        return card;
    }
}
