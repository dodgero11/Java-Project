package gui_admin;

import bll.*;
import java.awt.*;
import java.sql.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class LoginHistoryPanel extends JPanel {

    private UserService userService;
    private User currentUser;

    public LoginHistoryPanel(String username) {
        userService = new UserService();

        try {
            currentUser = userService.getUsersByUsername(username).get(0);
        } catch (Exception e) { 
            e.printStackTrace(); 
        }

        setLayout(new BorderLayout());

        // Table to display login history
        String[] columns = {"Time", "Username", "Full Name"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable historyTable = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(historyTable);

        // Sort the table
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        historyTable.setRowSorter(sorter);

        // Get login history from UserService
        UserService userService = new UserService();
        try {
            List<User> loginHistory = userService.getLoginHistory(currentUser.getUsername());
            for (User user : loginHistory) {
                for (Date date : user.getLoginHistory()) {
                    model.addRow(new Object[]{date, user.getUsername(), user.getFullName()});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        add(tableScrollPane, BorderLayout.CENTER);
    }
}