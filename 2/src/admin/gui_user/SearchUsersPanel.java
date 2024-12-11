package gui_user;

import bll.User;
import bll.UserService;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class SearchUsersPanel extends JPanel {

    private UserService userService;
    private User currentUser;

    private JTextField searchTextField;
    private JButton searchButton;
    private JTable usersTable;
    private JPopupMenu rightClickMenu;

    private String selectedUsername;

    public SearchUsersPanel(String currentUsername) {
        this.userService = new UserService();

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
        String[] columns = {"Username", "Full Name", "Status"};
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
        rightClickMenu.add(profileMenuItem);
        rightClickMenu.add(chatMenuItem);
        rightClickMenu.add(friendRequestMenuItem);
        rightClickMenu.add(blockMenuItem);
        rightClickMenu.add(unblockMenuItem);

        // Mouse Listener
        usersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = usersTable.rowAtPoint(evt.getPoint());
                if (row != -1) {
                    usersTable.setRowSelectionInterval(row, row); 
        
                    if (evt.getClickCount() == 2) { // Double-click for unnecessary mis-click
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

        // Chat will be implemented later
        // userService.startChat(currentUser.getUsername(), username);
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
                JOptionPane.showMessageDialog(this, "Unable to send friend request. The user may already be your friend, you have already sent a friend request or blocked.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error sending friend request: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to block " + username + "?", "Confirm Block", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = userService.blockUser(currentUser.getUsername(), username);
                if (success) {
                    JOptionPane.showMessageDialog(this, username + " has been blocked.");
                } else {
                    JOptionPane.showMessageDialog(this, "Unable to block user. The user may already be blocked.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error blocking user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to unblock " + username + "?", "Confirm Unblock", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = userService.unblockUser(currentUser.getUsername(), username);
                if (success) {
                    JOptionPane.showMessageDialog(this, username + " has been unblocked.");
                } else {
                    JOptionPane.showMessageDialog(this, "Unable to unblock user. The user may not be blocked.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error unblocking user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void refreshUserTable() {
        DefaultTableModel model = (DefaultTableModel) usersTable.getModel();
        model.setRowCount(0);
        UserService userService = new UserService();

        try {
            List<User> users = userService.getAllUsers(); 
            for (User user : users) {
                if (user.getUsername().equals(currentUser.getUsername())) {
                    continue;
                }
                model.addRow(new Object[]{
                    user.getUsername(),
                    user.getFullName(),
                    user.getIsActive()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
