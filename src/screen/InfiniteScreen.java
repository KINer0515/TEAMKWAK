package screen;

import engine.*;
import engine.GameState;
import engine.level.Level;
import entity.*;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.Random;
import java.awt.event.KeyEvent;

public class InfiniteScreen extends Screen implements CollisionContext {

    // ==================== Game State Fields ====================
    /** Player lives */
    private int lives;
    /** Player score. */
    private int score;
    /** Gotten coin. */
    private int coin;
    /** Total bullets shot by the player. */
    private int bulletsShot;
    /** Total ships destroyed by the player. */
    private int shipsDestroyed;
    /** Maximum lives player can have */
    private int maxLives;
    /** Get game information. */
    private GameState gameState;

    // ==================== Entity Fields ====================
    /** Player object. */
    private Ship ship;
    /** Omega boss */
    private MidBoss omegaBoss;
    /** Final boss */
    private FinalBoss finalBoss;
    /** Custom formation manager for Infinite Mode enemies */
    private InfiniteEnemyFormation enemyManager;

    // ==================== Bullet & Item Fields ====================
    /** Set of all bullets fired by on-screen ships. */
    private Set<Bullet> bullets;
    /** bossBullets carry bullets which Boss fires */
    private Set<BossBullet> bossBullets;
    /** Set of all dropItems dropped by on screen ships. */
    private Set<DropItem> dropItems;

    // ==================== Time Management Fields ====================
    /** Timer to track elapsed time in infinite mode */
    private GameTimer gameTimer;
    /** Elapsed time since the game started. */
    private long elapsedTime;
    /** Timestamp of the last score update. */
    private long lastScoreAdded;

    // Boss spawn timing
    /** Timestamp of the last boss spawn time. */
    private long lastBossSpawnTime = 0;

    // Enemy spawn timing
    /** Current interval between enemy spawns in milliseconds. */
    private int currentSpawnInterval;
    /** Cooldown for enemy spawn timing. */
    private Cooldown enemySpawnCooldown;
    /** Cooldown for difficulty increase timing. */
    private Cooldown difficultyIncreaseCooldown;

    // Game Start Time and Delay
    private long gameStartTime;
    private Cooldown inputDelay;

    // ==================== Enemyhship Speed Fields ====================
    /** Current speed multiplier for enemies based on elapsed time. */
    private double currentSpeedMultiplier;

    // ==================== Shop Fields ====================
    /** Whether shop is currently open. */
    private boolean isShopOpen;
    /** Cooldown for shop toggle. */
    private Cooldown shopToggleCooldown;
    /** Selected item in shop */
    private int selectedShopItem = 0;
    /** Shop selection cooldown */
    private Cooldown shopSelectionCooldown;

    // ==================== UI Fields ====================
    /** Text for health change popup display. */
    private String healthPopupText;
    /** Cooldown for health popup display duration. */
    private Cooldown healthPopupCooldown;
    /** Check Boss exists or not in screen */
    private boolean bossActive = false;
    /** Whether boss has appeared. */
    private boolean bossSpawned;
    /** Is the bullet on the screen erased */
    private boolean is_cleared = false;

    // ==================== Collision Manager ====================
    /** Manages collisions between entities. */
    private CollisionManager collisionManager;

    // ==================== Utility Fields ====================
    /** Random generator */
    private Random random;

    // ==================== Constants ====================
    /** Height of the items separation line (above items). */
    private static final int ITEMS_SEPARATION_LINE_HEIGHT = 400;
    private static final int SEPARATION_LINE_HEIGHT = 45;
    private static final int INPUT_DELAY = 6000;
    // Enemy spawn constants
    /** EnemyShip spawn interval :
     * every 10 seconds, enemyship spawn time is reduced by 0.1 seconds from 1.2 second to minimum 0.2 seconds
     * Starts at INITIAL_SPAWN_INTERVAL reduced once by SPAWN_INTERVAL_DECREASE.*/
    private static final int INITIAL_SPAWN_INTERVAL = 1300;
    private static final int MIN_SPAWN_INTERVAL = 200;
    private static final int SPAWN_INTERVAL_DECREASE = 100;
    private static final int SPAWN_INTERVAL_DECREASE_TIME = 10000;

    // Boss spawn constant
    /** Number of times the boss has spawned */
    private int BOSS_SPAWN_COUNT = 0;
    /** Boss spawn interval: 90 second(90000 milliseconds) */
    private static final int BOSS_SPAWN_INTERVAL = 90000;

    // Speed scaling constants
    private static final double INITIAL_SPEED_MULTIPLIER = 0.5;
    private static final double MAX_SPEED_MULTIPLIER = 4.0;
    private static final long SPEED_INCREASE_INTERVAL = 15000;

    // Score constants
    private static final int TIME_INTERVAL = 1000;
    private static final int POINTS_PER_SECOND = 10;

    // Shop constants
    /** Maximum levels for each item. */
    private static final int[] MAX_LEVELS = {3, 5, 2, 3, 5};
    /** Item levels */
    private int[] itemlevel = {
            0,                          // MultiShot: Max Level 3
            0,                          // Rapid Fire: Max Level 5
            0,                          // Penetration: Max Level 2
            0,                          // Bullet Speed: MAx Level 3
            0                           // Ship Speed: Max Level 5
    };
    /** Item prices */
    private int[][] prices = {
            {30, 60, 100},              // MultiShot: Level 1-3
            {15, 25, 40, 60, 80},       // Rapid Fire: Level 1-5
            {30, 60},                   // Penetration: Level 1-2
            {20, 40, 60},               // Bullet Speed: Level 1-3
            {15, 30, 50, 75, 100}       // Ship Speed: Level 1-5
    };

    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param maxLives Maximum number of lives.
     * @param width  Screen width.
     * @param height Screen height.
     * @param fps    Frames per second, frame rate at which the game is run.
     */
    public InfiniteScreen(final GameState gameState,
                          final int maxLives,
                          final int width, final int height, final int fps) {
        super(width, height, fps);

        this.maxLives = maxLives;
        this.score = gameState.getScore();
        this.coin = gameState.getCoin();
        this.lives = gameState.getLivesRemaining();
        this.gameState = gameState;
        this.bulletsShot = gameState.getBulletsShot();
        this.shipsDestroyed = gameState.getShipsDestroyed();
        this.gameTimer = new GameTimer();
        this.random = new Random();
        this.currentSpeedMultiplier = INITIAL_SPEED_MULTIPLIER;
    }

    /** Initializes basic screen properties, and adds necessary elements. */
    public void initialize() {
        super.initialize();

        this.collisionManager = new CollisionManager(this);
        this.enemyManager = new InfiniteEnemyFormation();
        this.ship = new Ship(this.width / 2, ITEMS_SEPARATION_LINE_HEIGHT - 20,Color.green);
        this.ship.setPlayerId(1);
        this.bossBullets = new HashSet<>();
        this.bullets = new HashSet<Bullet>();
        this.dropItems = new HashSet<DropItem>();

        this.currentSpawnInterval = INITIAL_SPAWN_INTERVAL;
        this.difficultyIncreaseCooldown = Core.getCooldown(SPAWN_INTERVAL_DECREASE_TIME);

        this.bossSpawned = false;
        this.lastBossSpawnTime = 0;
        BOSS_SPAWN_COUNT = 0;

        this.gameTimer = new GameTimer();
        this.elapsedTime = 0;
        this.finalBoss = null;
        this.omegaBoss = null;
        this.enemySpawnCooldown = Core.getCooldown(INITIAL_SPAWN_INTERVAL);
        this.enemySpawnCooldown.reset();

        this.shopToggleCooldown = Core.getCooldown(300);
        this.shopToggleCooldown.reset();
        this.shopSelectionCooldown = Core.getCooldown(200);
        this.shopSelectionCooldown.reset();
        this.isShopOpen = false;
        this.lastScoreAdded = System.currentTimeMillis();
        this.gameStartTime = System.currentTimeMillis();
        this.inputDelay = Core.getCooldown(INPUT_DELAY);
        this.inputDelay.reset();
    }

    /** Update game state (spawn enemies, update player, etc.) */
    protected void update() {
        super.update();
        handleShopToggle();
        // If shop is open, pause game and skip game logic
        if (isShopOpen) {
            if (this.gameTimer.isRunning()) { // Pause game timer
                this.gameTimer.stop();
            }
            handleShopInput(); // Handle shop-specific input (navigation, buy, close)
            drawInfiniteMode(); // Still draw game frame (so overlay is visible)
            return; // Skip all game logic
        }

        if (this.inputDelay.checkFinished()) {
            if (!this.gameTimer.isRunning() && !this.isShopOpen && !this.bossActive) {
                this.gameTimer.start();
                this.lastScoreAdded = System.currentTimeMillis();
            }

            if (this.gameTimer.isRunning()) {
                this.elapsedTime = this.gameTimer.getElapsedTime();
                updateSpeedMultiplier();
            }

            spawnEnemies();
            updateScore();
            updateTime();
            updateDifficulty();
            spawnBoss();

            if (!DropItem.isTimeFreezeActive()) {
                this.enemyManager.update();
            }
            this.enemyManager.shoot(this.bullets);

            if (this.lives > 0 && !this.ship.isDestroyed()) {
                boolean p1Right = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_D) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_RIGHT);
                boolean p1Left  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_A) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_LEFT);
                boolean p1Up    = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_W) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_UP);
                boolean p1Down  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_S) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_DOWN);
                boolean p1Fire  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_SPACE) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_ENTER);

                boolean isRightBorder = this.ship.getPositionX() + this.ship.getWidth() + this.ship.getSpeed() > this.width - 1;
                boolean isLeftBorder = this.ship.getPositionX() - this.ship.getSpeed() < 1;
                boolean isUpBorder = this.ship.getPositionY() - this.ship.getSpeed() < SEPARATION_LINE_HEIGHT;
                boolean isDownBorder = this.ship.getPositionY() + this.ship.getHeight() + this.ship.getSpeed() > ITEMS_SEPARATION_LINE_HEIGHT;

                if (p1Right && !isRightBorder) this.ship.moveRight();
                if (p1Left  && !isLeftBorder)  this.ship.moveLeft();
                if (p1Up    && !isUpBorder)    this.ship.moveUp();
                if (p1Down  && !isDownBorder)  this.ship.moveDown();

                if (p1Fire) {
                    if (this.ship.shoot(this.bullets)) {
                        this.bulletsShot++;
                    }
                }
            }
            this.ship.update();
        }

        cleanItems();
        collisionManager.manageCollisions();
        cleanBullets();

        if (this.lives <= 0) {
            if (this.gameTimer.isRunning()) {
                this.gameTimer.stop();
            }
            this.returnCode = 10;
            this.isRunning = false;
        }
        drawInfiniteMode();
    }
    //================== Spawn Method ==================
    /** Spawn enemies with random intervals */
    protected void spawnEnemies() {
        if (this.enemySpawnCooldown.checkFinished() && !this.bossActive) {
            this.enemySpawnCooldown.reset();
            InfiniteEnemyShip.MovementPattern pattern;
            int x = 0, y = 0;
            int typeRoll = random.nextInt(3); // 0, 1, 2 (3 types)

            switch (typeRoll) {
                case 0: // STRAIGHT_DOWN
                    pattern = InfiniteEnemyShip.MovementPattern.STRAIGHT_DOWN;
                    x = random.nextInt(this.width - 30);
                    y = -50;
                    break;

                case 1: // ZIGZAG_DOWN
                    pattern = InfiniteEnemyShip.MovementPattern.ZIGZAG_DOWN;
                    x = random.nextInt(this.width - 30);
                    y = -50;
                    break;

                case 2: // HORIZONTAL_MOVE - horizontal movement
                default:
                    pattern = InfiniteEnemyShip.MovementPattern.HORIZONTAL_MOVE;
                    int minY = SEPARATION_LINE_HEIGHT + 10;
                    int maxY = (int)(this.height * 0.3); // Upper 30% of screen
                    y = minY + random.nextInt(maxY - minY);

                    boolean startLeft = random.nextBoolean();
                    if (startLeft) {
                        x = -50; // Start from left
                    } else {
                        x = this.width + 10; // Start from right
                    }
                    break;
            }

            InfiniteEnemyShip enemy = new InfiniteEnemyShip(x, y, pattern, this.width, this.height);
            enemy.setSpeedMultiplier(this.currentSpeedMultiplier);
            // Enemy health increases every 30 seconds
            int plusHealth = (int) (this.elapsedTime / 30000);
            int newHealth = enemy.getHealth() + plusHealth;
            enemy.setHealth(newHealth);
            this.enemyManager.addEnemy(enemy);
        }
    }

    /** Spawn a boss if the conditions are met */
    protected void spawnBoss() {
        if (this.elapsedTime - this.lastBossSpawnTime >= BOSS_SPAWN_INTERVAL && !this.bossSpawned) {
            this.lastBossSpawnTime = this.elapsedTime;
            this.enemyManager.clear();
            // Increases HP by 10% every minute
            double timeMultiplier = 1.0 + (this.elapsedTime / 60000.0) * 0.1;
            if (BOSS_SPAWN_COUNT == 0) {
                BOSS_SPAWN_COUNT ++;
                this.omegaBoss = new OmegaBoss(Color.ORANGE, ITEMS_SEPARATION_LINE_HEIGHT);
                this.omegaBoss.attach(this);
                int newHp = (int) (this.omegaBoss.getMaxHp() * timeMultiplier);
                this.omegaBoss.setHealth(newHp);
                this.bossActive = true;
                this.bossSpawned = true;
                if (this.gameTimer.isRunning()) {
                    this.gameTimer.stop();
                }
                this.logger.info("Omega Boss has spawned!");
            }
            else {
                 this.finalBoss = new FinalBoss(this.width / 2 - 50, 50, this.width, this.height);
                 int newHp = (int) (this.finalBoss.getMaxHp() * timeMultiplier);
                 this.finalBoss.setHealth(newHp);
                 this.bossActive = true;
                 this.bossSpawned = true;
                if (this.gameTimer.isRunning()) {
                    this.gameTimer.stop();
                }
                this.logger.info("Final Boss has spawned!");
            }
            this.logger.info("========== BOSS SPAWNED! ==========");
            this.logger.info("Elapsed time: " + (this.elapsedTime / 1000) + " seconds");
            this.logger.info("All regular enemies have been cleared!");
            this.logger.info("===================================");
        }
    }
    //================== Method for update ==================
    /**
     * Update speed multiplier based on elapsed time.
     */
    private void updateSpeedMultiplier() {
        double timeInSeconds = this.elapsedTime / 1000.0;
        double increaseSteps = Math.floor(timeInSeconds / (SPEED_INCREASE_INTERVAL / 1000.0));

        this.currentSpeedMultiplier = INITIAL_SPEED_MULTIPLIER + (increaseSteps * 0.1);

        if (this.currentSpeedMultiplier > MAX_SPEED_MULTIPLIER) {
            this.currentSpeedMultiplier = MAX_SPEED_MULTIPLIER;
        }
    }
    private void updateBoss() {
        if (this.omegaBoss != null && !this.omegaBoss.isDestroyed()) {
            this.omegaBoss.update();
        } else if (this.omegaBoss != null && this.omegaBoss.isDestroyed()) {
            this.bossActive = false;
            this.bossSpawned = false;
            this.omegaBoss = null;
            if (!this.gameTimer.isRunning()) {
                this.gameTimer.resume();
            }
            return;
        }
        if (this.finalBoss != null && !this.finalBoss.isDestroyed()) {
            this.finalBoss.update();
            updateFinalBossBullets();
        }
        else if (this.finalBoss != null && this.finalBoss.isDestroyed()) {
            this.finalBoss = null;
            this.bossActive = false;
            this.bossSpawned = false;
            this.is_cleared = false;
            if (!this.gameTimer.isRunning()) {
                this.gameTimer.resume();
            }
        }
    }
    private void updateFinalBossBullets() {
        if (this.finalBoss.getHealPoint() > this.finalBoss.getMaxHp() / 4) {
            bossBullets.addAll(this.finalBoss.shoot1());
            bossBullets.addAll(this.finalBoss.shoot2());
        }
        else {
            if (!is_cleared) {
                bossBullets.clear();
                is_cleared = true;
                logger.info("boss is angry");
            } else {
                bossBullets.addAll(this.finalBoss.shoot3());
            }
        }
        Set<BossBullet> bulletsToRemove = new HashSet<>();

        for (BossBullet b : bossBullets) {
            b.update();
            if (b.isOffScreen(width, height)) {
                bulletsToRemove.add(b);
            }
        }
        bossBullets.removeAll(bulletsToRemove);
    }
    protected void updateTime(){
        if (this.gameTimer.isRunning()) {
            this.elapsedTime = this.gameTimer.getElapsedTime();
        }
    }
    protected void updateDifficulty(){
        if (this.bossActive) {
            updateBoss();
        }
        else {
            if (this.difficultyIncreaseCooldown.checkFinished()) {
                if (this.currentSpawnInterval > MIN_SPAWN_INTERVAL) {
                    this.difficultyIncreaseCooldown.reset();
                    this.currentSpawnInterval -= SPAWN_INTERVAL_DECREASE;
                    if (this.currentSpawnInterval < MIN_SPAWN_INTERVAL) {
                        this.currentSpawnInterval = MIN_SPAWN_INTERVAL;
                    }
                    this.enemySpawnCooldown.setMilliseconds(this.currentSpawnInterval);

                    this.logger.info("Difficulty increased! New spawn interval: " + this.currentSpawnInterval + "ms");
                }
            }
            spawnEnemies();
        }
    }
    private void updateScore() {
        if (this.gameTimer.isRunning()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastScoreAdded >= TIME_INTERVAL) {
                this.score += POINTS_PER_SECOND;
                this.lastScoreAdded = currentTime;
            }
        }
    }
    //================== Method for Shop ==================
    /**
     * Handles opening and closing the shop with T key.
     */
    private void handleShopToggle() {
        if (shopToggleCooldown.checkFinished() && inputManager.isKeyDown(KeyEvent.VK_T)) {
            if (isShopOpen) closeShop();
            else openShop();
            shopToggleCooldown.reset();
        }
    }

    /**
     * Opens the shop screen.
     */
    private void openShop() {
        isShopOpen = true;
        shopSelectionCooldown.reset();
        logger.info("Shop opened");
        if (gameTimer.isRunning()) gameTimer.stop();
    }

    /**
     * Closes the shop screen.
     */
    private void closeShop() {
        isShopOpen = false;
        logger.info("Shop closed");
        if (!gameTimer.isRunning() && !bossActive) gameTimer.resume();
    }

    private void handleShopInput() {
        if (this.shopSelectionCooldown.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
                closeShop();
                this.shopSelectionCooldown.reset();
                return;
            }
            if (inputManager.isKeyDown(KeyEvent.VK_W) || inputManager.isKeyDown(KeyEvent.VK_UP)) {
                selectedShopItem--;
                if (selectedShopItem < 0) selectedShopItem = 4;
                this.shopSelectionCooldown.reset();
            }
            if (inputManager.isKeyDown(KeyEvent.VK_S) || inputManager.isKeyDown(KeyEvent.VK_DOWN)) {
                selectedShopItem++;
                if (selectedShopItem > 4) selectedShopItem = 0;
                this.shopSelectionCooldown.reset();
            }
            if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
                attemptPurchase(selectedShopItem);
                this.shopSelectionCooldown.reset();
            }
        }
    }

    private void attemptPurchase(int itemIndex) {
        String[] names = {"Multi Shot", "Rapid Fire", "Penetration", "Bullet Speed", "Ship Speed"};

        int currentLevel = itemlevel[itemIndex];

        if (currentLevel >= MAX_LEVELS[itemIndex]) {
            this.logger.info(names[itemIndex] + " is already at max level!");
            return;
        }

        int price = prices[itemIndex][currentLevel];
        if (this.coin >= price) {
            this.coin -= price;
            this.gameState.deductCoins(price);
            itemlevel[itemIndex]++;
            applyItemUpgrade(itemIndex);

            this.logger.info("Purchased " + names[itemIndex] + " for " + price + " coins. New level: " + itemlevel[itemIndex]);
        } else {
            this.logger.info("Not enough coins for " + names[itemIndex] + ". Need: " + price + ", have: " + this.coin);
        }
    }
    private void applyItemUpgrade(int itemIndex) {
        switch (itemIndex) {
            case 0: // Multi Shot
                entity.ShopItem.setMultiShotLevel(entity.ShopItem.getMultiShotLevel() + 1);
                break;
            case 1: // Rapid Fire
                entity.ShopItem.setRapidFireLevel(entity.ShopItem.getRapidFireLevel() + 1);
                break;
            case 2: // Penetration
                entity.ShopItem.setPenetrationLevel(entity.ShopItem.getPenetrationLevel() + 1);
                break;
            case 3: // Bullet Speed
                entity.ShopItem.setBulletSpeedLevel(entity.ShopItem.getBulletSpeedLevel() + 1);
                break;
            case 4: // Ship Speed
                entity.ShopItem.setSHIPSPEED(entity.ShopItem.getSHIPSpeedCOUNT()/5 + 1);
                break;
        }
    }
    //================== Method for draw ==================
    /** Draw the current game screen */
    protected void drawInfiniteMode() {
        drawManager.initDrawing(this);

        drawManager.drawEntity(this.ship, this.ship.getPositionX(), this.ship.getPositionY());

        this.enemyManager.draw();

        drawBosses();
        drawBullets();
        drawItems();
        drawUI();
        if (!this.inputDelay.checkFinished()) {
            int countdown = (int) ((INPUT_DELAY
                    - (System.currentTimeMillis()
                    - this.gameStartTime)) / 1000);
            drawManager.drawCountDown(this, 1, countdown, false);
            drawManager.drawHorizontalLine(this, this.height / 2 - this.height / 12);
            drawManager.drawHorizontalLine(this, this.height / 2 + this.height / 12);
        }
        drawManager.completeDrawing(this);
    }
    private void drawBosses() {
        if (this.omegaBoss != null && !this.omegaBoss.isDestroyed()) {
            this.omegaBoss.draw(drawManager);
            drawManager.drawBossHealthBar(this.omegaBoss.getPositionX(), this.omegaBoss.getPositionY(), "OMEGA",
                    this.omegaBoss.getHealPoint(), this.omegaBoss.getMaxHp());
        }

        if (this.finalBoss != null && !this.finalBoss.isDestroyed()) {
            for (BossBullet bossBullet : bossBullets) {
                drawManager.drawEntity(bossBullet, bossBullet.getPositionX(), bossBullet.getPositionY());
            }
            drawManager.drawEntity(this.finalBoss, this.finalBoss.getPositionX(), this.finalBoss.getPositionY());
            drawManager.drawBossHealthBar(this.finalBoss.getPositionX(), this.finalBoss.getPositionY(), "FINAL",
                    this.finalBoss.getHealPoint(), this.finalBoss.getMaxHp());
        }
    }

    private void drawBullets() {
        for (Bullet bullet : this.bullets) {
            drawManager.drawEntity(bullet, bullet.getPositionX(), bullet.getPositionY());
        }
    }

    private void drawItems() {
        for (DropItem dropItem : this.dropItems) {
            drawManager.drawEntity(dropItem, dropItem.getPositionX(), dropItem.getPositionY());
        }
    }

    private void drawUI() {
        drawManager.drawScore(this, this.score);
        drawManager.drawLives(this, this.lives);
        drawManager.drawCoin(this, this.coin);
        drawManager.drawTime(this, this.elapsedTime);
        drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);
        drawManager.drawHorizontalLine(this, ITEMS_SEPARATION_LINE_HEIGHT);

        if (!isShopOpen) {
            drawManager.drawCenteredRegularString(this, "Press T to open shop", height - 30);
        } else {
            drawShopOverlay();
        }

        if (healthPopupText != null && !healthPopupCooldown.checkFinished()) {
            drawManager.drawHealthPopup(this, healthPopupText);
        } else {
            healthPopupText = null;
        }
    }
    private void drawShopOverlay() {
        Graphics g = drawManager.getBackBufferGraphics();
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.GREEN);
        drawManager.drawCenteredBigString(this, "SHOP", height / 8);
        drawManager.drawCenteredRegularString(this, "Coins: " + coin, height / 8 + 40);

        String[] names = {"Multi Shot", "Rapid Fire", "Penetration", "Bullet Speed", "Ship Speed"};
        int startY = height / 3;

        for (int i = 0; i < names.length; i++) {
            String text;
            if (itemlevel[i] >= MAX_LEVELS[i]) {
                text = names[i] + " - MAX LEVEL";
            } else {
                text = names[i] + " - " + prices[i][itemlevel[i]] + " coins";
            }

            if (i == selectedShopItem) {
                drawManager.drawCenteredBigString(this, "> " + text + " <", startY + i * 40);
            } else {
                drawManager.drawCenteredRegularString(this, text, startY + i * 40);
            }
        }

        drawManager.drawCenteredRegularString(this,
                "W/S: Select | SPACE: Buy | ESC/T: Close",
                height - 30);
    }
    //================== Clean Screen ==================
    /**
     * Cleans bullets that go off screen.
     */
    private void cleanBullets() {
        Set<Bullet> recyclable = new HashSet<Bullet>();
        for (Bullet bullet : this.bullets) {
            bullet.update();
            if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT || bullet.getPositionY() > this.height)
                recyclable.add(bullet);
        }
        this.bullets.removeAll(recyclable);
        BulletPool.recycle(recyclable);
    }

    /**
     * Cleans Items that go off screen.
     */
    private void cleanItems() {
        Set<DropItem> recyclable = new HashSet<DropItem>();
        for (DropItem dropItem : this.dropItems) {
            dropItem.update();
            if (dropItem.getPositionY() < SEPARATION_LINE_HEIGHT || dropItem.getPositionY() > this.height)
                recyclable.add(dropItem);
        }
        this.dropItems.removeAll(recyclable);
        ItemPool.recycle(recyclable);
    }

    @Override
    public final int run() {
        super.run();
        this.logger.info("Infinite mode ended with score: " + this.score);
        return this.returnCode;
    }

    /**
     * Returns a GameState object representing the status of the game.
     * Used to pass score to the ScoreScreen.
     */
    public final GameState getGameState() {
        return new GameState(
                0,
                this.score,
                this.lives,
                0,
                this.bulletsShot,
                this.shipsDestroyed,
                this.coin,
                false,
                false
        );
    }

    // CollisionContext interface implementations
    @Override
    public Set<Bullet> getBullets() { return this.bullets; }
    @Override
    public Set<BossBullet> getBossBullets() { return this.bossBullets; }
    @Override
    public EnemyShipFormation getEnemyShipFormation() { return null; }
    @Override
    public EnemyShipSpecialFormation getEnemyShipSpecialFormation() { return null; }
    @Override
    public InfiniteEnemyFormation getInfiniteEnemyFormation() { return this.enemyManager; }
    @Override
    public Set<DropItem> getDropItems() { return this.dropItems; }
    @Override
    public Ship getShip() { return this.ship; }
    @Override
    public void setLivesP1(int v) { this.lives = v; }
    @Override
    public int getLivesP1() { return this.lives; }
    @Override
    public void gainLife() { if (this.lives < this.maxLives) this.lives++; }
    @Override
    public MidBoss getOmegaBoss() { return this.omegaBoss; }
    @Override
    public FinalBoss getFinalBoss() { return this.finalBoss; }
    @Override
    public void addPointsFor(Bullet b, int p) { this.score += p; }
    @Override
    public void setCoin(int c) { this.coin = c; }
    @Override
    public int getCoin() { return this.coin; }
    @Override
    public void setShipsDestroyed(int shipsDestroyed) { this.shipsDestroyed = shipsDestroyed; }
    @Override
    public int getShipsDestroyed() { return this.shipsDestroyed; }
    @Override
    public Logger getLogger() { return this.logger; }
    @Override
    public Level getCurrentLevel() { return null; }
    @Override
    public void showHealthPopup(String msg) {
        this.healthPopupText = msg;
        this.healthPopupCooldown = Core.getCooldown(500);
        this.healthPopupCooldown.reset();
    }
    @Override
    public boolean isTwoPlayerMode() { return false; }
    @Override
    public boolean isLevelFinished() { return true; }
    @Override
    public Ship getShipP2() { throw new UnsupportedOperationException("Infinite mode does not support Player 2."); }
    @Override
    public void setLivesP2(int v) { throw new UnsupportedOperationException("Infinite mode does not support Player 2."); }
    @Override
    public int getLivesP2() { throw new UnsupportedOperationException("Infinite mode does not support Player 2."); }
    @Override
    public void gainLifeP2() { throw new UnsupportedOperationException("Infinite mode does not support Player 2."); }
}