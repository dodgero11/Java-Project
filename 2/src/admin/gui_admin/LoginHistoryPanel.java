package gui_admin;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class LoginHistoryPanel extends JPanel {
    public LoginHistoryPanel() {
        setLayout(new BorderLayout());

        // Table to display login history
        String[] columns = {"Time", "Username", "Full Name"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable historyTable = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(historyTable);

        add(tableScrollPane, BorderLayout.CENTER);
    }
}