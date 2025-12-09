package engine;

/**
 * Represents a single achievement in the game.
 */
public class Achievement {
    /** The name of the achievement. */
    private String name;
    /** A description of how to earn the achievement. */
    private String description;
    /** The unlock status of the achievement. */
    private boolean unlocked;

    /**
     * Constructor for the Achievement.
     *
     * @param name        The name of the achievement.
     * @param description A description of the achievement.
     */
    public Achievement(String name, String description) {
        this.name = name;
        this.description = description;
        this.unlocked = false;
    }

    /**
     * Gets the name of the achievement.
     *
     * @return The name of the achievement.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the description of the achievement.
     *
     * @return The description of the achievement.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if the achievement is unlocked.
     *
     * @return True if the achievement is unlocked, false otherwise.
     */
    public boolean isUnlocked() {
        return unlocked;
    }

    /**
     * Unlocks the achievement.
     */
    public void unlock() {
        this.unlocked = true;
    }

    /**
     * Locks the achievement.
     */
    public void lock() {
        this.unlocked = false;
    }
}
