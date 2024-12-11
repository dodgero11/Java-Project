package dao;

import bll.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/java";
    private static final String USER = "root";
    private static final String PASSWORD = "7z9aZbse928WJUf";

    private Connection getConnection() throws SQLException {
        try {

            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Test the connection
            Connection temp_connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        // Return the connection
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Get all users
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

    // Search for users by username
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

    // Send a friend request
    public boolean sendFriendRequest(String user1, String user2) throws SQLException {

        // Check for existing relationship
        String checkSql = """
                SELECT COUNT(*) FROM USER_FRIENDS
                WHERE ((user_username = ? AND friend_username = ?)
                OR (user_username = ? AND friend_username = ?))
                OR (user_username = ? AND friend_username = ? AND status = 'Blocked')
                """;

        // Insert into USER_FRIENDS table
        String insertSql = """
                INSERT INTO USER_FRIENDS (user_username, friend_username, status)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // Check for existing relationship
            checkStmt.setString(1, user1);
            checkStmt.setString(2, user2);
            checkStmt.setString(3, user2);
            checkStmt.setString(4, user1);
            checkStmt.setString(5, user2);
            checkStmt.setString(6, user1);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Relationship already exists in either direction
                    return false;
                }
            }

            // No relationship found, proceed to insert a new friend request
            insertStmt.setString(1, user1);
            insertStmt.setString(2, user2);
            insertStmt.setString(3, "Pending");
            return insertStmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get pending friend requests
    public List<User> getPendingFriendRequests(String username) throws SQLException {

        // SQL to retrieve pending friend requests
        String sql = """
                SELECT u.*
                FROM USERS u
                JOIN USER_FRIENDS uf
                    ON ((u.username = uf.user_username AND uf.friend_username = (
                        SELECT username FROM USERS WHERE username = ?)))
                    AND (uf.status = 'Pending')
                    AND (u.username != ?);
                """;
        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

    public boolean acceptFriendRequest(String user1, String user2) throws SQLException {
        String sql = "UPDATE USER_FRIENDS SET status = ? WHERE user_username = ? AND friend_username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "Accepted");
            stmt.setString(2, user1);
            stmt.setString(3, user2);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Reject a friend request
    public boolean rejectFriendRequest(String user1, String user2) throws SQLException {

        // Delete the friend request
        String sql = "DELETE FROM USER_FRIENDS WHERE user_username = ? AND friend_username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user1);
            stmt.setString(2, user2);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Remove a friend
    public boolean removeFriend(String user1, String user2) throws SQLException {

        // Remove the friend
        String sql = "DELETE FROM USER_FRIENDS WHERE (user_username = ? AND friend_username = ?) OR (user_username = ? AND friend_username = ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user1);
            stmt.setString(2, user2);
            stmt.setString(3, user2);
            stmt.setString(4, user1);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get friends by username
    public List<User> getFriendsByUsername(String username) throws SQLException {
        // Query to retrieve friends 
        String sql = """
                SELECT u.*
                FROM USERS u
                JOIN USER_FRIENDS uf
                    ON ((u.username = uf.friend_username AND uf.user_username = (
                        SELECT username FROM USERS WHERE username = ?))
                    OR (u.username = uf.user_username AND uf.friend_username = (
                        SELECT username FROM USERS WHERE username = ?)))
                    AND (uf.status = 'Accepted')
                    AND (u.username != ?);
                """;
        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            stmt.setString(3, username);
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

    public boolean blockUser(String currentUser, String targetUser) throws SQLException {
        // Check if the relationship already exists
        String checkSql = """
                SELECT status FROM USER_FRIENDS
                WHERE user_username = ? AND friend_username = ?
                """;

        // Update USER_FRIENDS table if already existed
        String updateSql = """
                UPDATE USER_FRIENDS
                SET status = 'Blocked'
                WHERE user_username = ? AND friend_username = ?
                """;
        
        // Delete existing relationship
        String deleteSql = """
                DELETE FROM USER_FRIENDS WHERE user_username = ? AND friend_username = ?
                """;

        // Insert into USER_FRIENDS table if not existed
        String insertSql = """
                INSERT INTO USER_FRIENDS (user_username, friend_username, status)
                VALUES (?, ?, 'Blocked')
                """;

        try (Connection conn = getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // Check if relationship exists
            checkStmt.setString(1, currentUser);
            checkStmt.setString(2, targetUser);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {

                    // If relationship exists, update to 'Blocked'
                    updateStmt.setString(1, currentUser);
                    updateStmt.setString(2, targetUser);

                    // Delete existing relationship
                    deleteStmt.setString(1, targetUser);
                    deleteStmt.setString(2, currentUser);
                    deleteStmt.executeUpdate();

                    return updateStmt.executeUpdate() > 0;
                } else {

                    // If no relationship exists, insert 'Blocked'
                    insertStmt.setString(1, currentUser);
                    insertStmt.setString(2, targetUser);

                    // Delete existing relationship
                    deleteStmt.setString(1, targetUser);
                    deleteStmt.setString(2, currentUser);
                    deleteStmt.executeUpdate();
                    return insertStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Unblock a user
    public boolean unblockUser(String currentUser, String targetUser) throws SQLException {
        String sql = "DELETE FROM USER_FRIENDS WHERE user_username = ? AND friend_username = ? AND status = 'Blocked'";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser);
            stmt.setString(2, targetUser);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Register a new user
    public boolean registerUser(User user) {
        String sql = "INSERT INTO USERS (username, password, full_name, email, gender, address, birth_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getGender());
            stmt.setString(6, user.getAddress());
            stmt.setDate(7, new java.sql.Date(user.getBirthDate().getTime()));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateUser(String username, String password) {
        String sql = "SELECT * FROM USERS WHERE username = ? AND password = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a user is found
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}