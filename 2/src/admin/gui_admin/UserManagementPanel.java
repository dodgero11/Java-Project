package gui_admin;

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
    private JTextField filterTextField;

    public UserManagementPanel() {
        setLayout(new BorderLayout());

        // Filter text field
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterTextField = new JTextField(20);
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterTextField);

        // Table to display users
        String[] columns = {"Username", "Full Name", "Address", "Birthdate", "Email", "Gender", "Creation Date", "Active"};
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
        JButton refreshUserPanelButton = new JButton("Refresh");

        buttonPanel.add(refreshUserPanelButton);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        // Add components to panel
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.NORTH);
        add(filterPanel, BorderLayout.SOUTH);

        // Event listeners
        addButton.addActionListener(e -> addUser());
        removeButton.addActionListener(e -> removeUser());
        refreshUserPanelButton.addActionListener(e -> refreshUserTable());

        // Mouse listener for row clicks
        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) { // Double-click for unnecessary mis-clicks
                    int row = userTable.getSelectedRow();
                    if (row != -1) {
                        String username = userTable.getValueAt(row, 0).toString(); // Fetch username
                        openUserDetails(username);
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

        // Load user data into table
        refreshUserTable();
    }

    private void addUser() {
        String username = "";
        String password = "";
        String fullName = "";
        String address = "";
        String birthDate = "";
        String email = "";
        String gender = "";

        // Create a panel to hold input fields
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 10, 10));

        // Create labels and text fields
        inputPanel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField();
        inputPanel.add(usernameField);

        inputPanel.add(new JLabel("Password:"));
        JTextField passwordField = new JTextField();
        inputPanel.add(passwordField);

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
            password = passwordField.getText();
            fullName = fullNameField.getText();
            address = addressField.getText();
            birthDate = birthdateField.getText();
            email = emailField.getText();
            gender = (String) genderComboBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || address.isEmpty() || birthDate.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                addUser();
                return;
            }

            // Validate Email format
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                JOptionPane.showMessageDialog(this, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
                addUser();
                return;
            }

            // Validate birthdate format
            if (!birthDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Birthdate must be in YYYY-MM-DD format!", "Error", JOptionPane.ERROR_MESSAGE);
                addUser();
                return;
            }
        }

        // Add user to the database
        UserService userService = new UserService();
        User newUser = new User(username, password, fullName, address, Date.valueOf(birthDate), gender, email);
        if (userService.addUser(newUser)) {
            JOptionPane.showMessageDialog(this, "User added successfully!");
            refreshUserTable();
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
                    refreshUserTable(); 
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
        model.setRowCount(0);
        UserService userService = new UserService();

        try {
            List<User> users = userService.getAllUsers(""); 
            for (User user : users) {
                model.addRow(new Object[]{
                    user.getUsername(),
                    user.getFullName(),
                    user.getAddress(),
                    user.getEmail(),
                    user.getBirthDate(),
                    user.getGender(),
                    user.getCreationDate(),
                    user.getIsActive()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Open user details
    private void openUserDetails(String username) {
        JFrame userDetailsFrame = new JFrame("User Details - " + username);
        userDetailsFrame.setSize(600, 400);
        userDetailsFrame.setLocationRelativeTo(this);
        userDetailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        UserService userService = new UserService();
        User user = null;
        try {
            user = userService.getUsersByUsername(username).get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        UserDetailsPanel userDetailsPanel = new UserDetailsPanel(user);
        userDetailsFrame.add(userDetailsPanel);

        userDetailsFrame.setVisible(true);
    }
}
