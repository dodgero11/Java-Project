package gui;

import bll.User;
import bll.UserService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;
import java.sql.SQLException;

public class FriendsPanel extends JPanel {

    public FriendsPanel(String username) {
        setLayout(new BorderLayout());

        String[] columns = {"Friend Username", "Full Name"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable friendsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(friendsTable);

        add(scrollPane, BorderLayout.CENTER);

        // Fetch friends data
        UserService userService = new UserService();
        try {
            List<User> friends = userService.getFriends(username);
            for (User user : friends) {
                model.addRow(new Object[]{
                    user.getUsername(),
                    user.getFullName(),
                    user.getAddress(),
                    user.getEmail(),
                    user.getBirthDate(),
                    user.getGender(),
                    user.getCreationDate(),
                    user.getIsActive()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading friends: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
