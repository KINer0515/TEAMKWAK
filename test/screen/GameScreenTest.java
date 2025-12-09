package screen;

import engine.GameState;
import engine.dto.StatePacket;
import engine.level.ItemDrop;
import engine.level.Level;
import entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GameScreenTest {

    private GameScreen gameScreen;
    int ScreenX = 448;
    int ScreenY = 520;

    @BeforeEach
    void setUp() {
        // fresh GameScreen for each test
        gameScreen = createDummyGameScreen();

        // Enemy formation (basic setup so enemies are present)
        Level level = gameScreen.getCurrentLevel();
        EnemyShipFormation enemyShipFormation = new EnemyShipFormation(level);
        enemyShipFormation.attach(gameScreen);
        gameScreen.setEnemyShipFormation(enemyShipFormation);

        // Bullets
        Set<Bullet> bullets = new HashSet<>();
        Bullet bullet = new Bullet(100, 100, 1);  // adjust ctor if needed
        bullets.add(bullet);
        gameScreen.setBullets(bullets);

        // Items
        Set<DropItem> dropItems = new HashSet<>();
        DropItem dropItem = new DropItem(200, 200, 1, DropItem.ItemType.Heal);
        dropItems.add(dropItem);
        gameScreen.setDropItems(dropItems);

        // Boss bullets (can be empty)
        Set<BossBullet> bossBullets = new HashSet<>();
        gameScreen.setBossBullets(bossBullets);

        // Boss (if GameScreen has this setter; adjust method name if different)
        FinalBoss finalBoss = new FinalBoss(300, 50, ScreenX, ScreenY);
        gameScreen.setFinalBoss(finalBoss);

        // Score (if GameScreen exposes this via GameState)
        gameScreen.setScoreP2(500);
    }

    /**
     * 1. Check P2: coordinates/HP are included in the packet, Check if coordinates/HP are correct
     */
    @Test
    void testP2StateMappedToPacket() {
        // given
        Ship p2 = new Ship(0, 0, Color.MAGENTA);

        int expectedX = 100;
        int expectedY = 200;
        int expectedHp = 2;

        p2.setPositionX(expectedX);
        p2.setPositionY(expectedY);
        gameScreen.setShipP2(p2);
        gameScreen.setLivesP2(expectedHp);

        // when
        StatePacket packet = gameScreen.buildStatePacket();

        // then
        assertNotNull(packet, "StatePacket should not be null");
        assertEquals(expectedX, packet.playerX, "playerX should use P2 position");
        assertEquals(expectedY, packet.playerY, "playerY should use P2 position");
        assertEquals(expectedHp, packet.playerHp, "playerHp should use P2 lives");
    }

    /**
     * 2. Check Bullet: bullets are included in the packet, Check if the coordinates/numbers are correct
     */
    @Test
    void testBulletsMappedToPacket() {
        // when
        StatePacket packet = gameScreen.buildStatePacket();

        // then
        assertNotNull(packet.bullets, "bullets list should not be null");
        assertFalse(packet.bullets.isEmpty(), "bullets list should not be empty");

        // We expect exactly one bullet that we set in setUp()
        assertEquals(1, packet.bullets.size(), "bullet count should match");

        // Assuming format [x, y, ownerId] for each bullet entry
        var firstBullet = packet.bullets.get(0);
        assertEquals(100, firstBullet.get(0), "bullet X should match");
        assertEquals(100, firstBullet.get(1), "bullet Y should match");
        // ownerId check is optional if mapping differs
    }

    /**
     * 3. Check Enemy: Enemies are included in the packet, Check if the coordinates/HP/Type are correct
     */
    @Test
    void testEnemiesMappedToPacket() {
        // when
        StatePacket packet = gameScreen.buildStatePacket();

        // then
        assertNotNull(packet.enemies, "enemies list should not be null");
        assertFalse(packet.enemies.isEmpty(), "enemies list should not be empty");
        // Example: [x, y, hp, type]
        var firstEnemy = packet.enemies.get(0);
        assertEquals(4, firstEnemy.size(), "enemy entry should have 4 fields (x, y, hp, type)");
    }

    /**
     * 4. Check DropItems: DropItems are included in the packet, Check if the coordinates/Type are correct
     */
    @Test
    void testItemsMappedToPacket() {
        // when
        StatePacket packet = gameScreen.buildStatePacket();

        // then
        assertNotNull(packet.items, "items list should not be null");
        assertFalse(packet.items.isEmpty(), "items list should not be empty");

        // Assuming each item is encoded as [x, y, type]

        //It shows same value but it
        var firstItem = packet.items.get(0);
        //In item, values change to String
        assertEquals("200", firstItem.get(0), "item X should match");
        assertEquals("200", firstItem.get(1), "item Y should match");
        assertEquals("Heal", firstItem.get(2), "itemType should match");

        // type index or value can be asserted if stable
    }

    /**
     * 5. Check score: score is included in the packet, Check if the score is correct
     */
    @Test
    void testScoreMappedToPacket() {
        // given
        gameScreen.setScoreP2(777);

        // when
        StatePacket packet = gameScreen.buildStatePacket();

        // then
        assertEquals(777, packet.score, "packet.score should match GameState score");
    }

    /**
     * Helper method to create a minimal GameScreen instance for tests.
     */
    private GameScreen createDummyGameScreen() {
        GameState gameState = new GameState(1,0,3,3,0,0,0,true,true);
        Level level = new Level(1, 200, 100, 1, 10);
        return new GameScreen(gameState, level, false, 3, ScreenX, ScreenY, 60);
    }
}