package engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all game achievements (including their state, unlocking logic, and persistence).
 */
public class AchievementManager {
    /** Stores the single instance of the AchievementManager. */
    private static AchievementManager instance;
    /** List of all achievements in the game. */
    private List<Achievement> achievements;

    /**
     * Private constructor to initialize the achievement list.
     * Part of the Singleton pattern.
     */
    private AchievementManager() {
        achievements = new ArrayList<>();
        achievements.add(new Achievement("Beginner", "Clear level 1"));
        achievements.add(new Achievement("Intermediate", "Clear level 3"));
        achievements.add(new Achievement("Boss Slayer", "Defeat a boss"));
        achievements.add(new Achievement("Mr. Greedy", "Have more than 2000 coins"));
        achievements.add(new Achievement("First Blood", "Defeat your first enemy"));
        achievements.add(new Achievement("Bear Grylls", "Survive for 60 seconds"));
        achievements.add(new Achievement("Bad Sniper", "Under 80% accuracy"));
        achievements.add(new Achievement("Conqueror", "Clear the final level"));
    }

    /**
     * Provides the global access point to the AchievementManager instance.
     *
     * @return The singleton instance of AchievementManager.
     */
    public static AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }

    /**
     * Synchronizes the in-memory achievement status with the given user's data.
     *
     * @param user The user whose achievements should be loaded. If null, all achievements are reset to locked.
     */
    public void syncAchievementsWithUser(User user) {
        if (user == null) {
            // No user logged in, so lock all achievements.
            for (Achievement achievement : achievements) {
                achievement.lock();
            }
        } else {
            // User is logged in, update from their achievement map.
            java.util.Map<String, Boolean> userAchievements = user.getAchievements();
            for (Achievement achievement : achievements) {
                if (userAchievements.getOrDefault(achievement.getName(), false)) {
                    achievement.unlock();
                } else {
                    achievement.lock();
                }
            }
        }
    }

    /**
     * Gets the list of all achievements.
     *
     * @return A list of all achievements.
     */
    public List<Achievement> getAchievements() {
        return achievements;
    }

    /**
     * Unlocks a specific achievement by name for the current user.
     *
     * @param name The name of the achievement to unlock.
     */
    public void unlockAchievement(String name) {
        User currentUser = Core.getCurrentUser();
        if (currentUser == null) {
            Core.getLogger().warning("Attempted to unlock achievement '" + name + "' with no user logged in.");
            return;
        }

        for (Achievement achievement : achievements) {
            if (achievement.getName().equals(name) && !achievement.isUnlocked()) {
                achievement.unlock();
                currentUser.setAchievementStatus(name, true);

                // Persist the change
                try {
                    Core.getFileManager().saveUsers(UserManager.getInstance().getUsers());
                } catch (IOException e) {
                    Core.getLogger().severe("Failed to save user data after unlocking achievement: " + e.getMessage());
                }
                break;
            }
        }
    }

    /**
     * Handles game events when an enemy is defeated to check for achievements.
     */
    public void checkKillAchievements(GameState gameState) {
        User currentUser = Core.getCurrentUser();
        if (currentUser == null || gameState == null) return;

        // First Blood Achievement
        if (!currentUser.getAchievements().getOrDefault("First Blood", false)) {
            unlockAchievement("First Blood");
        }

        // Bad Sniper Achievement
        if (!currentUser.getAchievements().getOrDefault("Bad Sniper", false) && gameState.getBulletsShot() > 5) {
            double accuracy = (double) gameState.getShipsDestroyed() / gameState.getBulletsShot();
            if (accuracy <= 0.8) {
                unlockAchievement("Bad Sniper");
            }
        }
    }

    /**
     * Handles game events related to elapsed time.
     *
     * @param elapsedSeconds The total number of seconds elapsed in the game.
     */
    public void onTimeElapsedSeconds(int elapsedSeconds) {
        User currentUser = Core.getCurrentUser();
        if (currentUser == null) return;

        if (!currentUser.getAchievements().getOrDefault("Bear Grylls", false) && elapsedSeconds >= 60) {
            unlockAchievement("Bear Grylls");
        }
    }


}
