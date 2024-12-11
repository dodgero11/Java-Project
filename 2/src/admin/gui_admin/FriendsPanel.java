package gui_admin;

import bll.User;
import bll.UserService;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FriendsPanel extends JPanel {

    private UserService userService;

    public FriendsPanel(String username) {
        setLayout(new BorderLayout());

        String[] columns = {"Friend Username", "Full Name"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable friendsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(friendsTable);

        add(scrollPane, BorderLayout.CENTER);

        // Fetch friends data
        userService = new UserService();
        try {
            List<User> friends = userService.getFriendsByUsername(username);
            for (User friend : friends) {
                model.addRow(new Object[]{
                    friend.getUsername(),
                    friend.getFullName(),
                    friend.getAddress(),
                    friend.getEmail(),
                    friend.getBirthDate(),
                    friend.getGender(),
                    friend.getCreationDate(),
                    friend.getIsActive()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading friends: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
