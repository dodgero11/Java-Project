package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import bll.SpamReport;

public class SpamReportDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/java";
    private static final String USER = "root"; 
    private static final String PASSWORD = "7z9aZbse928WJUf"; 

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection temp_connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public List<SpamReport> getSpamReports() throws SQLException {
        String sql = "SELECT report_id, reported_by, reported_user, reason, created_at FROM SPAM_REPORTS";
        List<SpamReport> spamReports = new ArrayList<>();
        
        try (Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                SpamReport report = new SpamReport();
                report.setReportId(rs.getInt("report_id"));
                report.setReportedBy(rs.getString("reported_by"));
                report.setReportedUser(rs.getString("reported_user"));
                report.setReason(rs.getString("reason"));
                report.setCreatedAt(rs.getDate("created_at"));
                spamReports.add(report);
            }
        }
        return spamReports;
    }
}
