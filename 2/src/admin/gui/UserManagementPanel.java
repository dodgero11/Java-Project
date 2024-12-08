package gui;

import bll.User;
import bll.UserService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class UserManagementPanel extends JPanel {

    private JTable userTable;

    public UserManagementPanel() {
        setLayout(new BorderLayout());

        // Table to display users
        String[] columns = {"Username", "Full Name", "Address", "Birthdate", "Email", "Gender"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        userTable = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(userTable);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        userTable.setRowSorter(sorter);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add User");
        JButton removeButton = new JButton("Remove User");
        JButton sortByNameButton = new JButton("Sort by Full Name");
        JButton sortByUsernameButton = new JButton("Sort by Username");
        JButton sortByCreationDateButton = new JButton("Sort by Creation Date");

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(sortByNameButton);
        buttonPanel.add(sortByUsernameButton);
        buttonPanel.add(sortByCreationDateButton);

        // Add components to panel
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.NORTH);

        // Event listeners
        addButton.addActionListener(e -> addUser());
        removeButton.addActionListener(e -> removeUser());

        // Sort buttons
        sortByNameButton.addActionListener(e -> sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING))));
        sortByUsernameButton.addActionListener(e -> sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING))));
        sortByCreationDateButton.addActionListener(e -> sorter.setSortKeys(List.of(new RowSorter.SortKey(3, SortOrder.ASCENDING))));

        // Load user data into table
        refreshUserTable();
    }

    // Add a new user (you will integrate this with your service/database layer later)
    private void addUser() {
        // Create temporary variables to hold user data
        String username = "";
        String fullName = "";
        String address = "";
        String birthDate = "";
        String email = "";
        String gender = "";

        // Create a panel to hold input fields
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        // Create labels and text fields
        inputPanel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField();
        inputPanel.add(usernameField);

        inputPanel.add(new JLabel("Full Name:"));
        JTextField fullNameField = new JTextField();
        inputPanel.add(fullNameField);

        inputPanel.add(new JLabel("Address:"));
        JTextField addressField = new JTextField();
        inputPanel.add(addressField);

        inputPanel.add(new JLabel("Birthdate (YYYY-MM-DD):"));
        JTextField birthdateField = new JTextField();
        inputPanel.add(birthdateField);

        inputPanel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        inputPanel.add(emailField);

        inputPanel.add(new JLabel("Gender:"));
        JComboBox<String> genderComboBox = new JComboBox<>(new String[]{"Male", "Female"});
        inputPanel.add(genderComboBox);

        // Show the input dialog
        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Add New User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // If the user clicks OK, retrieve the data
        if (result == JOptionPane.OK_OPTION) {
            username = usernameField.getText();
            fullName = fullNameField.getText();
            address = addressField.getText();
            birthDate = birthdateField.getText();
            email = emailField.getText();
            gender = (String) genderComboBox.getSelectedItem();

            if (username.isEmpty() || fullName.isEmpty() || address.isEmpty() || birthDate.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                addUser();
                return;
            }

            // Validate birthdate format
            if (!birthDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Birthdate must be in YYYY-MM-DD format!", "Error", JOptionPane.ERROR_MESSAGE);
                addUser();
                return;
            }

            // Validate birthdate format
            if (!birthDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Birthdate must be in YYYY-MM-DD format!", "Error", JOptionPane.ERROR_MESSAGE);
                addUser();
                return;
            }

            // Validate email format
            if (!email.contains("@")) {
                JOptionPane.showMessageDialog(this, "Invalid email format! Must contain '@'", "Error", JOptionPane.ERROR_MESSAGE);
                addUser();
                return;
            }
        }

        // Add user to the database
        UserService userService = new UserService();
        if (userService.addUser(username, fullName, address, Date.valueOf(birthDate), gender, email)) {
            JOptionPane.showMessageDialog(this, "User added successfully!");
            refreshUserTable(); // Refresh UI table
        }
    }

    // Remove selected user
    private void removeUser() {
        String username = JOptionPane.showInputDialog(this, "Enter the username to delete:");
        UserService userService = new UserService();
        if (username != null && !username.trim().isEmpty()) {
            try {
                boolean success = userService.removeUser(username.trim());
                if (success) {
                    JOptionPane.showMessageDialog(this, "User deleted successfully.");
                    refreshUserTable(); // Refresh UI table
                } else {
                    JOptionPane.showMessageDialog(this, "User not found or could not be deleted.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
            }
        }
    }

    private void refreshUserTable() {
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.setRowCount(0); // Clear existing rows
        UserService userService = new UserService();

        try {
            List<User> users = userService.getAllUsers(); // Fetch users from DB
            for (User user : users) {
                model.addRow(new Object[]{
                    user.getUsername(),
                    user.getFullName(),
                    user.getAddress(),
                    user.getEmail(),
                    user.getGender(),
                    user.getBirthDate()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
