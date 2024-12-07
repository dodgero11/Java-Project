package gui;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UserManagementPanel extends JPanel {
    private JTable userTable;

    public UserManagementPanel() {
        setLayout(new BorderLayout());

        // Table to display users
        String[] columns = {"Username", "Full Name", "Address", "Email", "Gender"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        userTable = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(userTable);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add User");
        JButton removeButton = new JButton("Remove User");
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        // Add components to panel
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event listeners
        addButton.addActionListener(e -> addUser());
        removeButton.addActionListener(e -> removeUser());
        
        // Load user data into table
        loadUserData();
    }

    // Method to load user data into the table (dummy data for now)
    private void loadUserData() {
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.setRowCount(0);  // Clear existing rows

        // Example: Populate the table with dummy data
        model.addRow(new Object[]{"johndoe", "John Doe", "123 Main St", "johndoe@email.com", "Male"});
        model.addRow(new Object[]{"janedoe", "Jane Doe", "456 Oak St", "janedoe@email.com", "Female"});
    }

    // Add a new user (you will integrate this with your service/database layer later)
    private void addUser() {
        String username = JOptionPane.showInputDialog("Enter username:");
        String fullName = JOptionPane.showInputDialog("Enter full name:");
        String address = JOptionPane.showInputDialog("Enter address:");
        String email = JOptionPane.showInputDialog("Enter email:");
        String gender = JOptionPane.showInputDialog("Enter gender (Male/Female):");

        // Example: Add user to the table (In real implementation, add to the database)
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.addRow(new Object[]{username, fullName, address, email, gender});
    }

    // Remove selected user (you will integrate this with your service/database layer later)
    private void removeUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow != -1) {
            DefaultTableModel model = (DefaultTableModel) userTable.getModel();
            model.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to remove.");
        }
    }
}
