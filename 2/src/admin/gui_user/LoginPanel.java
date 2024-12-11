package gui_user;

import bll.UserService;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class LoginPanel extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;

    public LoginPanel() {

        // Set the layout to FlowLayout for better control over positioning
        setLayout(new GridLayout(4, 4));

        // Username and password fields
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 2, 10));
        inputPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        inputPanel.add(usernameField);

        inputPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        inputPanel.add(passwordField);

        add(inputPanel);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Center buttons

        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 30)); // Set button size
        buttonPanel.add(loginButton);

        registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(100, 30)); // Set button size
        buttonPanel.add(registerButton);

        add(buttonPanel);

        // Action Listeners
        loginButton.addActionListener((ActionEvent e) -> handleLogin());
        registerButton.addActionListener((ActionEvent e) -> openRegisterPanel());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Both fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UserService userService = new UserService();
        boolean isValid = userService.validateUser(username, password);

        if (isValid) {
            JOptionPane.showMessageDialog(this, "Login successful!");
            openUserPanel(username);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegisterPanel() {
        JFrame registerFrame = new JFrame("Register");
        registerFrame.setSize(400, 300);
        registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        registerFrame.add(new RegisterPanel());
        registerFrame.setLocationRelativeTo(this);
        registerFrame.setVisible(true);
    }

    private void openUserPanel(String username) {
        JFrame userFrame = new JFrame("Welcome - " + username);
        userFrame.setSize(800, 600);
        userFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        userFrame.add(new UserHomePanel(username, userFrame)); // Create UserHomePanel for logged-in functionalities
        userFrame.setLocationRelativeTo(this);
        userFrame.setVisible(true);

        // Close the login frame (if needed)
        SwingUtilities.getWindowAncestor(this).dispose();
    }
}