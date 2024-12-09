package gui;

import bll.User;
import bll.UserService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Date;
import javax.swing.*;

public class UserDetailsPanel extends JPanel {
    private JLabel usernameLabel, fullNameLabel, addressLabel, birthDateLabel, emailLabel, genderLabel, statusLabel, passwordLabel;
    private JButton editInfoButton, deactivateButton, activateButton;

    private User user;
    private UserService userService;

    public UserDetailsPanel(User user) {
        this.user = user;
        this.userService = new UserService();

        setLayout(new BorderLayout(1, 1));

        // Display user information
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 2, 4));
        infoPanel.add(new JLabel("Username:"));
        usernameLabel = new JLabel(user.getUsername());
        infoPanel.add(usernameLabel);

        infoPanel.add(new JLabel("Password:"));
        passwordLabel = new JLabel(user.getPassword());
        infoPanel.add(passwordLabel);

        infoPanel.add(new JLabel("Full Name:"));
        fullNameLabel = new JLabel(user.getFullName());
        infoPanel.add(fullNameLabel);

        infoPanel.add(new JLabel("Address:"));
        addressLabel = new JLabel(user.getAddress());
        infoPanel.add(addressLabel);

        infoPanel.add(new JLabel("Birth Date:"));
        birthDateLabel = new JLabel(user.getBirthDate().toString());
        infoPanel.add(birthDateLabel);

        infoPanel.add(new JLabel("Email:"));
        emailLabel = new JLabel(user.getEmail());
        infoPanel.add(emailLabel);

        infoPanel.add(new JLabel("Gender:"));
        genderLabel = new JLabel(user.getGender());
        infoPanel.add(genderLabel);

        infoPanel.add(new JLabel("Status:"));
        statusLabel = new JLabel(user.getIsActive());
        infoPanel.add(statusLabel);

        add(infoPanel, BorderLayout.NORTH);

        // Add action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        editInfoButton = new JButton("Edit Info");
        deactivateButton = new JButton("Deactivate Account");
        activateButton = new JButton("Activate Account");
        buttonPanel.add(editInfoButton);

        if (user.getIsActive().equals("Active")) {
            buttonPanel.add(deactivateButton);
        }

        if (user.getIsActive().equals("Deactivated")) {
            buttonPanel.add(activateButton);
        }

        add(buttonPanel, BorderLayout.AFTER_LAST_LINE);

        // Event Listeners
        editInfoButton.addActionListener(e -> editUserInfo());
        deactivateButton.addActionListener(e -> deactivateAccount());
        activateButton.addActionListener(e -> activateAccount());

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Friends", new FriendsPanel(user.getUsername()));
        // tabbedPane.addTab("Login History", new LoginHistoryPanel(user.getUsername()));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void editUserInfo() {
        // Create a dialog for editing user information
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        inputPanel.add(new JLabel("Full Name:"));
        JTextField fullNameField = new JTextField(user.getFullName());
        inputPanel.add(fullNameField);

        inputPanel.add(new JLabel("Password:"));
        JTextField passwordField = new JTextField(user.getPassword());
        inputPanel.add(passwordField);

        inputPanel.add(new JLabel("Address:"));
        JTextField addressField = new JTextField(user.getAddress());
        inputPanel.add(addressField);

        inputPanel.add(new JLabel("Birth Date (YYYY-MM-DD):"));
        JTextField birthDateField = new JTextField(user.getBirthDate().toString());
        inputPanel.add(birthDateField);

        inputPanel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField(user.getEmail());
        inputPanel.add(emailField);

        inputPanel.add(new JLabel("Gender:"));
        JComboBox<String> genderComboBox = new JComboBox<>(new String[]{"Male", "Female"});
        genderComboBox.setSelectedItem(user.getGender());
        inputPanel.add(genderComboBox);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Edit User Info", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String fullName = fullNameField.getText();
            String password = passwordField.getText();
            String address = addressField.getText();
            String birthDate = birthDateField.getText();
            String email = emailField.getText();
            String gender = (String) genderComboBox.getSelectedItem();

            if (fullName.isEmpty() || password.isEmpty() || address.isEmpty() || birthDate.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                editUserInfo();
                return;
            }

            // Validate birthdate format
            if (!birthDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Birthdate must be in YYYY-MM-DD format!", "Error", JOptionPane.ERROR_MESSAGE);
                editUserInfo();
                return;
            }

            // Validate email format
            if (!email.contains("@")) {
                JOptionPane.showMessageDialog(this, "Invalid email format! Must contain '@'", "Error", JOptionPane.ERROR_MESSAGE);
                editUserInfo();
                return;
            }

            user.setFullName(fullName);
            user.setPassword(password);
            user.setAddress(address);
            user.setBirthDate(Date.valueOf(birthDate));
            user.setEmail(email);
            user.setGender(gender);

            try {
                if (userService.updateUser(user)) {
                    JOptionPane.showMessageDialog(this, "User information updated successfully!");
                    refreshUserInfo(); // Reload updated data
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update user information.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deactivateAccount() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to deactivate this account?", "Deactivate Account", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                user.setIsActive("Deactivated");
                if (userService.updateUser(user)) {
                    JOptionPane.showMessageDialog(this, "Account deactivated successfully!");
                    statusLabel.setText("Deactivated");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to deactivate account.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void activateAccount() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to activate this account?", "Activate Account", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                user.setIsActive("Active");
                if (userService.updateUser(user)) {
                    JOptionPane.showMessageDialog(this, "Account activated successfully!");
                    statusLabel.setText("Active");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to activate account.", "Error", JOptionPane.ERROR_MESSAGE);
                }    
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshUserInfo() {
        // Refresh the panel with updated user data
        try {
            this.user = userService.getUsersByUsername(user.getUsername()).get(0);
            passwordLabel.setText(user.getPassword());
            fullNameLabel.setText(user.getFullName());
            addressLabel.setText(user.getAddress());
            birthDateLabel.setText(user.getBirthDate().toString());
            emailLabel.setText(user.getEmail());
            genderLabel.setText(user.getGender());
            statusLabel.setText(user.getIsActive());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred while refreshing user data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

