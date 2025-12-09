package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Basic unit tests for AchievementManager class.
 * Tests singleton pattern and achievement list management.
 * Note: Full integration testing requires Core and User setup.
 */
@DisplayName("AchievementManager Tests")
public class AchievementManagerTest {

    private AchievementManager manager;

    @BeforeEach
    public void setUp() {
        manager = AchievementManager.getInstance();
        // Reset all achievements to locked state before each test
        manager.syncAchievementsWithUser(null);
    }

    @Test
    @DisplayName("getInstance should return singleton instance")
    public void testGetInstanceReturnsSingleton() {
        AchievementManager instance1 = AchievementManager.getInstance();
        AchievementManager instance2 = AchievementManager.getInstance();

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2, "Should return same singleton instance");
    }

    @Test
    @DisplayName("getAchievements should return non-null list")
    public void testGetAchievementsNotNull() {
        List<Achievement> achievements = manager.getAchievements();
        assertNotNull(achievements, "Achievements list should not be null");
    }

    @Test
    @DisplayName("getAchievements should contain expected achievements")
    public void testGetAchievementsContainsExpectedAchievements() {
        List<Achievement> achievements = manager.getAchievements();

        assertTrue(achievements.size() >= 5, "Should have at least 5 achievements");

        // Check for known achievement names
        boolean hasFirstBlood = false;
        boolean hasBadSniper = false;
        boolean hasBearGrylls = false;
        boolean hasConqueror = false;

        for (Achievement achievement : achievements) {
            String name = achievement.getName();
            if ("First Blood".equals(name)) hasFirstBlood = true;
            if ("Bad Sniper".equals(name)) hasBadSniper = true;
            if ("Bear Grylls".equals(name)) hasBearGrylls = true;
            if ("Conqueror".equals(name)) hasConqueror = true;
        }

        assertTrue(hasFirstBlood, "Should have 'First Blood' achievement");
        assertTrue(hasBadSniper, "Should have 'Bad Sniper' achievement");
        assertTrue(hasBearGrylls, "Should have 'Bear Grylls' achievement");
        assertTrue(hasConqueror, "Should have 'Conqueror' achievement");
    }

    @Test
    @DisplayName("Each achievement should have a name")
    public void testEachAchievementHasName() {
        List<Achievement> achievements = manager.getAchievements();

        for (Achievement achievement : achievements) {
            assertNotNull(achievement.getName(), "Achievement name should not be null");
            assertFalse(achievement.getName().trim().isEmpty(),
                    "Achievement name should not be empty");
        }
    }

    @Test
    @DisplayName("Each achievement should have a description")
    public void testEachAchievementHasDescription() {
        List<Achievement> achievements = manager.getAchievements();

        for (Achievement achievement : achievements) {
            assertNotNull(achievement.getDescription(),
                    "Achievement description should not be null");
        }
    }

    @Test
    @DisplayName("Achievement names should be unique")
    public void testAchievementNamesAreUnique() {
        List<Achievement> achievements = manager.getAchievements();
        java.util.Set<String> names = new java.util.HashSet<>();

        for (Achievement achievement : achievements) {
            String name = achievement.getName();
            assertFalse(names.contains(name),
                    "Duplicate achievement name found: " + name);
            names.add(name);
        }
    }

    @Test
    @DisplayName("getAchievements should return modifiable list")
    public void testGetAchievementsReturnsModifiableList() {
        List<Achievement> achievements = manager.getAchievements();
        int originalSize = achievements.size();

        // This tests if the list is modifiable (though modifying it is not recommended)
        assertDoesNotThrow(() -> {
            Achievement temp = new Achievement("Temp", "Temporary achievement");
            achievements.add(temp);
            achievements.remove(temp);
        });

        assertEquals(originalSize, achievements.size(),
                "Size should be restored after add/remove");
    }

    @Test
    @DisplayName("Singleton instance should persist across multiple calls")
    public void testSingletonPersistence() {
        AchievementManager instance1 = AchievementManager.getInstance();
        List<Achievement> achievements1 = instance1.getAchievements();
        int count1 = achievements1.size();

        AchievementManager instance2 = AchievementManager.getInstance();
        List<Achievement> achievements2 = instance2.getAchievements();
        int count2 = achievements2.size();

        assertEquals(count1, count2, "Achievement count should be consistent");
        assertSame(instance1, instance2, "Instances should be the same");
    }

    @Test
    @DisplayName("Achievements should initially be locked")
    public void testAchievementsInitiallyLocked() {
        List<Achievement> achievements = manager.getAchievements();

        for (Achievement achievement : achievements) {
            assertFalse(achievement.isUnlocked(),
                    "Achievement '" + achievement.getName() + "' should be locked initially");
        }
    }

    @Test
    @DisplayName("syncAchievementsWithUser with null should lock all achievements")
    public void testSyncAchievementsWithNull() {
        List<Achievement> achievements = manager.getAchievements();

        // First unlock some achievements
        if (!achievements.isEmpty()) {
            achievements.get(0).unlock();
        }

        // Sync with null user should lock all
        manager.syncAchievementsWithUser(null);

        for (Achievement achievement : achievements) {
            assertFalse(achievement.isUnlocked(),
                    "All achievements should be locked after syncing with null user");
        }
    }

    @Test
    @DisplayName("syncAchievementsWithUser should handle empty achievement map")
    public void testSyncAchievementsWithEmptyMap() {
        User user = new User("testUser", "testPass");
        // User has empty achievements map by default

        manager.syncAchievementsWithUser(user);

        List<Achievement> achievements = manager.getAchievements();
        for (Achievement achievement : achievements) {
            assertFalse(achievement.isUnlocked(),
                    "Achievements should be locked when user has empty achievement map");
        }
    }

    @Test
    @DisplayName("syncAchievementsWithUser should unlock specified achievements")
    public void testSyncAchievementsUnlocksSpecified() {
        User user = new User("testUser", "testPass");
        user.setAchievementStatus("First Blood", true);
        user.setAchievementStatus("Bad Sniper", false);

        manager.syncAchievementsWithUser(user);

        List<Achievement> achievements = manager.getAchievements();
        Achievement firstBlood = null;
        Achievement badSniper = null;

        for (Achievement achievement : achievements) {
            if ("First Blood".equals(achievement.getName())) {
                firstBlood = achievement;
            } else if ("Bad Sniper".equals(achievement.getName())) {
                badSniper = achievement;
            }
        }

        assertNotNull(firstBlood, "Should find First Blood achievement");
        assertNotNull(badSniper, "Should find Bad Sniper achievement");
        assertTrue(firstBlood.isUnlocked(), "First Blood should be unlocked");
        assertFalse(badSniper.isUnlocked(), "Bad Sniper should be locked");
    }

    @Test
    @DisplayName("Multiple syncs should update achievement state correctly")
    public void testMultipleSyncs() {
        User user1 = new User("user1", "pass1");
        user1.setAchievementStatus("First Blood", true);

        manager.syncAchievementsWithUser(user1);

        Achievement firstBlood = manager.getAchievements().stream()
                .filter(a -> "First Blood".equals(a.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(firstBlood);
        assertTrue(firstBlood.isUnlocked());

        // Sync with different user
        User user2 = new User("user2", "pass2");
        user2.setAchievementStatus("First Blood", false);

        manager.syncAchievementsWithUser(user2);

        assertFalse(firstBlood.isUnlocked(),
                "Achievement should be locked after syncing with user2");
    }

    @Test
    @DisplayName("getAchievements should return consistent reference")
    public void testGetAchievementsConsistentReference() {
        List<Achievement> list1 = manager.getAchievements();
        List<Achievement> list2 = manager.getAchievements();

        assertSame(list1, list2, "Should return same list reference");
    }

    @Test
    @DisplayName("Achievement list should contain Achievement objects")
    public void testAchievementListContainsAchievements() {
        List<Achievement> achievements = manager.getAchievements();

        for (Object obj : achievements) {
            assertTrue(obj instanceof Achievement,
                    "List should only contain Achievement objects");
        }
    }
}