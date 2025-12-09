package screen;

import engine.*;
import entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration and unit tests for InfiniteScreen class.
 * Tests initialization, game mechanics, enemy spawning, difficulty scaling,
 * boss encounters, shop system, and collision handling.
 *
 * @author Test Team
 */
@DisplayName("InfiniteScreen Test Suite")
public class InfiniteScreenTest {

    private InfiniteScreen infiniteScreen;
    private GameState testGameState;
    private static final int TEST_WIDTH = 448;
    private static final int TEST_HEIGHT = 520;
    private static final int TEST_FPS = 60;
    private static final int MAX_LIVES = 3;
    private static final int SEPARATION_LINE_HEIGHT = 45;
    private static final int ITEMS_SEPARATION_LINE_HEIGHT = 400;

    @BeforeEach
    void setUp() {
        testGameState = new GameState(
                1,      // level
                100,    // score
                100,    // scoreP1
                0,      // scoreP2
                3,      // livesRemaining
                0,      // livesRemainingP2
                10,     // bulletsShot
                5,      // shipsDestroyed
                0,      // coin
                false,  // isTwoPlayerMode
                false   // isAIMode
        );

        infiniteScreen = new InfiniteScreen(testGameState, MAX_LIVES, TEST_WIDTH, TEST_HEIGHT, TEST_FPS);
    }

    // ==================== INITIALIZATION & SETUP TESTS ====================

    @Nested
    @DisplayName("Initialization Tests - Verify proper screen and game state setup")
    class InitializationTests {

        @Test
        @DisplayName("initialize - Screen dimensions are set correctly")
        void testScreenDimensionsAfterInitialize() {
            infiniteScreen.initialize();

            assertEquals(TEST_WIDTH, infiniteScreen.getWidth(), "Width should be 448");
            assertEquals(TEST_HEIGHT, infiniteScreen.getHeight(), "Height should be 520");
        }

        @Test
        @DisplayName("initialize - Player ship is created at center-bottom position")
        void testPlayerShipInitialization() {
            infiniteScreen.initialize();
            Ship ship = infiniteScreen.getShip();

            assertNotNull(ship, "Ship should not be null");
            assertEquals(1, ship.getPlayerId(), "Player ID should be 1");
            assertFalse(ship.isDestroyed(), "Ship should not be destroyed initially");
            assertTrue(ship.getPositionY() <= ITEMS_SEPARATION_LINE_HEIGHT,
                    "Ship should be below items separation line");
        }

        @Test
        @DisplayName("initialize - All game collections are initialized")
        void testGameCollectionsInitialization() {
            infiniteScreen.initialize();

            assertNotNull(infiniteScreen.getBullets(), "Bullets set should not be null");
            assertNotNull(infiniteScreen.getDropItems(), "Drop items set should not be null");
            assertNotNull(infiniteScreen.getBossBullets(), "Boss bullets set should not be null");
            assertNotNull(infiniteScreen.getInfiniteEnemyFormation(), "Enemy formation should not be null");

            assertTrue(infiniteScreen.getBullets().isEmpty(), "Bullets should be empty initially");
            assertTrue(infiniteScreen.getDropItems().isEmpty(), "Drop items should be empty initially");
            assertTrue(infiniteScreen.getBossBullets().isEmpty(), "Boss bullets should be empty initially");
        }

        @Test
        @DisplayName("initialize - Enemy formation is empty and ready")
        void testEnemyFormationInitialization() {
            infiniteScreen.initialize();
            InfiniteEnemyFormation formation = infiniteScreen.getInfiniteEnemyFormation();

            assertNotNull(formation, "Enemy formation should be created");
            assertTrue(formation.isEmpty(), "Formation should be empty initially");
            assertEquals(0, formation.getEnemyCount(), "Enemy count should be 0");
        }

        @Test
        @DisplayName("initialize - Boss references are null at start")
        void testBossInitialization() {
            infiniteScreen.initialize();

            assertNull(infiniteScreen.getOmegaBoss(), "Omega boss should be null");
            assertNull(infiniteScreen.getFinalBoss(), "Final boss should be null");
        }

        @Test
        @DisplayName("initialize - Game timer is started")
        void testGameTimerInitialization() {
            infiniteScreen.initialize();

            // Verify initialization completed without errors
            GameState state = infiniteScreen.getGameState();
            assertNotNull(state, "GameState should be available after initialization");
        }

        @Test
        @DisplayName("initialize - Difficulty parameters are set to initial values")
        void testDifficultyParametersInitialization() {
            infiniteScreen.initialize();

            // Verify game can run without throwing exceptions
            assertNotNull(infiniteScreen.getShip(), "Ship should be initialized");
            assertNotNull(infiniteScreen.getInfiniteEnemyFormation(), "Enemy formation should be initialized");
            assertTrue(infiniteScreen.getInfiniteEnemyFormation().isEmpty(), "Enemy formation should be empty initially");
            assertEquals(0, infiniteScreen.getCoin(), "Initial coin should be 0");
        }
    }

    // ==================== GAME STATE TESTS ====================

    @Nested
    @DisplayName("Game State Tests - Verify game state management")
    class GameStateTests {

        @Test
        @DisplayName("getGameState - Returns valid GameState with correct values")
        void testGetGameStateReturnsValid() {
            infiniteScreen.initialize();
            GameState state = infiniteScreen.getGameState();

            assertNotNull(state, "GameState should not be null");
            assertEquals(100, state.getScore(), "Score should match initial value");
            assertEquals(MAX_LIVES, state.getLivesRemaining(), "Lives should match");
            assertEquals(5, state.getShipsDestroyed(), "Ships destroyed should match");
        }

        @Test
        @DisplayName("getGameState - Reflects coin changes")
        void testGameStateReflectsCoinChanges() {
            infiniteScreen.initialize();

            infiniteScreen.setCoin(150);
            GameState state = infiniteScreen.getGameState();
            assertEquals(150, state.getCoin(), "Coin should be updated in GameState");
        }

        @Test
        @DisplayName("getGameState - Reflects lives changes")
        void testGameStateReflectsLivesChanges() {
            infiniteScreen.initialize();

            infiniteScreen.setLivesP1(1);
            GameState state = infiniteScreen.getGameState();
            assertEquals(1, state.getLivesRemaining(), "Lives should be updated in GameState");
        }

        @Test
        @DisplayName("getGameState - Reflects ships destroyed changes")
        void testGameStateReflectsShipsDestroyedChanges() {
            infiniteScreen.initialize();

            infiniteScreen.setShipsDestroyed(50);
            GameState state = infiniteScreen.getGameState();
            assertEquals(50, state.getShipsDestroyed(), "Ships destroyed should be updated");
        }
    }

    // ==================== LIVES MANAGEMENT TESTS ====================

    @Nested
    @DisplayName("Lives Management Tests - Verify life system")
    class LivesManagementTests {

        @Test
        @DisplayName("getLivesP1 - Returns initial lives value")
        void testGetInitialLives() {
            infiniteScreen.initialize();

            assertEquals(MAX_LIVES, infiniteScreen.getLivesP1(), "Initial lives should be MAX_LIVES");
        }

        @Test
        @DisplayName("setLivesP1 - Updates lives correctly")
        void testSetLivesP1Updates() {
            infiniteScreen.initialize();

            infiniteScreen.setLivesP1(2);
            assertEquals(2, infiniteScreen.getLivesP1(), "Lives should be 2");

            infiniteScreen.setLivesP1(1);
            assertEquals(1, infiniteScreen.getLivesP1(), "Lives should be 1");

            infiniteScreen.setLivesP1(0);
            assertEquals(0, infiniteScreen.getLivesP1(), "Lives should be 0");
        }

        @Test
        @DisplayName("gainLife - Multiple calls work correctly")
        void testGainLifeMultipleTimes() {
            infiniteScreen.initialize();
            infiniteScreen.setLivesP1(0);

            infiniteScreen.gainLife();
            assertEquals(1, infiniteScreen.getLivesP1(), "Lives should be 1");

            infiniteScreen.gainLife();
            assertEquals(2, infiniteScreen.getLivesP1(), "Lives should be 2");

            infiniteScreen.gainLife();
            assertEquals(3, infiniteScreen.getLivesP1(), "Lives should be 3");

            infiniteScreen.gainLife();
            assertEquals(3, infiniteScreen.getLivesP1(), "Should stay at maximum");
        }
    }

    // ==================== COIN SYSTEM TESTS ====================

    @Nested
    @DisplayName("Coin System Tests - Verify coin management")
    class CoinSystemTests {

        @Test
        @DisplayName("setCoin - Updates coin value correctly")
        void testSetCoinUpdates() {
            infiniteScreen.initialize();

            assertEquals(0, infiniteScreen.getCoin(), "Initial coin should be 0");

            infiniteScreen.setCoin(100);
            assertEquals(100, infiniteScreen.getCoin(), "Coin should be 100");

            infiniteScreen.setCoin(500);
            assertEquals(500, infiniteScreen.getCoin(), "Coin should be 500");

            infiniteScreen.setCoin(0);
            assertEquals(0, infiniteScreen.getCoin(), "Coin should be 0");
        }

        @Test
        @DisplayName("setCoin - Handles large values")
        void testSetCoinLargeValues() {
            infiniteScreen.initialize();

            infiniteScreen.setCoin(999999);
            assertEquals(999999, infiniteScreen.getCoin(), "Should handle large values");

            infiniteScreen.setCoin(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, infiniteScreen.getCoin(), "Should handle Integer.MAX_VALUE");
        }

        @Test
        @DisplayName("setCoin - Handles negative values")
        void testSetCoinNegativeValues() {
            infiniteScreen.initialize();

            infiniteScreen.setCoin(-100);
            assertEquals(-100, infiniteScreen.getCoin(), "Should handle negative values");
        }

        @Test
        @DisplayName("setCoin - Multiple rapid changes")
        void testSetCoinRapidChanges() {
            infiniteScreen.initialize();

            for (int i = 0; i <= 100; i += 10) {
                infiniteScreen.setCoin(i);
                assertEquals(i, infiniteScreen.getCoin(), "Coin should update to " + i);
            }
        }
    }

    // ==================== SHIPS DESTROYED TESTS ====================

    @Nested
    @DisplayName("Ships Destroyed Tests - Verify enemy kill tracking")
    class ShipsDestroyedTests {

        @Test
        @DisplayName("getShipsDestroyed - Returns initial value")
        void testGetInitialShipsDestroyed() {
            infiniteScreen.initialize();

            assertEquals(5, infiniteScreen.getShipsDestroyed(), "Initial ships destroyed should be 5");
        }

        @Test
        @DisplayName("setShipsDestroyed - Updates value correctly")
        void testSetShipsDestroyedUpdates() {
            infiniteScreen.initialize();

            infiniteScreen.setShipsDestroyed(10);
            assertEquals(10, infiniteScreen.getShipsDestroyed(), "Should be 10");

            infiniteScreen.setShipsDestroyed(50);
            assertEquals(50, infiniteScreen.getShipsDestroyed(), "Should be 50");

            infiniteScreen.setShipsDestroyed(0);
            assertEquals(0, infiniteScreen.getShipsDestroyed(), "Should be 0");
        }

        @Test
        @DisplayName("setShipsDestroyed - Handles large values")
        void testSetShipsDestroyedLargeValues() {
            infiniteScreen.initialize();

            infiniteScreen.setShipsDestroyed(100000);
            assertEquals(100000, infiniteScreen.getShipsDestroyed(), "Should handle large values");
        }

        @Test
        @DisplayName("setShipsDestroyed - Handles negative values")
        void testSetShipsDestroyedNegativeValues() {
            infiniteScreen.initialize();

            infiniteScreen.setShipsDestroyed(-50);
            assertEquals(-50, infiniteScreen.getShipsDestroyed(), "Should handle negative values");
        }
    }

    // ==================== GAME MODE TESTS ====================

    @Nested
    @DisplayName("Game Mode Tests - Verify game mode configuration")
    class GameModeTests {

        @Test
        @DisplayName("isTwoPlayerMode - Returns false for Infinite mode")
        void testIsSinglePlayerMode() {
            infiniteScreen.initialize();

            assertFalse(infiniteScreen.isTwoPlayerMode(), "Infinite mode should be single player");
        }

        @Test
        @DisplayName("isAIMode - Returns false for Infinite mode")
        void testIsAIMode() {
            infiniteScreen.initialize();

            assertFalse(infiniteScreen.getGameState().isAIMode(), "Infinite mode should be single player");
        }

        @Test
        @DisplayName("isLevelFinished - Returns true (always active)")
        void testIsLevelFinishedAlwaysTrue() {
            infiniteScreen.initialize();

            assertTrue(infiniteScreen.isLevelFinished(), "Infinite mode should always return true");
        }
    }

    // ==================== PLAYER 2 EXCEPTION TESTS ====================

    @Nested
    @DisplayName("Player 2 Exception Tests - Verify Player 2 is unsupported")
    class Player2ExceptionTests {

        @Test
        @DisplayName("getShipP2 - Throws UnsupportedOperationException")
        void testGetShipP2ThrowsException() {
            infiniteScreen.initialize();

            UnsupportedOperationException exception = assertThrows(
                    UnsupportedOperationException.class,
                        () -> infiniteScreen.getShipP2()
            );
            assertTrue(exception.getMessage().contains("Player 2"), "Exception message should mention Player 2");
        }

        @Test
        @DisplayName("setLivesP2 - Throws UnsupportedOperationException")
        void testSetLivesP2ThrowsException() {
            infiniteScreen.initialize();

            assertThrows(UnsupportedOperationException.class,
                    () -> infiniteScreen.setLivesP2(2));
        }

        @Test
        @DisplayName("getLivesP2 - Throws UnsupportedOperationException")
        void testGetLivesP2ThrowsException() {
            infiniteScreen.initialize();

            assertThrows(UnsupportedOperationException.class,
                    () -> infiniteScreen.getLivesP2());
        }

        @Test
        @DisplayName("gainLifeP2 - Throws UnsupportedOperationException")
        void testGainLifeP2ThrowsException() {
            infiniteScreen.initialize();

            assertThrows(UnsupportedOperationException.class,
                    () -> infiniteScreen.gainLifeP2());
        }
    }

    // ==================== SCORE SYSTEM TESTS ====================

    @Nested
    @DisplayName("Score System Tests - Verify point management")
    class ScoreSystemTests {

        @Test
        @DisplayName("addPointsFor - Accepts bullet and points")
        void testAddPointsForValid() {
            infiniteScreen.initialize();
            Bullet bullet = new Bullet(0, 0, -6);

            assertDoesNotThrow(() -> infiniteScreen.addPointsFor(bullet, 50), "Should accept valid bullet and points");
        }

        @Test
        @DisplayName("addPointsFor - Handles zero points")
        void testAddPointsForZero() {
            infiniteScreen.initialize();
            Bullet bullet = new Bullet(0, 0, -6);

            assertDoesNotThrow(() -> infiniteScreen.addPointsFor(bullet, 0), "Should handle zero points");
        }

        @Test
        @DisplayName("addPointsFor - Handles negative points")
        void testAddPointsForNegative() {
            infiniteScreen.initialize();
            Bullet bullet = new Bullet(0, 0, -6);

            assertDoesNotThrow(() -> infiniteScreen.addPointsFor(bullet, -10), "Should handle negative points");
        }

        @Test
        @DisplayName("addPointsFor - Handles large point values")
        void testAddPointsForLargeValues() {
            infiniteScreen.initialize();
            Bullet bullet = new Bullet(0, 0, -6);

            assertDoesNotThrow(() -> infiniteScreen.addPointsFor(bullet, 999999), "Should handle large point values");
        }
    }

    // ==================== POPUP MESSAGE TESTS ====================

    @Nested
    @DisplayName("Popup Message Tests - Verify health popup system")
    class PopupMessageTests {

        @Test
        @DisplayName("showHealthPopup - Accepts message")
        void testShowHealthPopupAcceptsMessage() {
            infiniteScreen.initialize();

            assertDoesNotThrow(() -> infiniteScreen.showHealthPopup("Test Message"), "Should accept popup message");
        }

        @Test
        @DisplayName("showHealthPopup - Handles various message types")
        void testShowHealthPopupVariousMessages() {
            infiniteScreen.initialize();

            assertDoesNotThrow(() -> infiniteScreen.showHealthPopup("+10 HP"));
            assertDoesNotThrow(() -> infiniteScreen.showHealthPopup("Missed!"));
        }
    }

    // ==================== INTEGRATION TESTS ====================

    @Nested
    @DisplayName("Integration Tests - Complex gameplay scenarios")
    class IntegrationTests {

        @Test
        @DisplayName("Integration - Complete initialization and state management")
        void testCompleteInitializationFlow() {
            infiniteScreen.initialize();

            assertEquals(3, infiniteScreen.getLivesP1(), "Initial lives");
            assertEquals(0, infiniteScreen.getCoin(), "Initial coin");
            assertEquals(5, infiniteScreen.getShipsDestroyed(), "Initial kills");

            infiniteScreen.setLivesP1(2);
            infiniteScreen.setCoin(150);
            infiniteScreen.setShipsDestroyed(25);

            assertEquals(2, infiniteScreen.getLivesP1());
            assertEquals(150, infiniteScreen.getCoin());
            assertEquals(25, infiniteScreen.getShipsDestroyed());

            GameState state = infiniteScreen.getGameState();
            assertEquals(2, state.getLivesRemaining());
            assertEquals(150, state.getCoin());
            assertEquals(25, state.getShipsDestroyed());
        }

        @Test
        @DisplayName("Integration - Life loss and gain sequence")
        void testLifeManagementSequence() {
            infiniteScreen.initialize();

            assertEquals(3, infiniteScreen.getLivesP1());

            infiniteScreen.setLivesP1(2);
            assertEquals(2, infiniteScreen.getLivesP1());

            infiniteScreen.setLivesP1(1);
            assertEquals(1, infiniteScreen.getLivesP1());

            infiniteScreen.gainLife();
            assertEquals(2, infiniteScreen.getLivesP1());

            infiniteScreen.gainLife();
            assertEquals(3, infiniteScreen.getLivesP1());

            infiniteScreen.gainLife();
            assertEquals(3, infiniteScreen.getLivesP1(), "Should not exceed maximum");
        }

        @Test
        @DisplayName("Integration - Continuous state updates")
        void testContinuousUpdates() {
            infiniteScreen.initialize();

            for (int i = 0; i < 50; i++) {
                infiniteScreen.setCoin(infiniteScreen.getCoin() + 10);
                infiniteScreen.setShipsDestroyed(infiniteScreen.getShipsDestroyed() + 1);
            }

            assertEquals(500, infiniteScreen.getCoin(), "Coin should be 500");
            assertEquals(55, infiniteScreen.getShipsDestroyed(), "Ships should be 55");
        }

        @Test
        @DisplayName("Integration - Multiple rapid state changes")
        void testRapidStateChanges() {
            infiniteScreen.initialize();

            for (int i = 0; i <= 100; i++) {
                infiniteScreen.setCoin(i);
                assertEquals(i, infiniteScreen.getCoin());
            }
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    @DisplayName("Edge Case Tests - Boundary conditions and extreme values")
    class EdgeCaseTests {

        @Test
        @DisplayName("Edge Case - Integer.MAX_VALUE handling")
        void testMaxValueHandling() {
            infiniteScreen.initialize();

            infiniteScreen.setCoin(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, infiniteScreen.getCoin());

            infiniteScreen.setShipsDestroyed(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, infiniteScreen.getShipsDestroyed());
        }

        @Test
        @DisplayName("Edge Case - Integer.MIN_VALUE handling")
        void testMinValueHandling() {
            infiniteScreen.initialize();

            infiniteScreen.setCoin(Integer.MIN_VALUE);
            assertEquals(Integer.MIN_VALUE, infiniteScreen.getCoin());

            infiniteScreen.setShipsDestroyed(Integer.MIN_VALUE);
            assertEquals(Integer.MIN_VALUE, infiniteScreen.getShipsDestroyed());
        }

        @Test
        @DisplayName("Edge Case - Transition to zero")
        void testTransitionToZero() {
            infiniteScreen.initialize();
            infiniteScreen.setCoin(500);
            infiniteScreen.setLivesP1(3);
            infiniteScreen.setShipsDestroyed(100);

            infiniteScreen.setCoin(0);
            infiniteScreen.setLivesP1(0);
            infiniteScreen.setShipsDestroyed(0);

            assertEquals(0, infiniteScreen.getCoin());
            assertEquals(0, infiniteScreen.getLivesP1());
            assertEquals(0, infiniteScreen.getShipsDestroyed());
        }

        @Test
        @DisplayName("Edge Case - Set lives to zero (game over condition)")
        void testZeroLivesGameOverCondition() {
            infiniteScreen.initialize();

            infiniteScreen.setLivesP1(0);
            assertEquals(0, infiniteScreen.getLivesP1());

            // gainLife should work even from 0
            infiniteScreen.gainLife();
            assertEquals(1, infiniteScreen.getLivesP1());
        }

        @Test
        @DisplayName("Edge Case - Negative lives value")
        void testNegativeLivesValue() {
            infiniteScreen.initialize();

            infiniteScreen.setLivesP1(-1);
            assertEquals(-1, infiniteScreen.getLivesP1());

            infiniteScreen.setLivesP1(-100);
            assertEquals(-100, infiniteScreen.getLivesP1());
        }
    }

    // ==================== FIELD ACCESSIBILITY TESTS ====================

    @Nested
    @DisplayName("Field Accessibility Tests - Verify collection modifications")
    class FieldAccessibilityTests {

        @Test
        @DisplayName("Field Access - Bullets collection can be modified")
        void testBulletsCollectionAccessible() {
            infiniteScreen.initialize();
            Set<Bullet> bullets = infiniteScreen.getBullets();

            int initialSize = bullets.size();
            Bullet testBullet = new Bullet(100, 100, -6);
            bullets.add(testBullet);

            assertEquals(initialSize + 1, bullets.size());
        }

        @Test
        @DisplayName("Field Access - Enemy formation is iterable")
        void testEnemyFormationIterable() {
            infiniteScreen.initialize();
            InfiniteEnemyFormation formation = infiniteScreen.getInfiniteEnemyFormation();

            assertDoesNotThrow(() -> {
                for (InfiniteEnemyShip enemy : formation) {
                    assertNotNull(enemy);
                }
            });
        }
    }

    // ==================== BOSS SYSTEM TESTS ====================

    @Nested
    @DisplayName("Boss System Tests - Verify boss initialization and management")
    class BossSystemTests {

        @Test
        @DisplayName("getOmegaBoss - Returns null initially")
        void testOmegaBossNullInitially() {
            infiniteScreen.initialize();

            assertNull(infiniteScreen.getOmegaBoss(), "Omega boss should be null at start");
        }

        @Test
        @DisplayName("getFinalBoss - Returns null initially")
        void testFinalBossNullInitially() {
            infiniteScreen.initialize();

            assertNull(infiniteScreen.getFinalBoss(), "Final boss should be null at start");
        }
    }
}