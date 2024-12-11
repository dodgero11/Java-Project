package gui_admin;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class GroupManagementPanel extends JPanel {
    public GroupManagementPanel() {
        setLayout(new BorderLayout());

        // Table to display groups
        String[] columns = {"Group ID", "Group Name", "Created At"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable groupTable = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(groupTable);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton viewMembersButton = new JButton("View Members");
        JButton viewAdminsButton = new JButton("View Admins");
        buttonPanel.add(viewMembersButton);
        buttonPanel.add(viewAdminsButton);

        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}