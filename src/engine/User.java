package engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String username;
    private String password;
    private Map<String, Boolean> achievements;

    /**
     * Creates a user with the given username and password and initializes an empty achievements map.
     *
     * @param username the user's username
     * @param password the user's password
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.achievements = new HashMap<>();
    }

    /**
     * Retrieves the user's username.
     *
     * @return the user's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Retrieve the user's password.
     *
     * @return the user's password
     */
    public String getPassword() {
        return password;
    }



    /**
     * Access the user's achievements mapping.
     *
     * @return a Map from achievement identifiers to `Boolean` where `true` indicates the achievement is unlocked and `false` indicates it is not
     */
    public Map<String, Boolean> getAchievements() {
        return achievements;
    }
}