package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Achievement class.
 * Tests achievement creation, state management, and edge cases.
 */
@DisplayName("Achievement Class Tests")
public class AchievementTest {

    private Achievement achievement;
    private static final String TEST_NAME = "Test Achievement";
    private static final String TEST_DESCRIPTION = "Test Description";

    @BeforeEach
    public void setUp() {
        achievement = new Achievement(TEST_NAME, TEST_DESCRIPTION);
    }

    @Test
    @DisplayName("Constructor should initialize achievement with name and description")
    public void testConstructorInitialization() {
        assertNotNull(achievement);
        assertEquals(TEST_NAME, achievement.getName());
        assertEquals(TEST_DESCRIPTION, achievement.getDescription());
        assertFalse(achievement.isUnlocked(), "Achievement should be locked by default");
    }

    @Test
    @DisplayName("isUnlocked should return false initially")
    public void testIsUnlockedInitiallyFalse() {
        assertFalse(achievement.isUnlocked());
    }

    @Test
    @DisplayName("getName should return correct name")
    public void testGetName() {
        assertEquals(TEST_NAME, achievement.getName());
    }

    @Test
    @DisplayName("getDescription should return correct description")
    public void testGetDescription() {
        assertEquals(TEST_DESCRIPTION, achievement.getDescription());
    }

    @Test
    @DisplayName("unlock should set unlocked to true")
    public void testUnlock() {
        assertFalse(achievement.isUnlocked());

        achievement.unlock();

        assertTrue(achievement.isUnlocked());
    }

    @Test
    @DisplayName("lock should set unlocked to false")
    public void testLock() {
        achievement.unlock();
        assertTrue(achievement.isUnlocked());

        achievement.lock();

        assertFalse(achievement.isUnlocked());
    }

    @Test
    @DisplayName("Multiple unlock calls should keep achievement unlocked")
    public void testMultipleUnlockCalls() {
        achievement.unlock();
        achievement.unlock();
        achievement.unlock();

        assertTrue(achievement.isUnlocked());
    }

    @Test
    @DisplayName("Multiple lock calls should keep achievement locked")
    public void testMultipleLockCalls() {
        achievement.unlock();
        achievement.lock();
        achievement.lock();
        achievement.lock();

        assertFalse(achievement.isUnlocked());
    }

    @Test
    @DisplayName("unlock and lock can be alternated")
    public void testUnlockLockAlternation() {
        achievement.unlock();
        assertTrue(achievement.isUnlocked());

        achievement.lock();
        assertFalse(achievement.isUnlocked());

        achievement.unlock();
        assertTrue(achievement.isUnlocked());

        achievement.lock();
        assertFalse(achievement.isUnlocked());
    }

    @Test
    @DisplayName("Should handle null name")
    public void testNullName() {
        Achievement nullNameAchievement = new Achievement(null, TEST_DESCRIPTION);
        assertNull(nullNameAchievement.getName());
        assertEquals(TEST_DESCRIPTION, nullNameAchievement.getDescription());
    }

    @Test
    @DisplayName("Should handle null description")
    public void testNullDescription() {
        Achievement nullDescAchievement = new Achievement(TEST_NAME, null);
        assertEquals(TEST_NAME, nullDescAchievement.getName());
        assertNull(nullDescAchievement.getDescription());
    }

    @Test
    @DisplayName("Should handle empty name")
    public void testEmptyName() {
        Achievement emptyNameAchievement = new Achievement("", TEST_DESCRIPTION);
        assertEquals("", emptyNameAchievement.getName());
    }

    @Test
    @DisplayName("Should handle empty description")
    public void testEmptyDescription() {
        Achievement emptyDescAchievement = new Achievement(TEST_NAME, "");
        assertEquals("", emptyDescAchievement.getDescription());
    }

    @Test
    @DisplayName("Should handle very long name")
    public void testVeryLongName() {
        String longName = "A".repeat(1000);
        Achievement longNameAchievement = new Achievement(longName, TEST_DESCRIPTION);
        assertEquals(1000, longNameAchievement.getName().length());
    }

    @Test
    @DisplayName("Should handle very long description")
    public void testVeryLongDescription() {
        String longDesc = "D".repeat(1000);
        Achievement longDescAchievement = new Achievement(TEST_NAME, longDesc);
        assertEquals(1000, longDescAchievement.getDescription().length());
    }

    @Test
    @DisplayName("Should handle special characters in name")
    public void testSpecialCharactersInName() {
        String specialName = "Achievement!@#$%^&*()";
        Achievement specialAchievement = new Achievement(specialName, TEST_DESCRIPTION);
        assertEquals(specialName, specialAchievement.getName());
    }

    @Test
    @DisplayName("Should handle special characters in description")
    public void testSpecialCharactersInDescription() {
        String specialDesc = "Description with <tags> & \"quotes\"";
        Achievement specialAchievement = new Achievement(TEST_NAME, specialDesc);
        assertEquals(specialDesc, specialAchievement.getDescription());
    }

    @Test
    @DisplayName("Should handle unicode characters")
    public void testUnicodeCharacters() {
        String unicodeName = "Achievement 🏆";
        String unicodeDesc = "Description with émojis 😀";
        Achievement unicodeAchievement = new Achievement(unicodeName, unicodeDesc);
        assertEquals(unicodeName, unicodeAchievement.getName());
        assertEquals(unicodeDesc, unicodeAchievement.getDescription());
    }

    @Test
    @DisplayName("Should handle newlines in name")
    public void testNewlinesInName() {
        String nameWithNewlines = "Line1\nLine2\nLine3";
        Achievement achievement = new Achievement(nameWithNewlines, TEST_DESCRIPTION);
        assertEquals(nameWithNewlines, achievement.getName());
    }

    @Test
    @DisplayName("Should handle newlines in description")
    public void testNewlinesInDescription() {
        String descWithNewlines = "Line1\nLine2\nLine3";
        Achievement achievement = new Achievement(TEST_NAME, descWithNewlines);
        assertEquals(descWithNewlines, achievement.getDescription());
    }

    @Test
    @DisplayName("Name should not be modified after construction")
    public void testNameImmutability() {
        String originalName = "Original Name";
        Achievement achievement = new Achievement(originalName, TEST_DESCRIPTION);
        assertEquals("Original Name", achievement.getName());

        // Modifying original string should not affect achievement
        originalName = "Modified Name";
        assertEquals("Original Name", achievement.getName());
    }

    @Test
    @DisplayName("Description should not be modified after construction")
    public void testDescriptionImmutability() {
        String originalDesc = "Original Description";
        Achievement achievement = new Achievement(TEST_NAME, originalDesc);
        assertEquals("Original Description", achievement.getDescription());

        // Modifying original string should not affect achievement
        originalDesc = "Modified Description";
        assertEquals("Original Description", achievement.getDescription());
    }

    @Test
    @DisplayName("Achievement with whitespace-only name")
    public void testWhitespaceOnlyName() {
        Achievement achievement = new Achievement("   ", TEST_DESCRIPTION);
        assertEquals("   ", achievement.getName());
    }

    @Test
    @DisplayName("Achievement with whitespace-only description")
    public void testWhitespaceOnlyDescription() {
        Achievement achievement = new Achievement(TEST_NAME, "   ");
        assertEquals("   ", achievement.getDescription());
    }

    @Test
    @DisplayName("Real achievement names from codebase")
    public void testRealAchievementNames() {
        Achievement firstBlood = new Achievement("First Blood", "Kill your first enemy");
        Achievement badSniper = new Achievement("Bad Sniper", "Under 80% accuracy");
        Achievement bearGrylls = new Achievement("Bear Grylls", "Survive for 60 seconds");
        Achievement bossSlayer = new Achievement("Boss Slayer", "Defeat a boss");

        assertEquals("First Blood", firstBlood.getName());
        assertEquals("Bad Sniper", badSniper.getName());
        assertEquals("Bear Grylls", bearGrylls.getName());
        assertEquals("Boss Slayer", bossSlayer.getName());

        assertFalse(firstBlood.isUnlocked());
        assertFalse(badSniper.isUnlocked());
        assertFalse(bearGrylls.isUnlocked());
        assertFalse(bossSlayer.isUnlocked());
    }
}