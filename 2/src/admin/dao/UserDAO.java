package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import bll.*;

public class UserDAO {
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

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT username, password, full_name, address, email, gender, birth_date, status, created_at FROM users";
        
        try (Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullName(rs.getString("full_name"));
                user.setAddress(rs.getString("address"));
                user.setEmail(rs.getString("email"));
                user.setGender(rs.getString("gender"));
                user.setBirthDate(rs.getDate("birth_date"));
                user.setCreationDate(rs.getDate("created_at"));
                user.setIsActive(rs.getString("status"));
                users.add(user);
            }
        }
        return users;
    }

    // Add a user to the database
    public boolean addUser(User user) {
        String sql = "INSERT INTO USERS (username, password, full_name, address, birth_date, gender, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getAddress());
            stmt.setDate(5, user.getBirthDate());
            stmt.setString(6, user.getGender());
            stmt.setString(7, user.getEmail());
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
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET full_name = ?, password = ? , address = ?, birth_date = ?, gender = ?, email = ?, status = ? WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getAddress());
            stmt.setDate(4, user.getBirthDate());
            stmt.setString(5, user.getGender());
            stmt.setString(6, user.getEmail());
            stmt.setString(7, user.getIsActive());
            stmt.setString(8, user.getUsername());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Search for users by name or username
    public List<User> getUsersByUsername(String searchTerm) {
        String sql = "SELECT * FROM users WHERE username LIKE ?";
        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullName(rs.getString("full_name"));
                user.setAddress(rs.getString("address"));
                user.setEmail(rs.getString("email"));
                user.setGender(rs.getString("gender"));
                user.setBirthDate(rs.getDate("birth_date"));
                user.setCreationDate(rs.getDate("created_at"));
                user.setIsActive(rs.getString("status"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<User> getFriends(String username) throws SQLException {
        String sql = """
            SELECT u.*
            FROM USERS u
            JOIN USER_FRIENDS uf 
                ON (u.user_id = uf.friend_id AND uf.user_id = (
                    SELECT user_id FROM USERS WHERE username = ?))
                OR (u.user_id = uf.user_id AND uf.friend_id = (
                    SELECT user_id FROM USERS WHERE username = ?))
        """;
        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set the username parameter twice for the subqueries
            stmt.setString(1, username);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setAddress(rs.getString("address"));
                user.setEmail(rs.getString("email"));
                user.setGender(rs.getString("gender"));
                user.setBirthDate(rs.getDate("birth_date"));
                user.setCreationDate(rs.getDate("created_at"));
                user.setIsActive(rs.getString("status"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Optionally rethrow the exception
        }
        return users;
    }
}