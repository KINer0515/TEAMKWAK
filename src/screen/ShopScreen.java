package screen;

import java.awt.event.KeyEvent;
import engine.Cooldown;
import engine.Core;
import engine.GameState;
import entity.ShopItem;

/**
 * Implements the shop screen where players can purchase item upgrades.
 * Integrates with the ShopItem system from the Items Team.
 *
 * @author Currency System Team
 */
public class ShopScreen extends Screen {

    /** Milliseconds between changes in user selection. */
    private static final int SELECTION_TIME = 200;

    /** Time between changes in user selection. */
    private Cooldown selectionCooldown;

    /** Current game state containing player's coin balance. */
    private GameState gameState;

    /** Currently selected item index. */
    private int selectedItem;

    /** Currently selected level for the item. */
    private int selectedLevel;

    /** Total number of items available (5 items). */
    private static final int TOTAL_ITEMS = 5;

    /** Time in frames before auto-skipping shop during AI training (~1 second at 60 FPS). */
    private static final int AI_SKIP_FRAMES = 60;

    /** Item index constants. */
    private static final int ITEM_MULTISHOT = 0;
    private static final int ITEM_RAPID_FIRE = 1;
    private static final int ITEM_PENETRATION = 2;
    private static final int ITEM_BULLET_SPEED = 3;
    private static final int ITEM_SHIP_SPEED = 4;

    /** Price structure for each item level [item][level]. */
    private static final int[][] ITEM_PRICES = {
            {30, 60, 100},      // MultiShot: Level 1-3
            {25, 50, 75, 100, 150},  // Rapid Fire: Level 1-5
            {40, 80},           // Penetration: Level 1-2
            {35, 70, 110},      // Bullet Speed: Level 1-3
            {20, 40, 60, 80, 100}    // Ship Speed: Level 1-5
    };

    /** Item names. */
    private static final String[] ITEM_NAMES = {
            "Multi Shot",
            "Rapid Fire",
            "Penetration",
            "Bullet Speed",
            "Ship Speed"
    };

    /** Item descriptions. */
    private static final String[] ITEM_DESCRIPTIONS = {
            "Fire multiple bullets at once",
            "Shoot faster and more frequently",
            "Bullets pierce through enemies",
            "Bullets travel faster",
            "Ship moves faster"
    };

    /** Maximum levels for each item. */
    private static final int[] MAX_LEVELS = {3, 5, 2, 3, 5};

    /** Mode: 0 = selecting item, 1 = selecting level. */
    private int selectionMode;

    /** Cooldown for purchase feedback. */
    private Cooldown purchaseFeedbackCooldown;
    private String feedbackMessage;

    /** Indicates if the shop was opened between levels (true) or from the main menu (false) */
    public boolean betweenLevels;

    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param gameState
     *            Current game state with player's coin balance.
     * @param width
     *            Screen width.
     * @param height
     *            Screen height.
     * @param fps
     *            Frames per second, frame rate at which the game is run.
     */
    public ShopScreen(final GameState gameState, final int width,
                      final int height, final int fps,
                      final boolean betweenLevels) {
        super(width, height, fps);

        this.gameState = gameState;
        this.selectedItem = 0;
        this.selectedLevel = 1;
        this.selectionMode = 0;

        this.betweenLevels = betweenLevels;

        // If opened between levels : back to game, otherwise : back to menu
        this.returnCode = betweenLevels ? 2 : 1;

        this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();

        this.purchaseFeedbackCooldown = Core.getCooldown(2000);
        this.feedbackMessage = "";

        this.logger.info("Shop screen initialized with " +
                gameState.getCoin() + " coins. BetweenLevels=" + betweenLevels);
    }

    /**
     * Starts the action.
     *
     * @return Next screen code (1 = main menu).
     */
    public final int run() {
        super.run();
        return this.returnCode;
    }

    /**
     * Updates the elements on screen and checks for events.
     */

    // remaining time until auto skip
    private int aiSkipTimer = AI_SKIP_FRAMES;

    protected final void update() {
        super.update();

        if (Core.isAITraining) {
            aiSkipTimer--;
            if (aiSkipTimer <= 0) {
                this.returnCode = 5;
                this.isRunning = false;
            }
            return;
        }

        draw();

        if (this.selectionCooldown.checkFinished()
                && this.inputDelay.checkFinished()) {

            if (selectionMode == 0) {
                handleItemSelection();
            } else {
                handleLevelSelection();
            }
        }
    }


    /**
     * Handles input when selecting items.
     */
    private void handleItemSelection() {
        // Navigate up
        if (inputManager.isKeyDown(KeyEvent.VK_UP)
                || inputManager.isKeyDown(KeyEvent.VK_W)) {
            previousItem();
            this.selectionCooldown.reset();
        }

        // Navigate down
        if (inputManager.isKeyDown(KeyEvent.VK_DOWN)
                || inputManager.isKeyDown(KeyEvent.VK_S)) {
            nextItem();
            this.selectionCooldown.reset();
        }

        // Select item (enter level selection)
        if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
            if (selectedItem == TOTAL_ITEMS) {
                // Exit option selected
                this.isRunning = false;
            } else {
                // Enter level selection mode
                selectionMode = 1;
                selectedLevel = getCurrentLevel(selectedItem) + 1; // Next level
                if (selectedLevel > MAX_LEVELS[selectedItem]) {
                    selectedLevel = MAX_LEVELS[selectedItem];
                }
            }
            this.selectionCooldown.reset();
        }

        // Quick exit with ESC
        if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
            this.isRunning = false;
        }
    }

    /**
     * Handles input when selecting upgrade level.
     */
    private void handleLevelSelection() {
        // Navigate left (decrease level)
        if (inputManager.isKeyDown(KeyEvent.VK_LEFT)
                || inputManager.isKeyDown(KeyEvent.VK_A)) {
            if (selectedLevel > 1) {
                selectedLevel--;
            }
            this.selectionCooldown.reset();
        }

        // Navigate right (increase level)
        if (inputManager.isKeyDown(KeyEvent.VK_RIGHT)
                || inputManager.isKeyDown(KeyEvent.VK_D)) {
            if (selectedLevel < MAX_LEVELS[selectedItem]) {
                selectedLevel++;
            }
            this.selectionCooldown.reset();
        }

        // Confirm purchase
        if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
            purchaseItem(selectedItem, selectedLevel);
            this.selectionCooldown.reset();
        }

        // Cancel (back to item selection)
        if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
            selectionMode = 0;
            this.selectionCooldown.reset();
        }
    }

    /**
     * Shifts focus to next item.
     */
    private void nextItem() {
        if (this.selectedItem >= TOTAL_ITEMS) {
            this.selectedItem = 0;
        } else {
            this.selectedItem++;
        }
    }

    /**
     * Shifts focus to previous item.
     */
    private void previousItem() {
        if (this.selectedItem <= 0) {
            this.selectedItem = TOTAL_ITEMS;
        } else {
            this.selectedItem--;
        }
    }

    /**
     * Purchases an item upgrade.
     *
     * @param itemIndex Index of the item.
     * @param level Level to purchase.
     */
    private void purchaseItem(final int itemIndex, final int level) {
        int currentLevel = getCurrentLevel(itemIndex);
        int price = ITEM_PRICES[itemIndex][level - 1];

        // Check if already at or above this level
        if (currentLevel >= level) {
            feedbackMessage = "Already owned!";
            purchaseFeedbackCooldown.reset();
            logger.info("Purchase denied: Item already at level " + level);
            return;
        }

        else if (currentLevel + 1 < level) {
            feedbackMessage = "Need prior level";
            purchaseFeedbackCooldown.reset();
            logger.info("Purchase denied: " + level + 1 + " level not purchased.");
            return;
        }

        // Check if player has enough coins
        if (gameState.getCoin() < price) {
            feedbackMessage = "Not enough coins!";
            purchaseFeedbackCooldown.reset();
            logger.info("Purchase denied: Not enough coins. Need " + price +
                    ", have " + gameState.getCoin());
            return;
        }

        // Deduct coins
        gameState.deductCoins(price);

        // Apply upgrade
        boolean success = applyUpgrade(itemIndex, level);

        if (success) {
            feedbackMessage = "Purchased " + ITEM_NAMES[itemIndex] +
                    " Level " + level + "!";
            purchaseFeedbackCooldown.reset();
            logger.info("Purchased " + ITEM_NAMES[itemIndex] +
                    " Level " + level + " for " + price + " coins. " +
                    "Remaining: " + gameState.getCoin());

            // Return to item selection
            selectionMode = 0;
        } else {
            // Refund if upgrade failed
            gameState.addCoins(price);
            feedbackMessage = "Purchase failed!";
            purchaseFeedbackCooldown.reset();
            logger.warning("Failed to apply upgrade");
        }
    }

    /**
     * Applies the upgrade to the ShopItem system.
     *
     * @param itemIndex Index of the item.
     * @param level Level to set.
     * @return True if successful.
     */
    private boolean applyUpgrade(final int itemIndex, final int level) {
        switch (itemIndex) {
            case ITEM_MULTISHOT:
                return ShopItem.setMultiShotLevel(level);
            case ITEM_RAPID_FIRE:
                return ShopItem.setRapidFireLevel(level);
            case ITEM_PENETRATION:
                return ShopItem.setPenetrationLevel(level);
            case ITEM_BULLET_SPEED:
                return ShopItem.setBulletSpeedLevel(level);
            case ITEM_SHIP_SPEED:
                return ShopItem.setSHIPSPEED(level);
            default:
                return false;
        }
    }

    /**
     * Gets current level of an item.
     *
     * @param itemIndex Index of the item.
     * @return Current level.
     */
    private int getCurrentLevel(final int itemIndex) {
        switch (itemIndex) {
            case ITEM_MULTISHOT:
                return ShopItem.getMultiShotLevel();
            case ITEM_RAPID_FIRE:
                return ShopItem.getRapidFireLevel();
            case ITEM_PENETRATION:
                return ShopItem.getPenetrationLevel();
            case ITEM_BULLET_SPEED:
                return ShopItem.getBulletSpeedLevel();
            case ITEM_SHIP_SPEED:
                return ShopItem.getSHIPSpeedCOUNT() / 5; // Convert back to level
            default:
                return 0;
        }
    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);

        drawManager.drawShopScreen(this, gameState.getCoin(), selectedItem,
                selectionMode, selectedLevel, TOTAL_ITEMS,
                ITEM_NAMES, ITEM_DESCRIPTIONS, ITEM_PRICES,
                MAX_LEVELS, this);

        // Draw feedback message
        if (!purchaseFeedbackCooldown.checkFinished()) {
            drawManager.drawShopFeedback(this, feedbackMessage);
        }

        drawManager.completeDrawing(this);
    }

    /**
     * Gets current level for a specific item (for drawing).
     *
     * @param itemIndex Index of the item.
     * @return Current level.
     */
    public int getItemCurrentLevel(final int itemIndex) {
        return getCurrentLevel(itemIndex);
    }
}