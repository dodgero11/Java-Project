package dao;

import bll.*;
import common.ConfigReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

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

    // Get all users
    public List<User> getAllUsers(String username) throws SQLException {
        List<User> users = new ArrayList<>();
        // Get All users who haven't blocked the current user
        String sql = """
                    SELECT u.username, u.password, u.full_name, u.address, u.email, u.gender, u.birth_date, u.created_at, us.status AS online_status
                    FROM USERS u
                    LEFT JOIN USER_STATUS us ON u.username = us.username
                    LEFT JOIN USER_FRIENDS uf ON (u.username = uf.user_username AND uf.friend_username = ?)
                    WHERE (uf.status IS NULL OR uf.status != 'Blocked') AND u.username != ?;
                    """;

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, username);
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
                user.setIsActive(rs.getString("online_status"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Add a user to the database
    public boolean addUser(User user) {
        String sql1 = "INSERT INTO USERS (username, password, full_name, email, gender, address, birth_date) VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Put user into user_status table
        String sql2 = "INSERT INTO USER_STATUS (username, status) VALUES (?, 'Offline')";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql1)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getGender());
            stmt.setString(6, user.getAddress());
            stmt.setDate(7, new java.sql.Date(user.getBirthDate().getTime()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Put user into user_status table
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql2)) {
            stmt.setString(1, user.getUsername());
            stmt.executeUpdate();
            return true;
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
        String sql = """
                SELECT  u.username, u.password, u.full_name, u.address, u.email, u.gender, u.birth_date, u.status AS online_status,
                        u.created_at, us.status AS online_status, us.last_activity
                FROM USERS u
                LEFT JOIN USER_STATUS us ON u.username = us.username
                WHERE u.username LIKE ?
                """;
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
                user.setIsActive(rs.getString("online_status"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Get user and number of friends
    public List<User> getUsersAndFriends() throws SQLException {
        // Get username, number of their friends and number of their friends' friends
        String sql = """
                SELECT u.username, u.created_at,
                    (SELECT COUNT(*) FROM USER_FRIENDS WHERE (user_username = u.username OR friend_username = u.username) AND status = 'Accepted') AS num_friends,
                    (SELECT COUNT(*) FROM USER_FRIENDS WHERE (user_username = u.username OR friend_username = u.username) AND status = 'Accepted' AND friend_username IN (
                        SELECT friend_username FROM USER_FRIENDS WHERE (user_username = u.username OR friend_username = u.username) AND status = 'Accepted'
                    )) AS num_friends_of_friends
                FROM USERS u
                LEFT JOIN USER_STATUS us ON u.username = us.username
                """;
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                UserService userService = new UserService();
                String username = rs.getString("username");
                Date creationDate = rs.getDate("created_at");
                int numFriends = rs.getInt("num_friends");
                int numFriendsOfFriends = rs.getInt("num_friends_of_friends");
                User user = new User(username, creationDate, numFriends, numFriendsOfFriends);
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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
                    SELECT
                        u.username,
                        u.full_name,
                        u.address,
                        u.email,
                        u.gender,
                        u.birth_date,
                        u.created_at,
                        us.status AS online_status
                    FROM USERS u
                    JOIN USER_FRIENDS uf ON
                        (u.username = uf.friend_username AND uf.user_username = ?)
                        OR (u.username = uf.user_username AND uf.friend_username = ?)
                    LEFT JOIN USER_STATUS us ON u.username = us.username
                    WHERE uf.status = 'Accepted'
                        AND u.username != ?
                    ORDER BY us.status DESC; -- Optional: Prioritize friends who are online
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
                user.setIsActive(rs.getString("online_status"));
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
                WHERE user_username = ? AND friend_username = ? AND status != 'Blocked'
                """;

        // Delete existing relationship
        String deleteSql = """
                DELETE FROM USER_FRIENDS WHERE user_username = ? AND friend_username = ? AND status != 'Blocked'
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

    // Login
    public boolean validateUser(String username, String password) {
        String sql = "SELECT * FROM USERS WHERE username = ? AND password = ? AND status = 'Active'";
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

    // Get login history
    public List<User> getLoginHistory(String username) throws SQLException {
        String sql = """
                SELECT u.username, u.full_name, lh.login_time
                FROM USERS u
                JOIN LOGIN_HISTORY lh ON u.username = lh.username
                WHERE u.username = ?
                ORDER BY lh.login_time DESC
                """;
    
        List<User> loginHistory = new ArrayList<>();
        List<Date> loginDates = new ArrayList<>();
        String currentUsername = null;
        String currentFullName = null;
    
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username); // Set the username parameter
    
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String usernameFromDB = rs.getString("username");
                    String fullNameFromDB = rs.getString("full_name");
    
                    if (currentUsername == null || !currentUsername.equals(usernameFromDB)) {
                        // If a new user is encountered, add the previous User (if any) to the list
                        if (currentUsername != null) {
                            User user = new User(currentUsername, currentFullName, new ArrayList<>(loginDates));
                            loginHistory.add(user);
                        }
    
                        // Reset for the new user
                        currentUsername = usernameFromDB;
                        currentFullName = fullNameFromDB;
                        loginDates.clear();
                    }
    
                    // Add login time for the current user
                    loginDates.add(rs.getDate("login_time"));
                }
    
                // Add the last user to the list (if any records exist)
                if (currentUsername != null) {
                    User user = new User(currentUsername, currentFullName, new ArrayList<>(loginDates));
                    loginHistory.add(user);
                }
            }
        }
    
        return loginHistory;
    }
    
    
    // Get conversations for a user
    public List<String> getUserConversations(String username) throws SQLException {
        String sql = "SELECT conversation_id, user1_username, user2_username " +
                "FROM CHAT_CONVERSATIONS " +
                "WHERE user1_username = ? OR user2_username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            try (ResultSet rs = stmt.executeQuery()) {
                List<String> conversations = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt("conversation_id");
                    String user1 = rs.getString("user1_username");
                    String user2 = rs.getString("user2_username");
                    String partner = user1.equals(username) ? user2 : user1;
                    conversations.add(partner + " (ID: " + id + ")");
                }
                return conversations;
            }
        }
    }

    // Get a conversation with a specific user
    public int getConversationId(String user1, String user2) throws SQLException {
        String sql = "SELECT conversation_id FROM CHAT_CONVERSATIONS WHERE (user1_username = ? AND user2_username = ?) OR (user1_username = ? AND user2_username = ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user1);
            stmt.setString(2, user2);
            stmt.setString(3, user2);
            stmt.setString(4, user1);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("conversation_id");
                } else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Message> createNewConversation(String user1, String user2) throws SQLException {
        // Check if user2 blocked user1
        String blockedSql = "SELECT * FROM USER_FRIENDS WHERE user_username = ? AND friend_username = ? AND status = 'Blocked'";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(blockedSql)) {
            stmt.setString(1, user2);
            stmt.setString(2, user1);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return null;
                }
            }
        }

        // Create a new conversation
        String sql = "INSERT INTO CHAT_CONVERSATIONS (user1_username, user2_username, created_at) VALUES (?, ?, NOW())";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user1);
            stmt.setString(2, user2);
            stmt.executeUpdate();
            return getChatMessages(null, getConversationId(user1, user2));
        }
    }

    // Send message in private conversation
    public boolean sendMessage(String sender, Integer groupId, Integer conversationId, String message)
            throws SQLException {
        // Check if user2 blocked user1
        if (conversationId != -1) {
            String blockedSql = "SELECT * FROM USER_FRIENDS WHERE friend_username = ? AND user_username = (SELECT user2_username FROM CHAT_CONVERSATIONS WHERE conversation_id = ?) AND status = 'Blocked'";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(blockedSql)) {
                stmt.setString(1, sender);
                stmt.setInt(2, conversationId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return false;
                    }
                }
            }
        }

        String sql = "INSERT INTO CHAT_MESSAGES (group_id, conversation_id, sender_user_username, message, sent_at) " +
                "VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, groupId);
            stmt.setObject(2, conversationId);
            stmt.setString(3, sender);
            stmt.setString(4, message);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get chat messages from a private conversation or group chat
    public List<Message> getChatMessages(Integer groupId, Integer conversationId) throws SQLException {
        String sql = "SELECT message_id, group_id, conversation_id, sender_user_username, message, sent_at " +
                "FROM CHAT_MESSAGES " +
                "WHERE (group_id = ? OR conversation_id = ?) " +
                "ORDER BY sent_at ASC";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, groupId);
            stmt.setObject(2, conversationId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Message> messages = new ArrayList<>();
                while (rs.next()) {
                    Message message = new Message(
                            rs.getInt("message_id"),
                            rs.getObject("group_id") != null ? rs.getInt("group_id") : null,
                            rs.getObject("conversation_id") != null ? rs.getInt("conversation_id") : null,
                            rs.getString("sender_user_username"),
                            rs.getString("message"),
                            rs.getTimestamp("sent_at").toLocalDateTime());
                    messages.add(message);
                }
                return messages;
            }
        }
    }

    // Delete a message
    public boolean deleteMessageById(int messageId, String username) throws Exception {
        String sql = "DELETE FROM chat_messages WHERE message_id = ? AND sender_user_username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, messageId);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new Exception("Error deleting message", e);
        }

        // Check if any messages were deleted
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn
                        .prepareStatement("SELECT COUNT(*) FROM chat_messages WHERE message_id = ?")) {
            stmt.setInt(1, messageId);
            try (ResultSet rs = stmt.executeQuery()) {
                // If no messages were deleted, return false
                if (!rs.next() || rs.getInt(1) == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    // Delete a conversation
    public void deleteConversationById(int conversationId) throws Exception {
        // SQL for deleting messages and conversation
        String deleteMessagesSQL = "DELETE FROM chat_messages WHERE conversation_id = ?";
        String deleteConversationSQL = "DELETE FROM chat_conversations WHERE conversation_id = ?";

        try (Connection conn = getConnection()) {
            // Begin transaction
            conn.setAutoCommit(false);

            // Delete messages
            try (PreparedStatement deleteMessagesStmt = conn.prepareStatement(deleteMessagesSQL)) {
                deleteMessagesStmt.setInt(1, conversationId);
                deleteMessagesStmt.executeUpdate();
            }

            // Delete conversation
            try (PreparedStatement deleteConversationStmt = conn.prepareStatement(deleteConversationSQL)) {
                deleteConversationStmt.setInt(1, conversationId);
                deleteConversationStmt.executeUpdate();
            }

            // Commit transaction
            conn.commit();
        } catch (Exception e) {
            throw new Exception("Error deleting conversation", e);
        }
    }

    // Search messages from users and others across conversations of a user with
    // others
    public List<Message> searchMessagesAcrossConversations(String username, String searchTerm) throws Exception {
        String sql = "SELECT message_id, group_id, conversation_id, sender_user_username, message, sent_at " +
                "FROM CHAT_MESSAGES " +
                "WHERE message LIKE ? " +
                "AND (sender_user_username = ? " +
                "     OR conversation_id IN (SELECT conversation_id FROM CHAT_CONVERSATIONS " +
                "                            WHERE user1_username = ? OR user2_username = ?)) " +
                "ORDER BY sent_at ASC";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameters
            stmt.setString(1, "%" + searchTerm + "%"); // Search term
            stmt.setString(2, username); // For messages sent by the user
            stmt.setString(3, username); // For conversations where the user is user1
            stmt.setString(4, username); // For conversations where the user is user2

            try (ResultSet rs = stmt.executeQuery()) {
                List<Message> messages = new ArrayList<>();
                while (rs.next()) {
                    Message message = new Message(
                            rs.getInt("message_id"),
                            rs.getObject("group_id") != null ? rs.getInt("group_id") : null,
                            rs.getObject("conversation_id") != null ? rs.getInt("conversation_id") : null,
                            rs.getString("sender_user_username"),
                            rs.getString("message"),
                            rs.getTimestamp("sent_at").toLocalDateTime());
                    messages.add(message);
                }
                return messages;
            }
        }
    }

    // Search messages in a conversation
    public List<Message> searchMessagesInConversation(String username, int conversationId, String searchTerm)
            throws Exception {
        String sql = "SELECT message_id, group_id, conversation_id, sender_user_username, message, sent_at " +
                "FROM CHAT_MESSAGES " +
                "WHERE message LIKE ? AND conversation_id = ? " +
                "ORDER BY sent_at ASC";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + searchTerm + "%");
            stmt.setInt(2, conversationId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Message> messages = new ArrayList<>();
                while (rs.next()) {
                    Message message = new Message(
                            rs.getInt("message_id"),
                            rs.getObject("group_id") != null ? rs.getInt("group_id") : null,
                            rs.getObject("conversation_id") != null ? rs.getInt("conversation_id") : null,
                            rs.getString("sender_user_username"),
                            rs.getString("message"),
                            rs.getTimestamp("sent_at").toLocalDateTime());
                    messages.add(message);
                }
                return messages;
            }
        }
    }

    // User goes online
    public void userOnline(String username) throws Exception {
        // Update User Status
        String sql = "UPDATE user_status SET status = 'online', last_activity = now() WHERE username = ?";

        // Update Login History
        String sql1 = "INSERT INTO login_history (username, login_time) VALUES (?, now())";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql1)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }

    }

    // User goes offline
    public void userOffline(String username) throws Exception {
        String sql = "UPDATE user_status SET status = 'offline', last_activity = now() WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }
}