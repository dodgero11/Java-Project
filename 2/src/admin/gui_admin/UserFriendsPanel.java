package gui_admin;

import bll.User;
import bll.UserService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class UserFriendsPanel extends JPanel {

    private JTable userTable;
    private JTextField filterTextField;

    public UserFriendsPanel() {
        setLayout(new BorderLayout());

        // Filter text field
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterTextField = new JTextField(20);
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterTextField);

        // Table to display users
        String[] columns = { "Username", "Creation Date", "Number of Friends", "Number of Friends' Friends" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        userTable = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(userTable);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        userTable.setRowSorter(sorter);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshUserPanelButton = new JButton("Refresh");

        buttonPanel.add(refreshUserPanelButton);

        // Add components to panel
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.NORTH);
        add(filterPanel, BorderLayout.SOUTH);

        // Event listeners
        refreshUserPanelButton.addActionListener(e -> refreshUserTable());

        // Filter text field listener
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

        // Load user data into table
        refreshUserTable();
    }

    private void refreshUserTable() {
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.setRowCount(0);
        UserService userService = new UserService();

        try {
            List<User> users = userService.getUsersAndFriends();
            for (User user : users) {
                model.addRow(new Object[] {
                        user.getUsername(),
                        user.getCreationDate(),
                        user.getFriendsCount(),
                        user.getFriendsFriendsCount()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
