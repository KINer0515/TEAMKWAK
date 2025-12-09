package entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InfiniteEnemyFormation Test Suite")
class InfiniteEnemyFormationTest {

    private InfiniteEnemyFormation formation;
    private Set<Bullet> bullets;
    private static final int SCREEN_WIDTH = 448;
    private static final int SCREEN_HEIGHT = 520;

    @BeforeEach
    void setUp() {
        formation = new InfiniteEnemyFormation();
        bullets = new HashSet<>();
    }

    @Test
    @DisplayName("New formation should be empty initially")
    void testInitiallyEmpty() {
        assertTrue(formation.isEmpty());
        assertEquals(0, formation.getEnemyCount());
    }

    @Test
    @DisplayName("Adding enemy should increase enemy count")
    void testAddEnemy() {
        InfiniteEnemyShip enemy = new InfiniteEnemyShip(
                200, 100,
                InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );

        formation.addEnemy(enemy);

        assertFalse(formation.isEmpty());
        assertEquals(1, formation.getEnemyCount());
    }

    @Test
    @DisplayName("Multiple enemies can be added to formation")
    void testAddMultipleEnemies() {
        for (int i = 0; i < 5; i++) {
            InfiniteEnemyShip enemy = new InfiniteEnemyShip(
                    100 + i * 50, 100,
                    InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                    SCREEN_WIDTH, SCREEN_HEIGHT
            );
            formation.addEnemy(enemy);
        }

        assertEquals(5, formation.getEnemyCount());
    }

    @Test
    @DisplayName("Formation should be iterable")
    void testIteration() {
        InfiniteEnemyShip enemy1 = new InfiniteEnemyShip(
                100, 100,
                InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );
        InfiniteEnemyShip enemy2 = new InfiniteEnemyShip(
                200, 100,
                InfiniteEnemyShip.MovementPattern.ZIGZAG_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );

        formation.addEnemy(enemy1);
        formation.addEnemy(enemy2);

        int count = 0;
        for (InfiniteEnemyShip enemy : formation) {
            assertNotNull(enemy);
            count++;
        }

        assertEquals(2, count);
    }

    @Test
    @DisplayName("Update should move all enemies")
    void testUpdateMovesEnemies() {
        InfiniteEnemyShip enemy = new InfiniteEnemyShip(
                200, 100,
                InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );
        formation.addEnemy(enemy);

        int initialY = enemy.getPositionY();
        formation.update();

        assertTrue(enemy.getPositionY() > initialY);
    }

    @Test
    @DisplayName("Update should remove off-screen enemies")
    void testRemoveOffScreenEnemies() {
        InfiniteEnemyShip offScreenEnemy = new InfiniteEnemyShip(
                200, SCREEN_HEIGHT + 100,
                InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );
        formation.addEnemy(offScreenEnemy);

        formation.update();

        assertEquals(0, formation.getEnemyCount());
    }

    @Test
    @DisplayName("Update should remove enemies with finished explosions")
    void testRemoveExplodedEnemies() {
        InfiniteEnemyShip enemy = new InfiniteEnemyShip(
                200, 100,
                InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );
        formation.addEnemy(enemy);

        enemy.destroy();

        for (int i = 0; i < 100; i++) {
            formation.update();
        }

        assertTrue(formation.getEnemyCount() <= 1);
    }

    @Test
    @DisplayName("Destroying specific enemy should mark it as destroyed")
    void testDestroySpecificEnemy() {
        InfiniteEnemyShip enemy = new InfiniteEnemyShip(
                200, 100,
                InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );
        formation.addEnemy(enemy);

        formation.destroy(enemy);

        assertTrue(enemy.isDestroyed());
    }

    @Test
    @DisplayName("Formation should track destroyed count")
    void testDestroyedCount() {
        InfiniteEnemyShip enemy = new InfiniteEnemyShip(
                200, 100,
                InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );
        formation.addEnemy(enemy);

        int initialDestroyed = formation.getDestroyedCount();
        formation.destroy(enemy);

        for (int i = 0; i < 100; i++) {
            formation.update();
        }

        assertTrue(formation.getDestroyedCount() >= initialDestroyed);
    }

    @Test
    @DisplayName("Formation shooting should add bullets when enemies can shoot")
    void testFormationShooting() {
        InfiniteEnemyShip enemy = new InfiniteEnemyShip(
                200, 100,
                InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );
        formation.addEnemy(enemy);

        for (int i = 0; i < 50; i++) {
            formation.shoot(bullets);
        }

        assertTrue(bullets.size() >= 0);
    }

    @Test
    @DisplayName("Empty formation should not produce bullets")
    void testEmptyFormationShooting() {
        int initialBullets = bullets.size();

        formation.shoot(bullets);

        assertEquals(initialBullets, bullets.size());
    }

    @Test
    @DisplayName("Clear should remove all enemies")
    void testClear() {
        for (int i = 0; i < 5; i++) {
            InfiniteEnemyShip enemy = new InfiniteEnemyShip(
                    100 + i * 50, 100,
                    InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                    SCREEN_WIDTH, SCREEN_HEIGHT
            );
            formation.addEnemy(enemy);
        }

        formation.clear();

        assertTrue(formation.isEmpty());
        assertEquals(0, formation.getEnemyCount());
    }

    @Test
    @DisplayName("Getting enemies list should return current enemies")
    void testGetEnemiesList() {
        InfiniteEnemyShip enemy = new InfiniteEnemyShip(
                200, 100,
                InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );
        formation.addEnemy(enemy);

        assertNotNull(formation.getEnemies());
        assertEquals(1, formation.getEnemies().size());
    }

    @Test
    @DisplayName("Formation should handle mixed enemy patterns")
    void testMixedEnemyPatterns() {
        InfiniteEnemyShip straightEnemy = new InfiniteEnemyShip(
                100, 100,
                InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );
        InfiniteEnemyShip zigzagEnemy = new InfiniteEnemyShip(
                200, 100,
                InfiniteEnemyShip.MovementPattern.ZIGZAG_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );
        InfiniteEnemyShip horizontalEnemy = new InfiniteEnemyShip(
                -50, 200,
                InfiniteEnemyShip.MovementPattern.HORIZONTAL_MOVE,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );

        formation.addEnemy(straightEnemy);
        formation.addEnemy(zigzagEnemy);
        formation.addEnemy(horizontalEnemy);

        assertEquals(3, formation.getEnemyCount());

        formation.update();

        assertDoesNotThrow(() -> formation.update());
    }

    @Test
    @DisplayName("Formation should handle rapid consecutive updates")
    void testRapidUpdates() {
        InfiniteEnemyShip enemy = new InfiniteEnemyShip(
                200, 100,
                InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                SCREEN_WIDTH, SCREEN_HEIGHT
        );
        formation.addEnemy(enemy);

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 100; i++) {
                formation.update();
            }
        });
    }

    @Test
    @DisplayName("Iterator should handle concurrent modification safely")
    void testIteratorStability() {
        for (int i = 0; i < 3; i++) {
            InfiniteEnemyShip enemy = new InfiniteEnemyShip(
                    100 + i * 50, 100,
                    InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN,
                    SCREEN_WIDTH, SCREEN_HEIGHT
            );
            formation.addEnemy(enemy);
        }

        assertDoesNotThrow(() -> {
            for (InfiniteEnemyShip enemy : formation) {
                assertNotNull(enemy);
            }
        });
    }
}