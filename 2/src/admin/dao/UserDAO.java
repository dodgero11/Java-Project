package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/java";
    private static final String USER = "root";  // Replace with your DB username
    private static final String PASSWORD = "7z9aZbse928WJUf";  // Replace with your DB password

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection temp_connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database!");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Add a user to the database
    public boolean addUser(String username, String fullName, String address, Date birthDate, String gender, String email) {
        String sql = "INSERT INTO USERS (username, full_name, address, birth_date, gender, email) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, fullName);
            stmt.setString(3, address);
            stmt.setDate(4, birthDate);
            stmt.setString(5, gender);
            stmt.setString(6, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Remove a user from the database
    public boolean removeUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update user information
    public boolean updateUser(String username, String fullName, String address, Date birthDate, String gender, String email) {
        String sql = "UPDATE users SET full_name = ?, address = ?, birth_date = ?, gender = ?, email = ? WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setString(2, address);
            stmt.setDate(3, birthDate);
            stmt.setString(4, gender);
            stmt.setString(5, email);
            stmt.setString(6, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Search for users by name or username
    public List<String[]> searchUsers(String searchTerm) {
        String sql = "SELECT * FROM users WHERE username LIKE ? OR full_name LIKE ?";
        List<String[]> users = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + searchTerm + "%");
            stmt.setString(2, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String[] user = new String[6];
                user[0] = rs.getString("username");
                user[1] = rs.getString("full_name");
                user[2] = rs.getString("address");
                user[3] = rs.getDate("birth_date").toString();
                user[4] = rs.getString("gender");
                user[5] = rs.getString("email");
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}