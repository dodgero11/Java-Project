package bll;

import dao.UserDAO;
import java.sql.Date;
import java.util.List;
import java.sql.SQLException;

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

    public List<User> getFriends(String username) throws SQLException {
        return userDAO.getFriends(username);
    }
}

