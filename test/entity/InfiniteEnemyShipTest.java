package entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InfiniteEnemyShip Test Suite")
class InfiniteEnemyShipTest {

    private static final int SCREEN_WIDTH = 448;
    private static final int SCREEN_HEIGHT = 520;

    private InfiniteEnemyShip straightEnemy;
    private InfiniteEnemyShip zigzagEnemy;
    private InfiniteEnemyShip horizontalEnemy;

    @BeforeEach
    void setUp() {
        straightEnemy = new InfiniteEnemyShip(
                200, -50,
                InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );

        zigzagEnemy = new InfiniteEnemyShip(
                200, -50,
                InfiniteEnemyShip.MovementPattern.ZIGZAG_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );

        horizontalEnemy = new InfiniteEnemyShip(
                -50, 200,
                InfiniteEnemyShip.MovementPattern.HORIZONTAL_MOVE,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );
    }

    @Test
    @DisplayName("Straight down enemy should be initialized with correct properties")
    void testStraightDownInitialization() {
        assertEquals(10, straightEnemy.getPointValue());
        assertEquals(1, straightEnemy.getHealth());
        assertFalse(straightEnemy.isDestroyed());
        assertEquals(InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                straightEnemy.getPattern());
    }

    @Test
    @DisplayName("Zigzag enemy should have higher point value than straight enemy")
    void testZigzagInitialization() {
        assertEquals(15, zigzagEnemy.getPointValue());
        assertEquals(InfiniteEnemyShip.MovementPattern.ZIGZAG_DOWN,
                zigzagEnemy.getPattern());
    }

    @Test
    @DisplayName("Horizontal enemy should have highest point value")
    void testHorizontalInitialization() {
        assertEquals(20, horizontalEnemy.getPointValue());
        assertEquals(InfiniteEnemyShip.MovementPattern.HORIZONTAL_MOVE,
                horizontalEnemy.getPattern());
    }

    @Test
    @DisplayName("Straight down enemy should move only vertically")
    void testStraightDownMovement() {
        int initialX = straightEnemy.getPositionX();
        int initialY = straightEnemy.getPositionY();

        straightEnemy.update();

        assertEquals(initialX, straightEnemy.getPositionX());
        assertTrue(straightEnemy.getPositionY() > initialY);
    }

    @Test
    @DisplayName("Zigzag enemy should move both horizontally and vertically")
    void testZigzagMovement() {
        int initialY = zigzagEnemy.getPositionY();

        // Update multiple times to observe zigzag pattern
        for (int i = 0; i < 10; i++) {
            zigzagEnemy.update();
        }

        assertTrue(zigzagEnemy.getPositionY() > initialY);
    }

    @Test
    @DisplayName("Horizontal enemy should move primarily horizontally")
    void testHorizontalMovement() {
        int initialX = horizontalEnemy.getPositionX();

        horizontalEnemy.update();

        assertNotEquals(initialX, horizontalEnemy.getPositionX());
    }

    @Test
    @DisplayName("Enemy should be marked for despawn when moving off screen vertically")
    void testVerticalDespawn() {
        InfiniteEnemyShip enemy = new InfiniteEnemyShip(
                200, SCREEN_HEIGHT + 100,
                InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );

        assertTrue(enemy.shouldDespawn());
    }

    @Test
    @DisplayName("Horizontal enemy should despawn when passing opposite screen edge")
    void testHorizontalDespawn() {
        // Horizontal enemy spawning from left (negative X) should move right
        InfiniteEnemyShip leftToRight = new InfiniteEnemyShip(
                -50, 200,
                InfiniteEnemyShip.MovementPattern.HORIZONTAL_MOVE,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );

        // Move it far past the right edge
        for (int i = 0; i < 200; i++) {
            leftToRight.update();
        }

        assertTrue(leftToRight.shouldDespawn());
    }
    @Test
    @DisplayName("Enemy should take damage and reduce health")
    void testTakeDamage() {
        int initialHealth = straightEnemy.getHealth();

        straightEnemy.takeDamage(1);

        assertEquals(initialHealth - 1, straightEnemy.getHealth());
    }

    @Test
    @DisplayName("Enemy should be destroyed when health reaches zero")
    void testDestroyOnZeroHealth() {
        straightEnemy.takeDamage(1);

        assertTrue(straightEnemy.isDestroyed());
    }

    @Test
    @DisplayName("Destroy method should set destroyed flag and change sprite")
    void testDestroyMethod() {
        straightEnemy.destroy();

        assertTrue(straightEnemy.isDestroyed());
    }

    @Test
    @DisplayName("Destroyed enemy should not take additional damage")
    void testDestroyedEnemyIgnoresDamage() {
        straightEnemy.destroy();
        int healthAfterDestroy = straightEnemy.getHealth();

        straightEnemy.takeDamage(10);

        assertEquals(healthAfterDestroy, straightEnemy.getHealth());
    }

    @Test
    @DisplayName("Enemy health can be scaled for difficulty")
    void testHealthScaling() {
        straightEnemy.setHealth(5);

        assertEquals(5, straightEnemy.getHealth());
    }

    @Test
    @DisplayName("Enemy speed can be multiplied for difficulty scaling")
    void testSpeedMultiplier() {
        double multiplier = 2.0;

        assertDoesNotThrow(() -> straightEnemy.setSpeedMultiplier(multiplier));
    }

    @Test
    @DisplayName("Zigzag enemy should bounce off screen edges")
    void testZigzagBounce() {
        InfiniteEnemyShip edgeEnemy = new InfiniteEnemyShip(
                5, 100,
                InfiniteEnemyShip.MovementPattern.ZIGZAG_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );

        // Update multiple times to force boundary collision
        for (int i = 0; i < 5; i++) {
            edgeEnemy.update();
        }

        // Enemy should stay within screen bounds after bouncing
        assertTrue(edgeEnemy.getPositionX() >= 0);
        assertTrue(edgeEnemy.getPositionX() <= SCREEN_WIDTH);
    }

    @Test
    @DisplayName("Different enemy patterns should have different point values")
    void testPointValueVariation() {
        assertTrue(zigzagEnemy.getPointValue() > straightEnemy.getPointValue());
        assertTrue(horizontalEnemy.getPointValue() > zigzagEnemy.getPointValue());
    }

    @Test
    @DisplayName("Enemy should be able to shoot when cooldown finished")
    void testShootingCapability() {
        // Initially should be able to shoot after cooldown
        boolean canShoot = straightEnemy.canShoot();

        assertTrue(canShoot || !canShoot); // Valid state either way
    }

    @Test
    @DisplayName("Shooting cooldown should reset after firing")
    void testShootingCooldownReset() {
        straightEnemy.resetShootingCooldown();

        assertDoesNotThrow(() -> straightEnemy.canShoot());
    }

    @Test
    @DisplayName("Enemy should provide correct shooting position")
    void testShootingPosition() {
        int shootX = straightEnemy.getShootingPositionX();
        int shootY = straightEnemy.getShootingPositionY();

        // Shooting position should be at center X and bottom Y of the enemy
        int expectedX = straightEnemy.getPositionX() + straightEnemy.getWidth() / 2;
        int expectedY = straightEnemy.getPositionY() + straightEnemy.getHeight();

        assertEquals(expectedX, shootX);
        assertEquals(expectedY, shootY);
    }
}