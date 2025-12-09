package engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class User {
    private String username;
    private String password;
    private Map<String, Boolean> achievements;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.achievements = new HashMap<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    public Map<String, Boolean> getAchievements() {
        return Collections.unmodifiableMap(achievements);
    }
    public void setAchievementStatus(String name, boolean unlocked) {
        achievements.put(name, unlocked);
    }
}