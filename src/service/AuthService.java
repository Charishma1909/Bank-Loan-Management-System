package service;

import dao.UserDAO;
import exception.AuthenticationException;
import model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * Handles login authentication for Admin/Employee users.
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) throws AuthenticationException, SQLException {
        User user = userDAO.findByUsername(username);
        
        if (user == null) {
            throw new AuthenticationException("No account found with username: " + username);
        }
        
        if (!user.isActive()) {
            throw new AuthenticationException("This account has been deactivated. Contact the administrator.");
        }
        
        // Hash input password with SHA-256 and compare against DB
        String hashedInput = hashSHA256(password);
        if (!hashedInput.equalsIgnoreCase(user.getPassword())) {
            throw new AuthenticationException("Incorrect password.");
        }
        
        return user;
    }

    public boolean registerStaff(User user, String plainPassword) throws SQLException {
        user.setPassword(hashSHA256(plainPassword));
        user.setActive(true);
        return userDAO.insertUser(user);
    }

    /**
     * Converts a plain text password into a standard SHA-256 hex string.
     */
    private String hashSHA256(String input) {
        if (input == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error computing SHA-256 hash", e);
        }
    }
}