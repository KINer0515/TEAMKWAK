package engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class UserManager {
    private static UserManager instance;
    private Map<String, User> users;
    private static final Logger logger = Logger.getLogger(UserManager.class.getSimpleName());

    /**
     * Initializes the UserManager singleton by loading persisted user data into the internal map.
     *
     * <p>If persisted data cannot be read, initializes an empty user map so the manager remains usable.</p>
     */
    private UserManager() {
        try {
            users = FileManager.getInstance().loadUsers();
            logger.info("User data loaded successfully.");
        } catch (IOException e) {
            // If file doesn't exist or is unreadable, start with an empty user map.
            // This is safer than auto-creating a default user and overwriting a potentially corrupted file.
            logger.warning("Could not load user data (file may not exist yet): " + e.getMessage());
            users = new HashMap<>();
        }
    }

    /**
     * Get the singleton UserManager instance.
     *
     * @return the singleton UserManager instance
     */
    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /**
     * Authenticate a user by username and password, auto-registering a new account if the username does not exist.
     *
     * @param username the username to authenticate or register
     * @param password the password to verify for an existing user or assign to a newly registered user
     * @return the authenticated or newly registered {@code User} on success, {@code null} on failure
     */
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

    /**
     * Register a new user with the given username and password, initialize their achievements to false, and persist the updated user list.
     *
     * If a user with the same username already exists the method returns `false`. If persistence fails after adding the user, the in-memory addition is reverted and the method returns `false`.
     *
     * @return `true` if the user was created and saved successfully, `false` otherwise.
     */
    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            logger.warning("Registration failed for user '" + username + "': User already exists.");
            return false; // User already exists
        }
        User newUser = new User(username, password);
        // Initialize achievements for the new user
        for (Achievement achievement : AchievementManager.getInstance().getAchievements()) {
            newUser.getAchievements().put(achievement.getName(), false);
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

    /**
     * Get the internal map of users managed by this singleton.
     *
     * @return the live Map from username to User; modifying the returned map will modify the manager's internal state
     */
    public Map<String, User> getUsers() {
        return users;
    }
}