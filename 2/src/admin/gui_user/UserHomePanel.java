package gui_user;

import bll.User;
import bll.UserService;
import java.awt.*;
import javax.swing.*;

public class UserHomePanel extends JPanel {

    private User currentUser;
    private UserService userService;

    private JPanel mainPanel;

    public UserHomePanel(String username, JFrame parentFrame) {
        this.userService = new UserService();
        try {
            this.currentUser = userService.getUsersByUsername(username).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        parentFrame.setTitle("User Home");
        setSize(800, 600);
        parentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        parentFrame.setLocationRelativeTo(null);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();

        // Chats Menu
        JMenu chatsMenu = new JMenu("Chats");
        JMenuItem chatMenuItem = new JMenuItem("View chats");
    
        chatsMenu.add(chatMenuItem);
        menuBar.add(chatsMenu);
        
        // Groups menu
        JMenu groupsMenu = new JMenu("Groups");
        JMenuItem groupsMenuItem = new JMenuItem("View groups");

        groupsMenu.add(groupsMenuItem);
        menuBar.add(groupsMenu);

        // Search menu
        JMenu searchMenu = new JMenu("Search");
        JMenuItem searchMenuItem = new JMenuItem("Search users");

        searchMenu.add(searchMenuItem);
        menuBar.add(searchMenu);

        // Friends menu
        JMenu friendsMenu = new JMenu("Friends");
        JMenuItem friendsMenuItem = new JMenuItem("View friends");

        friendsMenu.add(friendsMenuItem);
        menuBar.add(friendsMenu);

        // Profile menu
        JMenu profileMenu = new JMenu("Profile");
        JMenuItem profileMenuItem = new JMenuItem("View profile");

        profileMenu.add(profileMenuItem);
        menuBar.add(profileMenu);

        // Main panel
        parentFrame.setJMenuBar(menuBar);
        mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        // Event listeners
        chatMenuItem.addActionListener(e -> showChatPanel());
        groupsMenuItem.addActionListener(e -> showGroupsPanel());
        searchMenuItem.addActionListener(e -> showSearchPanel());
        friendsMenuItem.addActionListener(e -> showFriendsPanel());
        profileMenuItem.addActionListener(e -> showProfilePanel());
    }

    private void showChatPanel() {
        mainPanel.removeAll();
        //mainPanel.add(new ChatPanel(currentUser.getUsername()));
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showGroupsPanel() {
        mainPanel.removeAll();
        //mainPanel.add(new GroupsPanel(currentUser.getUsername()));
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showSearchPanel() {
        mainPanel.removeAll();
        mainPanel.add(new SearchUsersPanel(currentUser.getUsername()));
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showFriendsPanel() {    
        mainPanel.removeAll();
        mainPanel.add(new FriendsPanel(currentUser.getUsername()));
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showProfilePanel() {
        mainPanel.removeAll();
        mainPanel.add(new ProfilePanel(currentUser.getUsername()));
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}