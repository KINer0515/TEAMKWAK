package engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Manages all game achievements (including their state, unlocking logic, and persistence).
 */
public class AchievementManager {
    /** Stores the single instance of the AchievementManager. */
    private static AchievementManager instance;
    /** List of all achievements in the game. */
    private List<Achievement> achievements;

    /**
     * Initializes the achievement list with the game's predefined achievements.
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
     * Aligns the manager's in-memory achievements with the provided user's saved achievement states.
     *
     * @param user the user whose achievement states should be applied; if null, all achievements are locked
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
     * Unlocks the achievement with the given name for the currently logged-in user.
     *
     * If no user is logged in, the method does nothing and logs a warning. When an
     * achievement is unlocked the achievement state is updated and the user's
     * achievement map is set to `true`; the method then attempts to persist all
     * users and logs a severe error if persistence fails.
     *
     * @param name the name of the achievement to unlock
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
                currentUser.getAchievements().put(name, true);

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
     * Evaluates the provided game state to unlock kill-related achievements for the current user.
     *
     * <p>Specifically, this checks and may unlock:
     * <ul>
     *   <li>"First Blood" — unlocked upon the user's first enemy defeat.</li>
     *   <li>"Bad Sniper" — unlocked when the user has fired more than 5 bullets and their accuracy
     *       (ships destroyed divided by bullets shot) is less than or equal to 0.8.</li>
     * </ul>
     *
     * If no user is currently logged in, the method does nothing.
     *
     * @param gameState the current game state used to evaluate kill-related achievements
     */
    public void checkKillAchievements(GameState gameState) {
        User currentUser = Core.getCurrentUser();
        if (currentUser == null) return;

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
     * Unlocks the "Bear Grylls" achievement for the current user when the provided elapsed time is 60 seconds or greater.
     *
     * @param elapsedSeconds the total number of seconds elapsed in the game
     */
    public void onTimeElapsedSeconds(int elapsedSeconds) {
        User currentUser = Core.getCurrentUser();
        if (currentUser == null) return;

        if (!currentUser.getAchievements().getOrDefault("Bear Grylls", false) && elapsedSeconds >= 60) {
            unlockAchievement("Bear Grylls");
        }
    }


}