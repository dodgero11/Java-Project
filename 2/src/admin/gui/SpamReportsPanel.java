package gui;

import java.awt.BorderLayout;
import java.util.List;

import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import bll.SpamReport;
import bll.SpamService;

public class SpamReportsPanel extends JPanel {
    private JTable spamReportsTable;
    private JTextField filterTextField;

    public SpamReportsPanel() {
        setLayout(new BorderLayout());

        // Table columns
        String[] columns = {"Report ID", "Reported By", "Reported User", "Reason", "Created At"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        spamReportsTable = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(spamReportsTable);

        // Filter text field
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterTextField = new JTextField(20); // 20 columns wide
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterTextField);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        spamReportsTable.setRowSorter(sorter);

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

        // Load spam reports data
        refreshSpamReportsTable();
    }

    private void refreshSpamReportsTable() {
        DefaultTableModel model = (DefaultTableModel) spamReportsTable.getModel();
        model.setRowCount(0); // Clear the table
        SpamService spamService = new SpamService();
        try {
            List<SpamReport> spamReports = spamService.getSpamReports();
            for (SpamReport spamReport : spamReports) {
                model.addRow(new Object[]{
                    spamReport.getReportId(),
                    spamReport.getReportedBy(),
                    spamReport.getReportedUser(),
                    spamReport.getReason(),
                    spamReport.getCreatedAt()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}