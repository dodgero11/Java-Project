package gui_admin;

import java.awt.*;
import javax.swing.*;

public class AdminDashboardPanel extends JFrame {
    private JPanel mainPanel;

    public AdminDashboardPanel() {
        setTitle("Admin Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();

        // User Management Menu
        JMenu userMenu = new JMenu("User Management");
        JMenuItem viewUsers = new JMenuItem("View Users");

        userMenu.add(viewUsers);
        menuBar.add(userMenu);

        // Group Management Menu
        JMenu groupMenu = new JMenu("Group Management");
        JMenuItem viewGroups = new JMenuItem("View Groups");

        groupMenu.add(viewGroups);
        menuBar.add(groupMenu);

        // Spam Reports Panel
        JMenu spamReportsMenu = new JMenu("Spam Reports");
        JMenuItem viewSpamReports = new JMenuItem("View Spam Reports");

        spamReportsMenu.add(viewSpamReports);
        menuBar.add(spamReportsMenu);

        // User Friends Panel
        JMenu userFriendsMenu = new JMenu("User Friends");
        JMenuItem viewUserFriends = new JMenuItem("View User Friends");

        userFriendsMenu.add(viewUserFriends);
        menuBar.add(userFriendsMenu);

        // Main panel
        setJMenuBar(menuBar);
        mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        // Event Listeners
        viewUsers.addActionListener(e -> showUserManagement());
        viewGroups.addActionListener(e -> showGroupManagement());
        viewSpamReports.addActionListener(e -> showSpamReportsPanel());
        viewUserFriends.addActionListener(e -> showFriendPanel());
    }

    private void showUserManagement() {
        mainPanel.removeAll();
        mainPanel.add(new UserManagementPanel());
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showGroupManagement() {
        mainPanel.removeAll();
        mainPanel.add(new GroupManagementPanel());
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showSpamReportsPanel() {
        mainPanel.removeAll();
        mainPanel.add(new SpamReportsPanel()); 
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showFriendPanel() {
        mainPanel.removeAll();
        mainPanel.add(new UserFriendsPanel()); 
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboardPanel().setVisible(true));
    }
}
