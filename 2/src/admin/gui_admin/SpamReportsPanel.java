package gui_admin;

import bll.SpamReport;
import bll.SpamReportService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class SpamReportsPanel extends JPanel {

    private JTable spamReportsTable;
    private JTextField filterTextField;

    public SpamReportsPanel() {
        setLayout(new BorderLayout());

        // Table columns
        String[] columns = { "Report ID", "Reported By", "Reported User", "Reason", "Status", "Created At" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        spamReportsTable = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(spamReportsTable);
        JPanel buttonPanel = new JPanel(new FlowLayout());

        // Buttons
        JButton refreshUserPanelButton = new JButton("Refresh");

        buttonPanel.add(refreshUserPanelButton);

        // Filter text field
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterTextField = new JTextField(20); // 20 columns wide
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterTextField);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        spamReportsTable.setRowSorter(sorter);

        // Refresh button listener
        refreshUserPanelButton.addActionListener(e -> refreshSpamReportsTable());

        // Mouse listener for row clicks
        spamReportsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) { // Double-click event
                    int row = spamReportsTable.getSelectedRow();
                    if (row != -1) {
                        String username = spamReportsTable.getValueAt(row, 0).toString(); // Fetch username
                        openSpamReportDetails(username);
                    }
                }
            }
        });

        // Filter text field listener
        filterTextField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                String text = filterTextField.getText();
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                String text = filterTextField.getText();
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                String text = filterTextField.getText();
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        // Add components to panel
        add(tableScrollPane, BorderLayout.CENTER);
        add(filterPanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.NORTH);

        // Load spam reports data
        refreshSpamReportsTable();
    }

    private void refreshSpamReportsTable() {
        DefaultTableModel model = (DefaultTableModel) spamReportsTable.getModel();
        model.setRowCount(0); // Clear the table
        SpamReportService spamReportService = new SpamReportService();
        try {
            List<SpamReport> spamReports = spamReportService.getSpamReports();
            for (SpamReport spamReport : spamReports) {
                model.addRow(new Object[] {
                        spamReport.getReportId(),
                        spamReport.getReportedBy(),
                        spamReport.getReportedUser(),
                        spamReport.getReason(),
                        spamReport.getStatus(),
                        spamReport.getCreatedAt()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openSpamReportDetails(String username) {
        JFrame spamReportDetailsFrame = new JFrame("Spam Report Details - " + username);
        spamReportDetailsFrame.setSize(600, 400);
        spamReportDetailsFrame.setLocationRelativeTo(this);
        spamReportDetailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        SpamReportService spamReportService = new SpamReportService();
        SpamReport spamReport = null;
        try {
            spamReport = spamReportService.getSpamReportById(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SpamReportDetailsPanel spamReportDetailsPanel = new SpamReportDetailsPanel(spamReport);
        spamReportDetailsFrame.add(spamReportDetailsPanel);

        spamReportDetailsFrame.setVisible(true);
    }
}
