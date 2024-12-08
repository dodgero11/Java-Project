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
    public boolean addUser(String username, String fullName, String address, Date birthDate, String gender, String email) {
        return userDAO.addUser(username, fullName, address, birthDate, gender, email);
    }

    // Remove a user
    public boolean removeUser(String username) {
        return userDAO.removeUser(username);
    }

    // Update user information
    public boolean updateUser(String username, String fullName, String address, Date birthDate, String gender, String email) {
        return userDAO.updateUser(username, fullName, address, birthDate, gender, email);
    }

    // Search for users by name or username
    public List<String[]> searchUsers(String searchTerm) {
        return userDAO.searchUsers(searchTerm);
    }

    // Get all users
    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }
}

