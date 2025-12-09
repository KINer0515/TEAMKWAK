package screen;

import engine.GameState;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InfiniteScoreScreen Simple Test")
class InfiniteScoreScreenTest {

    private static final int SCREEN_WIDTH = 448;
    private static final int SCREEN_HEIGHT = 520;
    private static final int FPS = 60;

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Constructor Test - GameState should not be null")
    void testConstructorWithValidGameState() {
        GameState gameState = new GameState(
                1,      // level
                1000,   // score
                2,      // lives
                2,      // livesP2
                50,     // bullets shot
                30,     // ships destroyed
                100,    // coin
                false,  // isTwoPlayerMode
                false   // isAIMode [Updated]
        );

        InfiniteScoreScreen screen = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, FPS, gameState
        );

        assertNotNull(screen, "Screen object should be created");
    }

    @Test
    @DisplayName("Constructor Test - Zero Score")
    void testConstructorWithZeroScore() {
        GameState gameState = new GameState(1, 0, 3, 3, 0, 0, 0, false, false);

        InfiniteScoreScreen screen = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, FPS, gameState
        );

        assertNotNull(screen, "Should be created even with zero score");
    }

    @Test
    @DisplayName("Constructor Test - High Score")
    void testConstructorWithHighScore() {
        GameState gameState = new GameState(
                5, 99999, 1, 1, 1000, 500, 5000, false, false
        );

        InfiniteScoreScreen screen = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, FPS, gameState
        );

        assertNotNull(screen, "Should be created with high score");
    }

    @Test
    @DisplayName("Constructor Test - Zero Lives")
    void testConstructorWithZeroLives() {
        GameState gameState = new GameState(1, 500, 0, 0, 25, 10, 50, false, false);

        InfiniteScoreScreen screen = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, FPS, gameState
        );

        assertNotNull(screen, "Should be created even with zero lives");
    }

    // ==================== Screen Dimension Tests ====================

    @Test
    @DisplayName("Screen Size Test - Width")
    void testScreenWidth() {
        GameState gameState = new GameState(1, 100, 2, 2, 10, 5, 10, false, false);
        InfiniteScoreScreen screen = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, FPS, gameState
        );

        int width = screen.getWidth();

        assertEquals(SCREEN_WIDTH, width, "Screen width should match");
    }

    @Test
    @DisplayName("Screen Size Test - Height")
    void testScreenHeight() {
        GameState gameState = new GameState(1, 100, 2, 2, 10, 5, 10, false, false);
        InfiniteScoreScreen screen = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, FPS, gameState
        );

        int height = screen.getHeight();

        assertEquals(SCREEN_HEIGHT, height, "Screen height should match");
    }

    // ==================== GameState Scenario Tests ====================

    @Test
    @DisplayName("Scenario Test - Beginner (Low Score)")
    void testBeginnerScenario() {
        GameState gameState = new GameState(
                1,      // level 1
                50,     // low score
                1,      // 1 life remaining
                1,
                100,    // high bullet count
                5,      // few kills
                10,     // low coin
                false,
                false   // isAIMode
        );

        InfiniteScoreScreen screen = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, FPS, gameState
        );

        assertNotNull(screen, "Beginner scenario should be handled correctly");
    }

    @Test
    @DisplayName("Scenario Test - Expert (High Score)")
    void testExpertScenario() {
        GameState gameState = new GameState(
                10,     // level 10
                50000,  // high score
                3,      // full lives
                3,
                200,    // low bullet count
                190,    // high kills (high accuracy)
                10000,  // high coin
                false,
                false   // isAIMode
        );

        InfiniteScoreScreen screen = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, FPS, gameState
        );

        assertNotNull(screen, "Expert scenario should be handled correctly");
    }

    @Test
    @DisplayName("Scenario Test - Perfect Accuracy")
    void testPerfectAccuracyScenario() {
        GameState gameState = new GameState(
                3, 5000, 2, 2,
                50,     // 50 bullets
                50,     // 50 kills (100% accuracy)
                500, false, false
        );

        InfiniteScoreScreen screen = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, FPS, gameState
        );

        assertNotNull(screen, "Perfect accuracy scenario should be handled correctly");
    }

    @Test
    @DisplayName("Scenario Test - No Kills")
    void testNoKillsScenario() {
        GameState gameState = new GameState(
                1, 0, 0, 0,
                1000,   // 1000 bullets
                0,      // 0 kills (0% accuracy)
                0, false, false
        );

        InfiniteScoreScreen screen = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, FPS, gameState
        );

        assertNotNull(screen, "No kills scenario should be handled correctly");
    }

    // ==================== Boundary Tests ====================

    @Test
    @DisplayName("Boundary Test - Minimum Screen Size")
    void testMinimumScreenSize() {
        GameState gameState = new GameState(1, 100, 2, 2, 10, 5, 10, false, false);

        InfiniteScoreScreen screen = new InfiniteScoreScreen(1, 1, FPS, gameState);

        assertNotNull(screen, "Should be created with minimum screen size");
        assertEquals(1, screen.getWidth());
        assertEquals(1, screen.getHeight());
    }

    @Test
    @DisplayName("Boundary Test - Large Screen Size")
    void testLargeScreenSize() {
        GameState gameState = new GameState(1, 100, 2, 2, 10, 5, 10, false, false);

        InfiniteScoreScreen screen = new InfiniteScoreScreen(
                1920, 1080, FPS, gameState
        );

        assertNotNull(screen, "Should be created with large screen size");
        assertEquals(1920, screen.getWidth());
        assertEquals(1080, screen.getHeight());
    }

    @Test
    @DisplayName("Boundary Test - Minimum FPS")
    void testMinimumFPS() {
        GameState gameState = new GameState(1, 100, 2, 2, 10, 5, 10, false, false);

        InfiniteScoreScreen screen = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, 1, gameState
        );

        assertNotNull(screen, "Should be created with 1 FPS");
    }

    @Test
    @DisplayName("Boundary Test - High FPS")
    void testHighFPS() {
        GameState gameState = new GameState(1, 100, 2, 2, 10, 5, 10, false, false);

        InfiniteScoreScreen screen = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, 144, gameState
        );

        assertNotNull(screen, "Should be created with high FPS");
    }


    // ==================== Exception Tests ====================

    @Test
    @DisplayName("Exception Test - Null GameState throws NullPointerException")
    void testNullGameState() {
        GameState gameState = null;

        assertThrows(NullPointerException.class, () -> {
            new InfiniteScoreScreen(SCREEN_WIDTH, SCREEN_HEIGHT, FPS, gameState);
        }, "Should throw exception when GameState is null");
    }

    // ==================== Integration Tests ====================

    @Test
    @DisplayName("Integration Test - Multiple Screen Creation")
    void testMultipleScreenCreation() {
        GameState state1 = new GameState(1, 100, 3, 3, 10, 5, 10, false, false);
        GameState state2 = new GameState(5, 5000, 1, 1, 100, 80, 500, false, false);
        GameState state3 = new GameState(10, 50000, 0, 0, 500, 400, 10000, false, false);

        InfiniteScoreScreen screen1 = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, FPS, state1
        );
        InfiniteScoreScreen screen2 = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, FPS, state2
        );
        InfiniteScoreScreen screen3 = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, FPS, state3
        );

        assertNotNull(screen1);
        assertNotNull(screen2);
        assertNotNull(screen3);
        assertNotSame(screen1, screen2, "Screens should be distinct objects");
        assertNotSame(screen2, screen3, "Screens should be distinct objects");
    }

    @Test
    @DisplayName("Integration Test - Real Gameplay Scenario")
    void testRealGameplayScenario() {
        // Player reached level 7, 15000 score, died (0 lives), 300 bullets shot, 200 enemies killed
        GameState gameState = new GameState(
                7,      // level
                15000,  // score
                0,      // lives
                0,
                300,    // bullets
                200,    // kills
                1500,   // coin
                false,
                false   // isAIMode
        );

        InfiniteScoreScreen screen = new InfiniteScoreScreen(
                SCREEN_WIDTH, SCREEN_HEIGHT, FPS, gameState
        );

        assertNotNull(screen, "Real gameplay scenario should be handled correctly");
        assertEquals(SCREEN_WIDTH, screen.getWidth());
        assertEquals(SCREEN_HEIGHT, screen.getHeight());
    }
}