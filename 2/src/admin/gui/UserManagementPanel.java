package gui;

import bll.UserService;
import java.awt.*;
import java.sql.Date;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UserManagementPanel extends JPanel {

    private JTable userTable;

    public UserManagementPanel() {
        setLayout(new BorderLayout());

        // Table to display users
        String[] columns = {"Username", "Full Name", "Address", "Birthdate", "Email", "Gender"};
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
        Date birthDate = Date.valueOf("1990-01-01");
        Date birthDate2 = Date.valueOf("1990-01-01");
        model.addRow(new Object[]{"johndoe", "John Doe", "123 Main St", birthDate, "johndoe@email.com", "Male"});
        model.addRow(new Object[]{"janedoe", "Jane Doe", "456 Oak St", birthDate2, "janedoe@email.com", "Female"});
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
            
            // Add user to the table
            DefaultTableModel model = (DefaultTableModel) userTable.getModel();
            model.addRow(new Object[]{username, fullName, address, birthDate, gender, email});
        }
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
