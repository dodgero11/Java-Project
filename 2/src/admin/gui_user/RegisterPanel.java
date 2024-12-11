package gui_user;

import bll.User;
import bll.UserService;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.sql.Date;
import javax.swing.*;

public class RegisterPanel extends JPanel {
    private JTextField usernameField, emailField, fullNameField, addressField;
    private JPasswordField passwordField;
    private JComboBox<String> genderComboBox;
    private JFormattedTextField birthDateField;
    private JButton registerButton;

    public RegisterPanel() {
        setLayout(new GridLayout(0, 2, 10, 10));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel("Full Name:"));
        fullNameField = new JTextField();
        add(fullNameField);

        add(new JLabel("Address:"));
        addressField = new JTextField();
        add(addressField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Gender:"));
        genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        add(genderComboBox);

        add(new JLabel("Birth Date (YYYY-MM-DD):"));
        birthDateField = new JFormattedTextField();
        add(birthDateField);

        registerButton = new JButton("Register");
        add(new JLabel());
        add(registerButton);

        // Action Listener
        registerButton.addActionListener((ActionEvent e) -> registerUser());
    }

    private void registerUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String gender = (String) genderComboBox.getSelectedItem();
        String birthDate = birthDateField.getText();
        String address = addressField.getText();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty() || birthDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate email format
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

        User user = new User(username, password, fullName, address, Date.valueOf(birthDate), gender, email);
        UserService userService = new UserService();
        if (userService.addUser(user)) {
            JOptionPane.showMessageDialog(this, "Registration successful!");
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}