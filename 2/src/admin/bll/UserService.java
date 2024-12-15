package bll;

import dao.GroupDAO;
import dao.UserDAO;
import java.sql.SQLException;
import java.util.List;

public class UserService {
    private UserDAO userDAO;
    private GroupDAO groupDAO;

    public UserService() {
        userDAO = new UserDAO();
        groupDAO = new GroupDAO();
    }

    // Add a new user
    public boolean addUser(User user) {
        return userDAO.addUser(user);
    }

    // Remove a user
    public boolean removeUser(String username) {
        return userDAO.removeUser(username);
    }

    // Update user information
    public boolean updateUser(User user) {
        return userDAO.updateUser(user);
    }

    public List<User> getAllUsers(String username) throws SQLException {
        return userDAO.getAllUsers(username);
    }

    public List<User> getUsersByUsername(String username) throws SQLException {
        return userDAO.getUsersByUsername(username);
    }

    public List<User> getLoginHistory(String username) throws SQLException {
        return userDAO.getLoginHistory(username);
    }

    public boolean deactivateUserService(User user) {
        user.setIsActive("Deactivated");
        return userDAO.updateUser(user);
    }

    public boolean validateUser(String username, String password) {
        return userDAO.validateUser(username, password);
    }

    public List<User> getFriendsByUsername(String username) throws SQLException {
        return userDAO.getFriendsByUsername(username);
    }

    public boolean sendFriendRequest(String user1, String user2) throws SQLException {
        return userDAO.sendFriendRequest(user1, user2);
    }

    public List<User> getPendingFriendRequests(String username) throws SQLException {
        return userDAO.getPendingFriendRequests(username);
    }

    public boolean acceptFriendRequest(String user1, String user2) throws SQLException {
        return userDAO.acceptFriendRequest(user1, user2);
    }

    public boolean rejectFriendRequest(String user1, String user2) throws SQLException {
        return userDAO.rejectFriendRequest(user1, user2);
    }

    public boolean removeFriend(String user1, String user2) throws SQLException {
        return userDAO.removeFriend(user1, user2);
    }

    public boolean blockUser(String user1, String user2) throws SQLException {
        return userDAO.blockUser(user1, user2);
    }

    public boolean unblockUser(String user1, String user2) throws SQLException {
        return userDAO.unblockUser(user1, user2);
    }

    // Get list of conversations for a user
    public List<String> getUserConversations(String username) throws Exception {
        return userDAO.getUserConversations(username);
    }

    // Send a message in a private conversation
    public boolean sendMessage(String sender, Integer groupId, Integer conversationId, String message) throws Exception {
        if (conversationId == null && groupId == null)
            throw new IllegalArgumentException("Either groupId or conversationId must be provided.");
        return userDAO.sendMessage(sender, groupId, conversationId, message);
    }

    // Get messages in a conversation
    public List<Message> getChatMessages(Integer groupId, Integer conversationId) throws Exception {
        if (conversationId == null && groupId == null) {
            throw new IllegalArgumentException("Either groupId or conversationId must be provided.");
        }
        return userDAO.getChatMessages(groupId, conversationId);
    }

    public List<Message> getChatMessagesWith(String sender, String receiver) throws Exception {
        int conversationId = userDAO.getConversationId(sender, receiver);
        if (conversationId == -1) {
            return userDAO.createNewConversation(sender, receiver);
        }
        return userDAO.getChatMessages(null, conversationId);
    }

    public int getConversationId(String sender, String receiver) throws Exception {
        return userDAO.getConversationId(sender, receiver);
    }

    public List<Group> getGroupsForUser(String username) throws Exception {
        return groupDAO.getGroupsForUser(username);
    }

    public boolean addParticipantToGroup(int groupId, String username, boolean isAdmin) throws Exception {
        return groupDAO.addParticipantToGroup(groupId, username, isAdmin);
    }

    public boolean removeParticipantFromGroup(int groupId, String username, String adminUsername) throws Exception {
        return groupDAO.removeParticipantFromGroup(groupId, username, adminUsername);
    }

    public List<Message> getGroupMessages(int groupId) throws Exception {
        return groupDAO.getGroupMessages(groupId);
    }

    public List<User> getGroupParticipants(int groupId) throws Exception {
        return groupDAO.getGroupParticipants(groupId);
    }

    public List<User> getGroupAdmins(int groupId) throws Exception {
        return groupDAO.getGroupAdmins(groupId);
    }

    public int createGroup(String groupName, String adminUsername) throws Exception {
        return groupDAO.createGroup(groupName, adminUsername);
    }

    public int createGroupWith(String groupName, String adminUsername, String friendUsername) throws Exception {
        int groupId = groupDAO.createGroup(groupName, adminUsername);
        groupDAO.addParticipantToGroup(groupId, friendUsername, false);
        return groupId;
    }

    public Group getGroupByName(String groupName) throws Exception {
        return groupDAO.getGroupByName(groupName);
    }

    public boolean changeGroupName(int groupId, String groupName, String adminUsername) throws Exception {
        return groupDAO.changeGroupName(groupId, groupName, adminUsername);
    }

    public List<Group> getAllGroups() throws Exception {
        return groupDAO.getAllGroups();
    }

    public boolean sendMessageToGroup(int groupId, String senderUsername, String messageContent) throws Exception {
        return groupDAO.sendMessageToGroup(groupId, senderUsername, messageContent);
    }

    public boolean giveAdmin(int groupId, String username, String adminUsername) throws Exception {
        return groupDAO.giveAdmin(groupId, username, adminUsername);
    }

    public boolean deleteGroup(int groupId, String adminUsername) throws Exception {
        return groupDAO.deleteGroup(groupId, adminUsername);
    }

    public boolean deleteMessage(int messageId, String username) throws Exception {
        return userDAO.deleteMessageById(messageId, username);
    }

    public void deleteConversationById(int conversationId) throws Exception {
        userDAO.deleteConversationById(conversationId);
    }

    public List<Message> searchMessagesAcrossConversations(String username, String searchTerm) throws Exception {
        return userDAO.searchMessagesAcrossConversations(username, searchTerm);
    }

    public List<Message> searchMessagesInConversation(String username, int conversationId, String searchTerm) throws Exception {
        return userDAO.searchMessagesInConversation(username, conversationId, searchTerm);
    }

    // Set User as online
    public void UserOnline(String username) throws Exception {
        userDAO.userOnline(username);
    }

    // Set User as offline
    public void UserOffline(String username) throws Exception {
        userDAO.userOffline(username);
    }

    public List<User> getUsersAndFriends() throws Exception {
        return userDAO.getUsersAndFriends();
    }
}
