package bll;

import dao.UserDAO;
import java.sql.SQLException;
import java.util.List;

public class UserService {
    private UserDAO userDAO;

    public UserService() {
        userDAO = new UserDAO();
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

    // Get all users
    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }

    public List<User> getUsersByUsername(String username) throws SQLException {
        return userDAO.getUsersByUsername(username);
    }
    
    public List<String[]> getLoginHistory(String username) throws SQLException {
        //return userDAO.getLoginHistory(username);
        return null;
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
}


