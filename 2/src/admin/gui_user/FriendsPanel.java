package gui_user;

import bll.User;
import bll.UserService;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FriendsPanel extends JPanel {

    private UserService userService;
    private User currentUser;

    private JTable friendsTable;
    private JTextField filterTextField;
    private JButton removeFriendButton, viewRequestsButton, blockMenuItem, viewProfileButton, refreshButton;

    public FriendsPanel(String username) {
        this.userService = new UserService();

        try {
            this.currentUser = userService.getUsersByUsername(username).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLayout(new BorderLayout());

        // Filter text field
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterTextField = new JTextField(10); // 20 columns wide
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterTextField);

        // Friends Table
        String[] columns = { "Name", "Status (Online/Offline)" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        friendsTable = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(friendsTable);

        // Buttons
        viewRequestsButton = new JButton("View Requests");
        viewProfileButton = new JButton("View Profile");
        removeFriendButton = new JButton("Remove Friend");
        blockMenuItem = new JButton("Block User");
        refreshButton = new JButton("Refresh");
        filterPanel.add(viewRequestsButton);
        filterPanel.add(viewProfileButton);
        filterPanel.add(removeFriendButton);
        filterPanel.add(blockMenuItem);
        filterPanel.add(refreshButton);

        add(tableScrollPane, BorderLayout.CENTER);
        add(filterPanel, BorderLayout.NORTH);

        // Listeners
        removeFriendButton.addActionListener((ActionEvent e) -> removeFriend());
        viewRequestsButton.addActionListener((ActionEvent e) -> viewRequests());
        viewProfileButton.addActionListener((ActionEvent e) -> openUserDetails());
        blockMenuItem.addActionListener((ActionEvent e) -> blockUser());
        refreshButton.addActionListener((ActionEvent e) -> refreshFriends(model));

        // Filter text field listener
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        friendsTable.setRowSorter(sorter);
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

        // Load friends
        refreshFriends(model);
    }

    private void refreshFriends(DefaultTableModel model) {
        model.setRowCount(0); // Clear existing rows

        try {
            for (User friend : userService.getFriendsByUsername(currentUser.getUsername())) {
                model.addRow(new Object[] { friend.getUsername() });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeFriend() {
        int selectedRow = friendsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a friend to remove.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String friendUsername = friendsTable.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove " + friendUsername + " as a friend?", "Confirm",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = false;
            try {
                success = userService.removeFriend(currentUser.getUsername(), friendUsername);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (success) {
                JOptionPane.showMessageDialog(this, "Friend removed successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshFriends((DefaultTableModel) friendsTable.getModel());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove friend. Please try again later.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewRequests() {
        JFrame requestsFrame = new JFrame("Friend Requests");
        requestsFrame.setSize(400, 300);
        requestsFrame.setLocationRelativeTo(this);

        String[] columns = { "Name", "Action" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable requestsTable = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(requestsTable);

        // Load friend requests
        try {
            for (User requester : userService.getPendingFriendRequests(currentUser.getUsername())) {
                model.addRow(new Object[] { requester.getUsername(), "Pending" });
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        // Accept and Decline buttons
        JButton acceptButton = new JButton("Accept");
        JButton declineButton = new JButton("Decline");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(acceptButton);
        buttonPanel.add(declineButton);

        requestsFrame.setLayout(new BorderLayout());
        requestsFrame.add(tableScrollPane, BorderLayout.CENTER);
        requestsFrame.add(buttonPanel, BorderLayout.SOUTH);

        // Action listeners
        acceptButton.addActionListener(e -> {
            int selectedRow = requestsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(requestsFrame, "Please select a request to accept.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean success = false;
            String requesterUsername = requestsTable.getValueAt(selectedRow, 0).toString();
            try {
                success = userService.acceptFriendRequest(requesterUsername, currentUser.getUsername());
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            if (success) {
                JOptionPane.showMessageDialog(requestsFrame, "Friend request accepted.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshFriends((DefaultTableModel) friendsTable.getModel());
            } else {
                JOptionPane.showMessageDialog(requestsFrame, "Failed to accept friend request. Please try again later.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        declineButton.addActionListener(e -> {
            int selectedRow = requestsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(requestsFrame, "Please select a request to reject.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String requesterUsername = requestsTable.getValueAt(selectedRow, 0).toString();
            boolean success = false;
            try {
                success = userService.rejectFriendRequest(requesterUsername, currentUser.getUsername());
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            if (success) {
                JOptionPane.showMessageDialog(requestsFrame, "Friend request rejected.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshFriends((DefaultTableModel) friendsTable.getModel());
            } else {
                JOptionPane.showMessageDialog(requestsFrame,
                        "Failed to decline friend request. Please try again later.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        requestsFrame.setVisible(true);
    }

    private void openUserDetails() {
        int selectedRow = friendsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String username = friendsTable.getValueAt(selectedRow, 0).toString();
        User user = null;
        try {
            user = userService.getUsersByUsername(username).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(this, "User Details:\n" +
                "Username: " + user.getUsername() + "\n" +
                "Full Name: " + user.getFullName() + "\n" +
                "Email: " + user.getEmail() + "\n" +
                "Address: " + user.getAddress() + "\n" +
                "Birth Date: " + user.getBirthDate() + "\n" +
                "Gender: " + user.getGender() + "\n" +
                "Status: " + user.getIsActive() + "\n",
                "User Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    private void blockUser() {
        int selectedRow = friendsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = friendsTable.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to block " + username + "?",
                "Confirm Block", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = userService.blockUser(currentUser.getUsername(), username);
                if (success) {
                    JOptionPane.showMessageDialog(this, username + " has been blocked.");
                    refreshFriends((DefaultTableModel) friendsTable.getModel());
                } else {
                    JOptionPane.showMessageDialog(this, "Unable to block user. The user may already be blocked.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error blocking user: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}
