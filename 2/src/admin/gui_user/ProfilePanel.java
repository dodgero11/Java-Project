package gui_user;

import bll.User;
import bll.UserService;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class ProfilePanel extends JPanel {

    private User currentUser;
    private UserService userService;
    private JTextField fullNameField, addressField, emailField, birthDateField;
    private JLabel usernameLabel;
    private JComboBox<String> genderField;
    private JButton updateInfoButton, changePasswordButton;

    public ProfilePanel(String username) {
        setLayout(new GridLayout(0, 2, 10, 10));

        // UserService to fetch user information
        UserService userService = new UserService();
        try {
            currentUser = userService.getUsersByUsername(username).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Display user details
        add(new JLabel("Username:"));
        usernameLabel = new JLabel(currentUser.getUsername());
        add(usernameLabel);

        add(new JLabel("Full Name:"));
        fullNameField = new JTextField(currentUser.getFullName());
        add(fullNameField);

        add(new JLabel("Address:"));
        addressField = new JTextField(currentUser.getAddress());
        add(addressField);

        add(new JLabel("Email:"));
        emailField = new JTextField(currentUser.getEmail());
        add(emailField);

        add(new JLabel("Gender:"));
        genderField = new JComboBox<>(new String[]{"Male", "Female"});
        genderField.setSelectedItem(currentUser.getGender());
        add(genderField);

        add(new JLabel("Birth Date:"));
        birthDateField = new JTextField(currentUser.getBirthDate().toString());
        add(birthDateField);

        // Action buttons
        updateInfoButton = new JButton("Update Info");
        changePasswordButton = new JButton("Change Password");
        add(updateInfoButton);
        add(changePasswordButton);

        // Event Listeners
        updateInfoButton.addActionListener((ActionEvent e) -> updateUserInfo());
        changePasswordButton.addActionListener((ActionEvent e) -> changePassword());
    }

    private void updateUserInfo() {
        String fullName = fullNameField.getText();
        String address = addressField.getText();
        String birthDate = birthDateField.getText();
        String email = emailField.getText();
        String gender = (String) genderField.getSelectedItem();

        if (fullName.isEmpty() || address.isEmpty() || email.isEmpty() || birthDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Email format
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate birth date format
        try {
            java.sql.Date date = java.sql.Date.valueOf(birthDate);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid birth date format!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update user information in database
        currentUser.setFullName(fullName);
        currentUser.setAddress(address);
        currentUser.setEmail(email);
        currentUser.setGender(gender);
        currentUser.setBirthDate(java.sql.Date.valueOf(birthDate));

        boolean success = userService.updateUser(currentUser);

        if (success) {
            JOptionPane.showMessageDialog(this, "Information updated successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update information.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changePassword() {
        // Show dialog for password change
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Current Password:"));
        JPasswordField currentPasswordField = new JPasswordField();
        panel.add(currentPasswordField);

        panel.add(new JLabel("New Password:"));
        JPasswordField newPasswordField = new JPasswordField();
        panel.add(newPasswordField);

        panel.add(new JLabel("Confirm New Password:"));
        JPasswordField confirmPasswordField = new JPasswordField();
        panel.add(confirmPasswordField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Change Password", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            UserService userService = new UserService();
            User user = null;
            try {
                user = userService.getUsersByUsername(currentUser.getUsername()).get(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (!user.getPassword().equals(currentPassword)) {
                JOptionPane.showMessageDialog(this, "Current password is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            user.setPassword(confirmPassword);

            boolean success = userService.updateUser(user);
            if (success) {
                JOptionPane.showMessageDialog(this, "Password changed successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to change password. Please check your current password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
