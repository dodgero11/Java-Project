package gui_user;

import bll.Message;
import bll.SpamReport;
import bll.User;
import bll.UserService;
import bll.SpamReportService;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class SearchUsersPanel extends JPanel {

    private UserService userService;
    private SpamReportService spamReportService;
    private User currentUser;

    private JTextField searchTextField;
    private JButton searchButton;
    private JTable usersTable;
    private JPopupMenu rightClickMenu;

    private ScheduledExecutorService scheduler;
    private String selectedUsername;

    public SearchUsersPanel(String currentUsername) {
        this.userService = new UserService();
        this.spamReportService = new SpamReportService();

        try {
            currentUser = userService.getUsersByUsername(currentUsername).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set the layout for the panel
        setLayout(new BorderLayout());

        // Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchTextField = new JTextField(20);
        searchButton = new JButton("Search");
        searchPanel.add(new JLabel("Search Users:"));
        searchPanel.add(searchTextField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // Users Table
        String[] columns = { "Username", "Full Name", "Status" };
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        usersTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(usersTable);
        add(tableScrollPane, BorderLayout.SOUTH);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        usersTable.setRowSorter(sorter);

        // Right-Click Menu
        rightClickMenu = new JPopupMenu();
        JMenuItem profileMenuItem = new JMenuItem("View Profile");
        JMenuItem chatMenuItem = new JMenuItem("Start Chat");
        JMenuItem friendRequestMenuItem = new JMenuItem("Send Friend Request");
        JMenuItem blockMenuItem = new JMenuItem("Block User");
        JMenuItem unblockMenuItem = new JMenuItem("Unblock User");
        JMenuItem reportSpamMenuItem = new JMenuItem("Report Spam");
        rightClickMenu.add(profileMenuItem);
        rightClickMenu.add(chatMenuItem);
        rightClickMenu.add(friendRequestMenuItem);
        rightClickMenu.add(blockMenuItem);
        rightClickMenu.add(unblockMenuItem);
        rightClickMenu.add(reportSpamMenuItem);

        // Mouse Listener
        usersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = usersTable.rowAtPoint(evt.getPoint());
                if (row != -1) {
                    usersTable.setRowSelectionInterval(row, row);

                    if (evt.getClickCount() == 1) { // Double-click for unnecessary mis-clicks
                        selectedUsername = usersTable.getValueAt(row, 0).toString();
                        rightClickMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                    }
                }
            }
        });

        // Filter text field listener
        searchTextField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                String text = searchTextField.getText();
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                String text = searchTextField.getText();
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                String text = searchTextField.getText();
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        profileMenuItem.addActionListener(e -> openUserDetails());
        chatMenuItem.addActionListener(e -> startChat());
        friendRequestMenuItem.addActionListener(e -> sendFriendRequest());
        blockMenuItem.addActionListener(e -> blockUser());
        unblockMenuItem.addActionListener(e -> unblockUser());
        reportSpamMenuItem.addActionListener(e -> reportSpam());

        refreshUserTable();
    }

    private void openUserDetails() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        User user = null;
        try {
            user = userService.getUsersByUsername(usersTable.getValueAt(selectedRow, 0).toString()).get(0);
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
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = usersTable.getValueAt(selectedRow, 0).toString();
        JOptionPane.showMessageDialog(this, "Starting chat with: " + username);

        JFrame chatFrame = new JFrame("Chat - " + username);
        chatFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chatFrame.setSize(600, 400);
        chatFrame.setLocationRelativeTo(null);

        // Chat display area
        JTextArea chatArea = new JTextArea();

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                backgroundTask(chatArea, username); // Reload chat every 5 seconds
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

        // Listener for closing the chat window
        chatFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                scheduler.shutdown();
            }
        });

        chatFrame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatFrame.setVisible(true);
    }

    private void sendFriendRequest() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = usersTable.getValueAt(selectedRow, 0).toString();
        try {
            boolean success = userService.sendFriendRequest(currentUser.getUsername(), username);
            if (success) {
                JOptionPane.showMessageDialog(this, "Friend request sent to: " + username);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Unable to send friend request. The user may already be your friend, you have already sent a friend request or blocked.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error sending friend request: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void blockUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = usersTable.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to block " + username + "?",
                "Confirm Block", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = userService.blockUser(currentUser.getUsername(), username);
                if (success) {
                    JOptionPane.showMessageDialog(this, username + " has been blocked.");
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

    private void unblockUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = usersTable.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to unblock " + username + "?",
                "Confirm Unblock", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = userService.unblockUser(currentUser.getUsername(), username);
                if (success) {
                    JOptionPane.showMessageDialog(this, username + " has been unblocked.");
                } else {
                    JOptionPane.showMessageDialog(this, "Unable to unblock user. The user may not be blocked.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error unblocking user: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void refreshUserTable() {
        DefaultTableModel model = (DefaultTableModel) usersTable.getModel();
        model.setRowCount(0);
        UserService userService = new UserService();

        try {
            List<User> users = userService.getAllUsers(currentUser.getUsername());
            for (User user : users) {
                if (user.getUsername().equals(currentUser.getUsername())) {
                    continue;
                }
                model.addRow(new Object[] {
                        user.getUsername(),
                        user.getFullName(),
                        user.getIsActive()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void reportSpam() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = usersTable.getValueAt(selectedRow, 0).toString();

        // Create the report spam frame
        JFrame reportSpamFrame = new JFrame("Report Spam - " + username);
        reportSpamFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        reportSpamFrame.setSize(400, 300);
        reportSpamFrame.setLocationRelativeTo(null);
        reportSpamFrame.setLayout(new BorderLayout());

        // Report form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        // Add components
        formPanel.add(new JLabel("Reporting User: " + currentUser.getUsername()));
        formPanel.add(new JLabel("Reported User: " + username));
        JLabel descriptionLabel = new JLabel("Reason:");
        formPanel.add(descriptionLabel);
        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setRows(4);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        formPanel.add(descriptionScrollPane);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing between components
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton submitButton = new JButton("Submit Report");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        // Add action for cancel button
        cancelButton.addActionListener(e -> reportSpamFrame.dispose());

        // Add action for submit button
        submitButton.addActionListener(e -> {
            String reportDescription = descriptionArea.getText().trim();
            if (reportDescription.isEmpty()) {
                JOptionPane.showMessageDialog(reportSpamFrame, "Please provide a description for the report.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    spamReportService.reportSpam(currentUser.getUsername(), username, reportDescription);
                    JOptionPane.showMessageDialog(reportSpamFrame, "Spam report submitted successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    reportSpamFrame.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(reportSpamFrame, "Error submitting spam report: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        // Assemble frame
        reportSpamFrame.add(formPanel, BorderLayout.CENTER);
        reportSpamFrame.add(buttonPanel, BorderLayout.SOUTH);
        reportSpamFrame.setVisible(true);
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
