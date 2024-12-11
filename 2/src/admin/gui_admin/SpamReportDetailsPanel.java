package gui_admin;

import bll.SpamReport;
import bll.SpamReportService;
import bll.UserService;
import java.awt.*;
import javax.swing.*;

public class SpamReportDetailsPanel extends JPanel {

    private JLabel reporterLabel, reportedUserLabel, reportReasonLabel, reportStatusLabel;
    private JButton deactivateUserButton, dismissReportButton;
    private SpamReport spamReport;

    public SpamReportDetailsPanel(SpamReport spamReport) {
        this.spamReport = spamReport;

        setLayout(new BorderLayout(0, 0));

        // Display report information
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 2, 4));
        infoPanel.add(new JLabel("Reporter:"));
        reporterLabel = new JLabel(spamReport.getReportedBy());
        infoPanel.add(reporterLabel);

        infoPanel.add(new JLabel("Reported User:"));
        reportedUserLabel = new JLabel(spamReport.getReportedUser());
        infoPanel.add(reportedUserLabel);

        infoPanel.add(new JLabel("Reason for Report:"));
        reportReasonLabel = new JLabel(spamReport.getReason());
        infoPanel.add(reportReasonLabel);

        infoPanel.add(new JLabel("Reason Status:"));
        reportReasonLabel = new JLabel(spamReport.getReason());
        infoPanel.add(reportReasonLabel);

        infoPanel.add(new JLabel("Report Time:"));
        reportStatusLabel = new JLabel(spamReport.getCreatedAt().toString());
        infoPanel.add(reportStatusLabel);

        add(infoPanel, BorderLayout.NORTH);

        // Action buttons for resolving or dismissing the report
        JPanel buttonPanel = new JPanel(new FlowLayout());
        deactivateUserButton = new JButton("Deactivate User");
        dismissReportButton = new JButton("Dismiss Report");
        buttonPanel.add(deactivateUserButton);
        buttonPanel.add(dismissReportButton);

        add(buttonPanel, BorderLayout.CENTER);

        // Event Listeners
        deactivateUserButton.addActionListener(e -> deactivateUserReport());
        dismissReportButton.addActionListener(e -> dismissReport());
    }

    // Handle resolving the report
    private void deactivateUserReport() {
        // Logic to resolve the report, e.g., flag the user, delete message, etc.
        UserService userService = new UserService();
        try {
            userService.deactivateUserService(userService.getUsersByUsername(spamReport.getReportedUser()).get(0));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        JOptionPane.showMessageDialog(this, "Account deactivated!");
        dismissReport();
    }

    // Handle dismissing the report
    private void dismissReport() {
        // Logic to dismiss the report
        SpamReportService spamReportService = new SpamReportService();
        try {
            spamReportService.dismissReport(spamReport.getReportId()); // Dismiss report in DB
        } catch (Exception e) {
            e.printStackTrace();
        }

        JOptionPane.showMessageDialog(this, "Report dismissed!");
        reportStatusLabel.setText("Dismissed");
    }
}
