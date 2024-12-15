package gui_admin;

import bll.Group;
import bll.User;
import bll.UserService;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class GroupManagementPanel extends JPanel {

    private JPanel buttonPanel;
    private JTable groupsTable;
    private JTable membersTable;
    private JTable adminsTable;

    public GroupManagementPanel() {
        setLayout(new BorderLayout());

        // Table to display groups
        String[] columns = {"Group ID", "Group Name", "Created At"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        groupsTable = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(groupsTable);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        groupsTable.setRowSorter(sorter);

        // Fetch groups data
        UserService userService = new UserService();
        try {
            List<Group> groups = userService.getAllGroups();
            for (Group group : groups) {
                model.addRow(new Object[] {
                    group.getGroupId(),
                    group.getName(),
                    group.getDateCreated()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Filter
        JTextField filterField = new JTextField(20);

        // Filter text field listener
        filterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                String text = filterField.getText();
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                String text = filterField.getText();
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                String text = filterField.getText();
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        // Buttons
        buttonPanel = new JPanel(new FlowLayout());
        JButton viewMembersButton = new JButton("View Members");
        JButton viewAdminsButton = new JButton("View Admins");
        JTextArea filterTextArea = new JTextArea("Filter:");
        buttonPanel.add(filterTextArea);
        buttonPanel.add(filterField);
        buttonPanel.add(viewMembersButton);
        buttonPanel.add(viewAdminsButton);

        // Listeners
        viewMembersButton.addActionListener(e -> viewMembersButton());
        viewAdminsButton.addActionListener(e -> viewAdminsButton());

        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void viewMembersButton() {
        JFrame membersFrame = new JFrame("Group Members");
        membersFrame.setSize(800, 600);

        // Table to display group members
        String[] columns = {"Username", "Full Name", "Address", "Birthdate", "Email", "Gender", "Creation Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable membersTable = new JTable(model);
        JScrollPane membersScrollPane = new JScrollPane(membersTable);

        // Get selected group id
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int groupId = Integer.parseInt(groupsTable.getValueAt(selectedRow, 0).toString());

        membersFrame.add(membersScrollPane);
        // Fetch group members data
        UserService userService = new UserService();
        try {
            List<User> members = userService.getGroupParticipants(groupId);
            for (User member : members) {
                model.addRow(new Object[] {
                    member.getUsername(),
                    member.getFullName(),
                    member.getAddress(),
                    member.getBirthDate(),    
                    member.getEmail(),
                    member.getGender(),
                    member.getCreationDate(),
                    member.getIsActive()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        membersFrame.setLocationRelativeTo(null); // Center the frame on the screen
        membersFrame.setVisible(true);
    }

    private void viewAdminsButton() {
        JFrame adminsFrame = new JFrame("Group Admins");
        adminsFrame.setSize(800, 600);

        // Table to display group admins
        String[] columns = {"Username", "Full Name", "Address", "Birthdate", "Email", "Gender", "Creation Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable adminsTable = new JTable(model);
        JScrollPane adminsScrollPane = new JScrollPane(adminsTable);    

        // Get selected group id
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int groupId = Integer.parseInt(groupsTable.getValueAt(selectedRow, 0).toString());

        adminsFrame.add(adminsScrollPane);
        // Fetch group admins data
        UserService userService = new UserService();
        try {
            List<User> admins = userService.getGroupAdmins(groupId);
            for (User admin : admins) {
                model.addRow(new Object[] {
                    admin.getUsername(),
                    admin.getFullName(),
                    admin.getAddress(),
                    admin.getBirthDate(),    
                    admin.getEmail(),
                    admin.getGender(),
                    admin.getCreationDate(),
                    admin.getIsActive()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        adminsFrame.setLocationRelativeTo(null); // Center the frame on the screen
        adminsFrame.setVisible(true);
    }
}