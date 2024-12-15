// Updated ChatPanel
package gui_user;

import bll.Message;
import bll.User;
import bll.UserService;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ChatPanel extends JPanel {
    private User currentUser;
    private UserService userService;

    private JComboBox<String> conversationSelector;
    private JTable messageTable;
    private DefaultTableModel tableModel;
    private JTextField messageField;
    private JButton sendButton;

    ScheduledExecutorService scheduler;
    private int selectedConversationId = -1;

    public ChatPanel(String username) {
        userService = new UserService();

        try {
            this.currentUser = userService.getUsersByUsername(username).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLayout(new BorderLayout());

        // Conversation selector
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        conversationSelector = new JComboBox<>();
        conversationSelector.addItem("Select Conversation");
        conversationSelector.addActionListener(e -> loadConversationChat());
        topPanel.add(new JLabel("Conversations:"));
        topPanel.add(conversationSelector);

        JButton deleteMessagesButton = new JButton("Delete Selected Messages");
        deleteMessagesButton.addActionListener(e -> deleteSelectedMessages());
        topPanel.add(deleteMessagesButton);

        JButton deleteAllMessagesButton = new JButton("Delete All Messages");
        deleteAllMessagesButton.addActionListener(e -> deleteAllMessages());
        topPanel.add(deleteAllMessagesButton);

        JButton searchButton = new JButton("Search Chat History");
        searchButton.addActionListener(e -> searchChatHistory());
        topPanel.add(searchButton);

        add(topPanel, BorderLayout.NORTH);

        // Message display table
        String[] columnNames = { "Sender", "Message", "Message ID" };
        tableModel = new DefaultTableModel(columnNames, 0);
        messageTable = new JTable(tableModel);
        messageTable.getColumnModel().getColumn(2).setMinWidth(0); // Hide Message ID
        messageTable.getColumnModel().getColumn(2).setMaxWidth(0);
        JScrollPane scrollPane = new JScrollPane(messageTable);
        add(scrollPane, BorderLayout.CENTER);

        // Message input
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(this::sendMessage);
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Load available conversations
        loadConversations();

        // Reload chat every 5 seconds
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::loadConversationChat, 0, 5, TimeUnit.SECONDS);

        this.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("invisible")) {
                scheduler.shutdownNow();
            }
        });
    }

    private void loadConversations() {
        try {
            List<String> conversations = userService.getUserConversations(currentUser.getUsername());
            conversationSelector.removeAllItems();
            conversationSelector.addItem("Select Conversation");
            for (String conversation : conversations) {
                conversationSelector.addItem(conversation);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading conversations.");
        }
    }

    private void loadConversationChat() {
        String selectedConversation = (String) conversationSelector.getSelectedItem();
        if (selectedConversation == null || selectedConversation.equals("Select Conversation")) return;

        selectedConversationId = Integer.parseInt(selectedConversation.split("\\(ID: ")[1].replace(")", ""));

        try {
            tableModel.setRowCount(0);
            List<Message> messages = userService.getChatMessages(null, selectedConversationId);
            for (Message message : messages) {
                tableModel.addRow(new Object[] { message.getSender(), message.getContent(), message.getMessageId() });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading conversation chat.");
        }
    }

    private void sendMessage(ActionEvent e) {
        String message = messageField.getText().trim();
        if (message.isEmpty() || selectedConversationId == -1) {
            JOptionPane.showMessageDialog(this, "Enter a message and select a conversation.");
            return;
        }

        try {
            if (userService.sendMessage(currentUser.getUsername(), null, selectedConversationId, message)) {
                JOptionPane.showMessageDialog(this, "Message sent successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "You might be blocked from sending messages.");
            }
            messageField.setText("");
            loadConversationChat();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error sending message.");
        }
    }

    private void deleteSelectedMessages() {
        int[] selectedRows = messageTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "No messages selected for deletion.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected messages?");
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            for (int row : selectedRows) {
                int messageId = (int) tableModel.getValueAt(row, 2);
                if (userService.deleteMessage(messageId, currentUser.getUsername())) {
                    JOptionPane.showMessageDialog(this, "Message could not be deleted.");
                }
                else {
                    JOptionPane.showMessageDialog(this, "Selected messages deleted successfully.");
                }
            }
            loadConversationChat();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Message could not be deleted.");
        }
    }

    // Delete all messages of a conversation
    private void deleteAllMessages() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete all messages of the selected conversation?");
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            userService.deleteConversationById(selectedConversationId);
            loadConversationChat();
            JOptionPane.showMessageDialog(this, "All messages deleted successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Conversation could not be deleted.");
            ex.printStackTrace();
        }
    }

    private void searchChatHistory() {
        String searchTerm = JOptionPane.showInputDialog(this, "Enter text to search for:");
        if (searchTerm == null || searchTerm.trim().isEmpty()) return;

        try {
            List<Message> results;
            if (selectedConversationId == -1) {
                results = userService.searchMessagesAcrossConversations(currentUser.getUsername(), searchTerm);
            } else {
                results = userService.searchMessagesInConversation(currentUser.getUsername(), selectedConversationId, searchTerm);
            }

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No matches found.");
                return;
            }

            // Clear messages
            tableModel.setRowCount(0);
            messageField.setText("Search Results:\n");
            for (Message message : results) {
                tableModel.addRow(new Object[] { message.getSender(), message.getContent(), message.getMessageId() });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching chat history.");
            e.printStackTrace();
        }
    }
}
