package dao;

import bll.*;
import common.ConfigReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {

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

    // Get all groups
    public List<Group> getAllGroups() {
        String query = """
            SELECT g.group_id, g.group_name, g.created_at, p.username AS participant
            FROM CHAT_GROUPS g
            JOIN CHAT_GROUP_PARTICIPANTS p ON g.group_id = p.group_id
            ORDER BY g.group_id, p.username
        """;
    
        List<Group> groups = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                Group currentGroup = null;
                while (resultSet.next()) {
                    // Retrieve data from the result set
                    int groupId = resultSet.getInt("group_id");
                    String groupName = resultSet.getString("group_name");
                    String participant = resultSet.getString("participant");
                    Date createdAt = resultSet.getDate("created_at");
    
                    // If a new group is encountered, create a new Group object
                    if (currentGroup == null || !currentGroup.getName().equals(groupName)) {
                        currentGroup = new Group(groupId,groupName, new ArrayList<>(), createdAt);
                        groups.add(currentGroup);
                    }
    
                    // Add participant to the current group
                    currentGroup.getParticipants().add(participant);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public List<Group> getGroupsForUser(String username) {
        List<Group> groups = new ArrayList<>();

        String query = "SELECT g.group_id, g.group_name, p.username AS participant " +
                "FROM CHAT_GROUP_PARTICIPANTS p " +
                "JOIN CHAT_GROUPS g ON p.group_id = g.group_id " +
                "WHERE p.group_id IN (SELECT group_id FROM CHAT_GROUP_PARTICIPANTS WHERE username = ?)";

        try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String groupName = resultSet.getString("group_name");
                String participant = resultSet.getString("participant");

                // Check if group exists
                Group group = groups.stream()
                        .filter(g -> g.getName().equals(groupName))
                        .findFirst()
                        .orElse(null);

                if (group == null) {
                    group = new Group(groupName, new ArrayList<>());
                    groups.add(group);
                }
                group.getParticipants().add(participant);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public List<User> getGroupParticipants(int groupId) {
        List<User> participants = new ArrayList<>();
        String query = """
                    SELECT us.username, full_name, email, address, birth_date, gender, u.created_at , us.status as online_status
                    FROM CHAT_GROUP_PARTICIPANTS cp JOIN USERS u ON u.username = cp.username
                    JOIN USER_STATUS us ON us.username = cp.username
                    WHERE group_id = ?
                    ORDER BY username
                    """;
        try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, groupId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String fullName = resultSet.getString("full_name");
                String email = resultSet.getString("email");
                String address = resultSet.getString("address");
                Date birthDate = resultSet.getDate("birth_date");
                String gender = resultSet.getString("gender");
                Date createdAt = resultSet.getDate("created_at");
                String onlineStatus = resultSet.getString("online_status");

                // Add participant to the list
                participants.add(new User(username, fullName, address, birthDate, gender, email, createdAt, onlineStatus));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return participants;
    }

    // Get group admins
    public List<User> getGroupAdmins(int groupId) {
        List<User> admins = new ArrayList<>();
        String query = """
                    SELECT us.username, full_name, email, address, birth_date, gender, u.created_at , us.status as online_status
                    FROM CHAT_GROUP_PARTICIPANTS cp JOIN USERS u ON u.username = cp.username AND cp.is_admin = 1
                    JOIN USER_STATUS us ON us.username = cp.username
                    WHERE group_id = ?
                    ORDER BY username
                    """;
        try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, groupId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String fullName = resultSet.getString("full_name");
                String email = resultSet.getString("email");
                String address = resultSet.getString("address");
                Date birthDate = resultSet.getDate("birth_date");
                String gender = resultSet.getString("gender");
                Date createdAt = resultSet.getDate("created_at");
                String onlineStatus = resultSet.getString("online_status");

                // Add participant to the list
                admins.add(new User(username, fullName, address, birthDate, gender, email, createdAt, onlineStatus));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return admins;
    }

    // Method to create a group
    public int createGroup(String groupName, String adminUsername) {
        // Create the group
        String groupQuery = "INSERT INTO CHAT_GROUPS (group_name, created_by_user_username) VALUES (?, ?)";
        // Add the admin as a participant
        String userQuery = "INSERT INTO CHAT_GROUP_PARTICIPANTS (group_id, username, is_admin) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement stmt1 = conn.prepareStatement(groupQuery, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement stmt2 = conn.prepareStatement(userQuery)) {

            // Insert into CHAT_GROUPS
            stmt1.setString(1, groupName);
            stmt1.setString(2, adminUsername);
            stmt1.executeUpdate();

            // Get the generated group_id
            try (ResultSet rs = stmt1.getGeneratedKeys()) {
                if (rs.next()) {
                    int groupId = rs.getInt(1);

                    // Insert into CHAT_GROUP_PARTICIPANTS
                    stmt2.setInt(1, groupId); // group_id
                    stmt2.setString(2, adminUsername); // admin's username
                    stmt2.setBoolean(3, true); // is_admin = true
                    stmt2.executeUpdate();

                    return groupId; // Return the generated group_id
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Indicates failure
    }

    // Add a participant to a group
    public boolean addParticipantToGroup(int groupId, String username, boolean isAdmin) {
        String query = "INSERT INTO CHAT_GROUP_PARTICIPANTS (group_id, username, is_admin) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, groupId);
            stmt.setString(2, username);
            stmt.setBoolean(3, isAdmin);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Remove a participant from a group
    public boolean removeParticipantFromGroup(int groupId, String username, String adminUsername) {
        // Checks if the admin is an admin
        String adminQuery = "SELECT is_admin FROM CHAT_GROUP_PARTICIPANTS WHERE group_id = ? AND username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(adminQuery)) {
            stmt.setInt(1, groupId);
            stmt.setString(2, adminUsername);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next() || !rs.getBoolean("is_admin")) {
                return false; // The admin is not an admin
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Delete the participant
        String query = "DELETE FROM CHAT_GROUP_PARTICIPANTS WHERE group_id = ? AND username = ?";

        // Check if the admin is trying to remove themselves
        if (username.equals(adminUsername)) {
            return false;
        }
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, groupId);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Send a message
    public boolean sendMessageToGroup(int groupId, String senderUsername, String message) {
        String query = "INSERT INTO CHAT_MESSAGES (group_id, sender_user_username, message, sent_at) VALUES (?, ?, ?, NOW())";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, groupId);
            stmt.setString(2, senderUsername);
            stmt.setString(3, message);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Retrieve messages for a group
    public List<Message> getGroupMessages(int groupId) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT message_id, group_id, conversation_id, sender_user_username, message, sent_at FROM CHAT_MESSAGES WHERE group_id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int messageId = rs.getInt("message_id");
                int group_id = rs.getInt("group_id");
                int conversationId = rs.getInt("conversation_id");
                String senderUsername = rs.getString("sender_user_username");
                String message = rs.getString("message");
                Timestamp timestamp = rs.getTimestamp("sent_at");
                messages.add(new Message(messageId, groupId, conversationId, senderUsername, message,
                        timestamp.toLocalDateTime()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // Get group
    public Group getGroupByName(String groupName) {
        String query = "SELECT group_id, group_name FROM CHAT_GROUPS WHERE group_name = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, groupName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Group(rs.getInt("group_id"), rs.getString("group_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Give admin
    public boolean giveAdmin(int groupId, String username, String adminUsername) {
        // Check if admin is an admin
        String adminQuery = "SELECT is_admin FROM CHAT_GROUP_PARTICIPANTS WHERE group_id = ? AND username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(adminQuery)) {
            stmt.setInt(1, groupId);
            stmt.setString(2, adminUsername);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next() || !rs.getBoolean("is_admin")) {
                return false; // The admin is not an admin
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Give admin
        String query = "UPDATE CHAT_GROUP_PARTICIPANTS SET is_admin = 1 WHERE group_id = ? AND username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, groupId);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete group
    public boolean deleteGroup(int groupId, String adminUsername) {
        // Check if admin is an admin
        String adminQuery = "SELECT is_admin FROM CHAT_GROUP_PARTICIPANTS WHERE group_id = ? AND username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(adminQuery)) {
            stmt.setInt(1, groupId);
            stmt.setString(2, adminUsername);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next() || !rs.getBoolean("is_admin")) {
                return false; // The admin is not an admin
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Delete group
        String query1 = "DELETE FROM CHAT_GROUPS WHERE group_id = ?";

        // Delete group connections
        String query2 = "DELETE FROM CHAT_GROUP_PARTICIPANTS WHERE group_id = ?";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query2)) {
            stmt.setInt(1, groupId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query1)) {
            stmt.setInt(1, groupId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Change group name
    public boolean changeGroupName(int groupId, String groupName, String adminUsername) {
        // Check if admin is an admin
        String adminQuery = "SELECT is_admin FROM CHAT_GROUP_PARTICIPANTS WHERE group_id = ? AND username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(adminQuery)) {
            stmt.setInt(1, groupId);
            stmt.setString(2, adminUsername);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next() || !rs.getBoolean("is_admin")) {
                return false; // The admin is not an admin
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Change group name
        String query = "UPDATE CHAT_GROUPS SET group_name = ? WHERE group_id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, groupName);
            stmt.setInt(2, groupId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}