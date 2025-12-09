package engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class UserManager {
    private static UserManager instance;
    private Map<String, User> users;
    private static final Logger logger = Logger.getLogger(UserManager.class.getSimpleName());

    private UserManager() {
        try {
            users = FileManager.getInstance().loadUsers();
            logger.info("User data loaded successfully.");

        } catch (IOException e) {
            logger.warning("Could not load user data (file may not exist yet): " + e.getMessage());
            users = new HashMap<>();
        }
        logger.info("Current Map Type: " + users.getClass().getName());
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User login(String username, String password) {
        User user = users.get(username);
        if (user == null) {
            // User does not exist, attempt to register
            logger.info("User '" + username + "' not found. Attempting to register new account.");
            if (register(username, password)) {
                logger.info("New account for '" + username + "' created and logged in.");
                return users.get(username); // Return the newly registered user
            } else {
                logger.warning("Failed to register new account for '" + username + "'.");
                return null; // Registration failed (e.g., already exists, though this path should be covered by user == null check)
            }
        } else {
            // User exists, check password
            if (user.getPassword().equals(password)) {
                logger.info("User '" + username + "' logged in successfully.");
                return user;
            } else {
                logger.warning("Login failed for user '" + username + "': Incorrect password.");
                return null; // Incorrect password
            }
        }
    }

    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            logger.warning("Registration failed for user '" + username + "': User already exists.");
            return false; // User already exists
        }
        User newUser = new User(username, password);
        // Initialize achievements for the new user
        for (Achievement achievement : AchievementManager.getInstance().getAchievements()) {
            newUser.setAchievementStatus(achievement.getName(), false);
        }

        users.put(username, newUser);
        try {
            FileManager.getInstance().saveUsers(users); // Save updated user list
            logger.info("User '" + username + "' registered and data saved.");
            return true;
        } catch (IOException e) {
            logger.severe("Failed to save user data after registering user '" + username + "': " + e.getMessage());
            // Optionally remove user from map if save fails to keep in-memory consistent with file
            users.remove(username);
            return false;
        }
    }

    public Map<String, User> getUsers() {
        return users;
    }
}
