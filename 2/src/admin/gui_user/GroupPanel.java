package gui_user;

import bll.Group;
import bll.Message;
import bll.User;
import bll.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class GroupPanel extends JPanel {

    private UserService userService;
    private User currentUser;

    private JTable groupTable;
    private DefaultTableModel groupTableModel;
    private JPopupMenu rightClickMenu;

    private ScheduledExecutorService scheduler;

    public GroupPanel(String currentUser) {
        this.userService = new UserService();

        try {
            this.currentUser = userService.getUsersByUsername(currentUser).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLayout(new BorderLayout());

        // Table for groups
        String[] columns = { "Group Name", "Participants" };
        groupTableModel = new DefaultTableModel(columns, 0);
        groupTable = new JTable(groupTableModel);
        JScrollPane scrollPane = new JScrollPane(groupTable);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons for main actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadGroups());
        buttonPanel.add(refreshButton);

        JButton createGroupButton = new JButton("Create Group");
        createGroupButton.addActionListener(e -> createGroup());
        buttonPanel.add(createGroupButton);

        add(buttonPanel, BorderLayout.NORTH);

        // Create right-click menu
        rightClickMenu = new JPopupMenu();
        JMenuItem giveAdminItem = new JMenuItem("Give admin");
        JMenuItem deleteGroupItem = new JMenuItem("Delete group");
        JMenuItem addParticipantItem = new JMenuItem("Add a participant");
        JMenuItem removeParticipantItem = new JMenuItem("Remove a participant");
        JMenuItem openGroupChatItem = new JMenuItem("Open group chat");
        JMenuItem changeGroupNameItem = new JMenuItem("Change group name");

        rightClickMenu.add(giveAdminItem);
        rightClickMenu.add(deleteGroupItem);
        rightClickMenu.add(addParticipantItem);
        rightClickMenu.add(removeParticipantItem);
        rightClickMenu.add(openGroupChatItem);
        rightClickMenu.add(changeGroupNameItem);

        // Add mouse listener for table
        groupTable.addMouseListener(new MouseAdapter() {
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
                int row = groupTable.rowAtPoint(e.getPoint());
                if (row != -1) {
                    groupTable.setRowSelectionInterval(row, row);
                    rightClickMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // Event handlers for menu items
        giveAdminItem.addActionListener(e -> giveAdmin());
        deleteGroupItem.addActionListener(e -> deleteGroup());
        addParticipantItem.addActionListener(e -> addParticipant());
        removeParticipantItem.addActionListener(e -> removeParticipant());
        openGroupChatItem.addActionListener(e -> openGroupChat());
        changeGroupNameItem.addActionListener(e -> changeGroupName());

        // Add menu bar to the panel
        JPanel menuPanel = new JPanel(new BorderLayout());
        add(menuPanel, BorderLayout.WEST);

        loadGroups();
    }

    private void loadGroups() {
        groupTableModel.setRowCount(0); // Clear existing rows
        try {
            List<Group> groups = userService.getGroupsForUser(currentUser.getUsername());
            for (Group group : groups) {
                groupTableModel.addRow(new Object[] { group.getName(), String.join(", ", group.getParticipants()) });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createGroup() {
        String groupName = JOptionPane.showInputDialog(this, "Enter group name:");
        if (groupName != null && !groupName.trim().isEmpty()) {
            try {
                int groupId = userService.createGroup(groupName, currentUser.getUsername());
                if (groupId > 0) {
                    JOptionPane.showMessageDialog(this, "Group created successfully!");
                    loadGroups();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create group.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addParticipant() {
        int selectedRow = groupTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group to add a participant.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String groupName = groupTableModel.getValueAt(selectedRow, 0).toString();
        String username = JOptionPane.showInputDialog(this, "Enter the username to add:");
        if (username != null && !username.trim().isEmpty()) {
            try {
                Group group = userService.getGroupByName(groupName);
                if (userService.addParticipantToGroup(group.getGroupId(), username, false)) {
                    JOptionPane.showMessageDialog(this, "Participant added successfully!");
                    group.addParticipant(username);
                    loadGroups();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add participant.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void removeParticipant() {
        int selectedRow = groupTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group to remove a participant.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String groupName = groupTableModel.getValueAt(selectedRow, 0).toString();
        String username = JOptionPane.showInputDialog(this, "Enter the username to remove:");
        if (username != null && !username.trim().isEmpty()) {
            try {
                int groupId = userService.getGroupByName(groupName).getGroupId();
                if (userService.removeParticipantFromGroup(groupId, username, currentUser.getUsername())) {
                    JOptionPane.showMessageDialog(this, "Participant removed successfully!");
                    loadGroups();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to remove participant.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openGroupChat() {
        int selectedRow = groupTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group to open.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String groupName = groupTableModel.getValueAt(selectedRow, 0).toString();

        Group group = null;
        try {
            group = userService.getGroupByName(groupName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final int groupId = group.getGroupId();
        JTextArea chatArea = new JTextArea();

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                backgroundTask(chatArea, groupId); // Reload chat every 5 seconds
            }
        }, 0, 5, TimeUnit.SECONDS);

        JFrame chatFrame = new JFrame("Group Chat: " + groupName);
        chatFrame.setSize(500, 600);
        chatFrame.setLayout(new BorderLayout());
        chatFrame.setLocationRelativeTo(null);

        // Chat display area
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        // Message input area
        JTextField messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            String messageContent = messageField.getText();
            if (!messageContent.isEmpty()) {
                try {

                    if (userService.sendMessageToGroup(userService.getGroupByName(groupName).getGroupId(),
                            currentUser.getUsername(), messageContent)) {
                        chatArea.append(currentUser.getUsername() + ": " + messageContent + "\n");
                        messageField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(chatFrame, "Failed to send message.", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatFrame.add(chatScroll, BorderLayout.CENTER);
        chatFrame.add(inputPanel, BorderLayout.SOUTH);
        chatFrame.setVisible(true);

        chatFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Mark the chat window as closed
                scheduler.shutdownNow();
            }
        });
    }

    private void giveAdmin() {
        int selectedRow = groupTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group to give admin.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String groupName = groupTableModel.getValueAt(selectedRow, 0).toString();
        String username = JOptionPane.showInputDialog(this, "Enter the username to give admin:");
        if (username != null && !username.trim().isEmpty()) {
            try {
                if (userService.giveAdmin(userService.getGroupByName(groupName).getGroupId(), username,
                        currentUser.getUsername())) {
                    JOptionPane.showMessageDialog(this, "Admin given successfully!");
                    loadGroups();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to give admin.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteGroup() {
        int selectedRow = groupTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group to delete.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String groupName = groupTableModel.getValueAt(selectedRow, 0).toString();
        // Warning
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + groupName + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (userService.deleteGroup(userService.getGroupByName(groupName).getGroupId(),
                        currentUser.getUsername())) {
                    JOptionPane.showMessageDialog(this, "Group deleted successfully!");
                    loadGroups();
                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void changeGroupName() {
        int selectedRow = groupTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group to change name.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String groupName = groupTableModel.getValueAt(selectedRow, 0).toString();
        String newName = JOptionPane.showInputDialog(this, "Enter new group name:");
        if (newName != null && !newName.trim().isEmpty()) {
            try {
                if (userService.changeGroupName(userService.getGroupByName(groupName).getGroupId(),
                        newName, currentUser.getUsername())) {
                    JOptionPane.showMessageDialog(this, "Group name changed successfully!");
                    loadGroups();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to change group name.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private JTextArea backgroundTask(JTextArea chatArea, int groupId) {
        chatArea.setEditable(false);
        chatArea.setText("");
        List<Message> messages = null;
        try {
            messages = userService.getGroupMessages(groupId);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        for (Message message : messages) {
            chatArea.append(message.getSender() + ": " + message.getContent() + "\n");
        }
        System.out.println("1");
        return chatArea;
    }
}
