package dao;

import bll.SpamReport;
import common.ConfigReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SpamReportDAO {

    private Connection getConnection() throws SQLException {
        // Get the database connection details
        String URL = ConfigReader.get("db.url");
        String USER = ConfigReader.get("db.username");
        String PASSWORD = ConfigReader.get("db.password");
    
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Return the connection
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
            throw new SQLException("Database driver initialization failed", e);
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            throw e; // Rethrow to allow upstream handling
        }
    }
    // Get all spam reports
    public List<SpamReport> getSpamReports() throws SQLException {
        String sql = "SELECT report_id, reported_by, reported_user, reason, status, created_at FROM SPAM_REPORTS";
        List<SpamReport> spamReports = new ArrayList<>();

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                SpamReport report = new SpamReport();
                report.setReportId(rs.getString("report_id"));
                report.setReportedBy(rs.getString("reported_by"));
                report.setReportedUser(rs.getString("reported_user"));
                report.setReason(rs.getString("reason"));
                report.setStatus(rs.getString("status"));
                report.setCreatedAt(rs.getDate("created_at"));
                spamReports.add(report);
            }
        }
        return spamReports;
    }

    // Get a specific spam report
    public SpamReport getSpamReportById(String reportId) throws SQLException {
        String sql = "SELECT report_id, reported_by, reported_user, reason, status, created_at FROM SPAM_REPORTS WHERE report_id = ?";
        SpamReport report = null;

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reportId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                report = new SpamReport();
                report.setReportId(rs.getString("report_id"));
                report.setReportedBy(rs.getString("reported_by"));
                report.setReportedUser(rs.getString("reported_user"));
                report.setReason(rs.getString("reason"));
                report.setStatus(rs.getString("status"));
                report.setCreatedAt(rs.getDate("created_at"));
            }
        }
        return report;
    }

    // Dismiss a spam report
    public void dismissReport(String reportId) throws SQLException {
        String sql = "UPDATE SPAM_REPORTS SET status = 'Resolved' WHERE report_id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reportId);
            stmt.executeUpdate();
        }
    }

    // Report a user as spam
    public boolean reportSpam(String reporterUsername, String reportedUsername, String description) throws Exception {
        String sql = "INSERT INTO SPAM_REPORTS (reported_by, reported_user, reason) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reporterUsername);
            stmt.setString(2, reportedUsername);
            stmt.setString(3, description);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
