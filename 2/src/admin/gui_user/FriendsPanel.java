package gui_user;

import bll.Message;
import bll.User;
import bll.UserService;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FriendsPanel extends JPanel {

    private UserService userService;
    private User currentUser;

    private JTable friendsTable;
    private JTable incomingRequestsTable;
    private JTextField filterTextField;
    private JPopupMenu rightClickMenu;
    private JFrame requestFrame;

    private ScheduledExecutorService scheduler;

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
        filterTextField = new JTextField(10);
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterTextField);

        // Friends Table
        String[] columns = { "Name", "Status (Online/Offline)" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        friendsTable = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(friendsTable);

        // Add panels for displaying friends
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        add(panel, BorderLayout.CENTER);

        // Create right-click menu
        rightClickMenu = new JPopupMenu();
        JMenuItem viewProfileItem = new JMenuItem("View Profile");
        JMenuItem chatItem = new JMenuItem("Start Chat");
        JMenuItem createGroupItem = new JMenuItem("Create Group");
        JMenuItem removeFriendItem = new JMenuItem("Remove Friend");
        JMenuItem blockItem = new JMenuItem("Block User");

        rightClickMenu.add(viewProfileItem);
        rightClickMenu.add(chatItem);
        rightClickMenu.add(createGroupItem);
        rightClickMenu.add(removeFriendItem);
        rightClickMenu.add(blockItem);

        // Add mouse listener for table
        friendsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int row = friendsTable.rowAtPoint(e.getPoint());
                if (row != -1) {
                    friendsTable.setRowSelectionInterval(row, row);
                    rightClickMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // Event handlers for menu items
        viewProfileItem.addActionListener(e -> openUserDetails());
        chatItem.addActionListener(e -> startChat());
        createGroupItem.addActionListener(e -> createGroup());
        removeFriendItem.addActionListener(e -> removeFriend());
        blockItem.addActionListener(e -> blockUser());

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

        // Load friends and incoming requests
        refreshFriends(model);

        // Add Friend Requests Button
        JButton friendRequestsButton = new JButton("Friend Requests");
        friendRequestsButton.addActionListener(e -> showFriendRequests());
        filterPanel.add(friendRequestsButton);

        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshFriends(model));
        filterPanel.add(refreshButton);

    }

    private void showFriendRequests() {
        // Create a new frame for displaying incoming requests
        if (requestFrame == null) {
            requestFrame = new JFrame("Friend Requests");
            requestFrame.setSize(400, 300);

            String[] requestColumns = { "Sender", "Status" };
            DefaultTableModel requestModel = new DefaultTableModel(requestColumns, 0);
            incomingRequestsTable = new JTable(requestModel);
            JScrollPane requestScrollPane = new JScrollPane(incomingRequestsTable);
            requestFrame.add(requestScrollPane, BorderLayout.CENTER);

            // Sort the table
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(requestModel);
            incomingRequestsTable.setRowSorter(sorter);

            JButton acceptButton = new JButton("Accept");
            JButton rejectButton = new JButton("Reject");
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(acceptButton);
            buttonPanel.add(rejectButton);
            requestFrame.add(buttonPanel, BorderLayout.SOUTH);

            // Handle accept/reject
            acceptButton.addActionListener(e -> handleFriendRequest("Accept"));
            rejectButton.addActionListener(e -> handleFriendRequest("Reject"));

            requestFrame.setLocationRelativeTo(null); // Center the frame on the screen
            requestFrame.setVisible(true);
            refreshIncomingRequests(requestModel);
        } else {
            requestFrame.setVisible(true); // If already opened, just bring it to front
        }
    }

    private void refreshFriends(DefaultTableModel model) {
        model.setRowCount(0); // Clear existing rows
        try {
            List<User> friends = userService.getFriendsByUsername(currentUser.getUsername());
            for (User friend : friends) {
                model.addRow(new Object[]{friend.getUsername(), friend.getIsActive()});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshIncomingRequests(DefaultTableModel requestModel) {
        requestModel.setRowCount(0); // Clear existing requests
        try {
            List<User> requests = userService.getPendingFriendRequests(currentUser.getUsername());
            for (User sender : requests) {
                requestModel.addRow(new Object[]{sender.getUsername(), "Pending"});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleFriendRequest(String action) {
        int selectedRow = incomingRequestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request to " + action, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String senderUsername = incomingRequestsTable.getValueAt(selectedRow, 0).toString();

        try {
            boolean success = false;
            if ("Accept" == action) {
                success = userService.acceptFriendRequest(senderUsername, currentUser.getUsername());
            } else if ("Reject" == action) {
                success = userService.rejectFriendRequest(senderUsername, currentUser.getUsername());
            }

            if (success) {
                JOptionPane.showMessageDialog(this, "Friend request " + action + "ed.");
                refreshIncomingRequests((DefaultTableModel) incomingRequestsTable.getModel());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to " + action + " friend request. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void startChat() {
        int selectedRow = friendsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = friendsTable.getValueAt(selectedRow, 0).toString();
        JOptionPane.showMessageDialog(this, "Starting chat with: " + username);

        JFrame chatFrame = new JFrame("Chat - " + username);
        chatFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chatFrame.setSize(600, 400);
        chatFrame.setLocationRelativeTo(null);

        // Chat display area
        JTextArea chatArea = new JTextArea(20, 50);
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatFrame.add(chatScrollPane, BorderLayout.CENTER);

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                backgroundTask(chatArea, username);  // Reload chat every 5 seconds
            }
        }, 0, 5, TimeUnit.SECONDS);

        // Message input
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatFrame.add(inputPanel, BorderLayout.SOUTH);

        // Listener for sending messages
        sendButton.addActionListener(e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                try {
                    if (userService.sendMessage(currentUser.getUsername(), null, userService.getConversationId(currentUser.getUsername(), username), message)) {
                        JOptionPane.showMessageDialog(this, "Message sent successfully.");
                    } else {
                        JOptionPane.showMessageDialog(this, "You might be blocked from sending messages.");
                    }
                    chatArea.append(currentUser.getUsername() + ": " + message + "\n");
                    messageField.setText("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error sending message.");
                }
            }
        });
        chatFrame.setVisible(true);

        chatFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Mark the chat window as closed
                scheduler.shutdownNow();
            }
        });
    }
    private void createGroup() {
        int selectedRow = friendsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        String username = friendsTable.getValueAt(selectedRow, 0).toString();
    
        // Create group frame
        JFrame groupFrame = new JFrame("Create Group");
        groupFrame.setSize(400, 100);
        groupFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        groupFrame.setLocationRelativeTo(null);
        groupFrame.setLayout(new BorderLayout()); // Use BorderLayout
    
        // Panel for group input
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel groupNameLabel = new JLabel("Group Name:");
        JTextField groupNameField = new JTextField(20);
        inputPanel.add(groupNameLabel);
        inputPanel.add(groupNameField);
    
        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
    
        // Add panels to frame
        groupFrame.add(inputPanel, BorderLayout.CENTER);
        groupFrame.add(buttonPanel, BorderLayout.SOUTH);
    
        // Listener for creating groups
        createButton.addActionListener(e -> {
            String groupName = groupNameField.getText().trim();
            if (!groupName.isEmpty()) {
                try {
                    userService.createGroupWith(groupName, currentUser.getUsername(), username);
                    JOptionPane.showMessageDialog(groupFrame, "Group created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    groupFrame.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(groupFrame, "Error creating group: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(groupFrame, "Please enter a group name.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    
        // Listener for cancel button
        cancelButton.addActionListener(e -> groupFrame.dispose());
    
        groupFrame.setVisible(true);
    }

    private void removeFriend() {
        int selectedRow = friendsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a friend to remove.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String friendUsername = friendsTable.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove " + friendUsername + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = userService.removeFriend(currentUser.getUsername(), friendUsername);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Friend removed successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to remove friend. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void blockUser() {
        int selectedRow = friendsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to block.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String friendUsername = friendsTable.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to block " + friendUsername + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = userService.blockUser(currentUser.getUsername(), friendUsername);
                if (success) {
                    JOptionPane.showMessageDialog(this, "User blocked successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to block user. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void backgroundTask(JTextArea chatArea, String username) {
        chatArea.setEditable(false);
        chatArea.setText("");
        try {
            List<Message> messages = userService.getChatMessagesWith(currentUser.getUsername(), username);
            for (Message message : messages) {
                chatArea.append(message.getSender() + ": " + message.getContent() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
