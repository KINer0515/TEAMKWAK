package entity;

import java.awt.Color;
import engine.Cooldown;
import engine.Core;
import engine.DrawManager.SpriteType;

/**
 * Implements enemies specifically for Infinite Mode with custom movement patterns.
 */
public class InfiniteEnemyShip extends Entity {

    /** Movement pattern types */
    public enum MovementPattern {
        STRAIGHT_DOWN,
        ZIGZAG_DOWN,
        HORIZONTAL_MOVE
    }

    /** Point values by pattern type */
    private static final int STRAIGHT_POINT_VALUE = 10;
    private static final int ZIGZAG_POINT_VALUE = 15;
    private static final int HORIZONTAL_POINT_VALUE = 20;

    /** Movement pattern of this enemy */
    private MovementPattern pattern;

    /** Screen dimensions for boundary checking */
    private int screenWidth;
    private int screenHeight;

    /** Movement speeds */
    private double speedX;
    private double speedY;

    /** Zigzag direction: 1 = right, -1 = left */
    private int zigzagDirection = 1;

    /** Default movement speeds */
    private static final double STRAIGHT_SPEED_Y = 4.0;
    private static final double ZIGZAG_SPEED_X = 3.0;
    private static final double ZIGZAG_SPEED_Y = 2.5;
    private static final double HORIZONTAL_SPEED_X = 4.5;

    /** Point value when destroyed */
    private int pointValue;

    /** Whether the enemy is destroyed */
    private boolean isDestroyed;

    /** Health points */
    private int hp;
    private int maxHp;

    private Color baseColor;

    private static final int SHOOTING_INTERVAL = 1500;

    private Cooldown shootingCooldown;

    /** Animation cooldown */
    private Cooldown animationCooldown;

    /** Explosion cooldown */
    private Cooldown explosionCooldown;

    /**
     * Constructor for Infinite Mode Enemy.
     *
     * @param positionX Initial X position
     * @param positionY Initial Y position
     * @param pattern Movement pattern type
     * @param screenWidth Screen width for boundary checking
     * @param screenHeight Screen height for boundary checking
     */
    public InfiniteEnemyShip(final int positionX, final int positionY,
                             final MovementPattern pattern,
                             final int screenWidth, final int screenHeight) {
        super(positionX, positionY, 12 * 2, 8 * 2, Color.WHITE);

        this.pattern = pattern;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.isDestroyed = false;
        this.hp = 1;
        this.maxHp = 1;

        this.animationCooldown = Core.getCooldown(500);
        this.explosionCooldown = Core.getCooldown(500);

        this.shootingCooldown = Core.getCooldown(SHOOTING_INTERVAL);
        this.shootingCooldown.reset();

        initializeByPattern();
    }

    /**
     * Initializes movement parameters and appearance based on pattern.
     */
    private void initializeByPattern() {
        switch (pattern) {
            case STRAIGHT_DOWN:
                this.speedX = 0;
                this.speedY = STRAIGHT_SPEED_Y;
                this.spriteType = SpriteType.EnemyShipA1;
                this.baseColor = Color.BLUE;
                this.color = this.baseColor;
                this.pointValue = STRAIGHT_POINT_VALUE;
                break;

            case ZIGZAG_DOWN:
                this.speedX = ZIGZAG_SPEED_X;
                this.speedY = ZIGZAG_SPEED_Y;
                this.spriteType = SpriteType.EnemyShipB1;
                this.baseColor = Color.YELLOW;
                this.color = this.baseColor;
                this.pointValue = ZIGZAG_POINT_VALUE;
                // Random initial zigzag direction
                this.zigzagDirection = (Math.random() > 0.5) ? 1 : -1;
                break;

            case HORIZONTAL_MOVE:
                this.speedY = 0;
                // Determine direction based on spawn position
                if (this.positionX < 0) {
                    this.speedX = HORIZONTAL_SPEED_X;  // Spawn left -> move right
                } else {
                    this.speedX = -HORIZONTAL_SPEED_X; // Spawn right -> move left
                }
                this.spriteType = SpriteType.EnemyShipC1;
                this.baseColor = Color.MAGENTA;
                this.color = this.baseColor;
                this.pointValue = HORIZONTAL_POINT_VALUE;
                break;
        }
    }

    /**
     * Updates the enemy's position and animation.
     */
    public void update() {
        if (isDestroyed) return;

        updateColorBasedOnHealth();
        // Animation update
        updateAnimation();

        // Movement update
        switch (pattern) {
            case STRAIGHT_DOWN:
                updateStraightDown();
                break;
            case ZIGZAG_DOWN:
                updateZigzagDown();
                break;
            case HORIZONTAL_MOVE:
                updateHorizontal();
                break;
        }
    }

    /**
     * Updates sprite animation.
     */
    private void updateAnimation() {
        if (this.animationCooldown.checkFinished()) {
            this.animationCooldown.reset();

            switch (this.spriteType) {
                case EnemyShipA1:
                    this.spriteType = SpriteType.EnemyShipA2;
                    break;
                case EnemyShipA2:
                    this.spriteType = SpriteType.EnemyShipA1;
                    break;
                case EnemyShipB1:
                    this.spriteType = SpriteType.EnemyShipB2;
                    break;
                case EnemyShipB2:
                    this.spriteType = SpriteType.EnemyShipB1;
                    break;
                case EnemyShipC1:
                    this.spriteType = SpriteType.EnemyShipC2;
                    break;
                case EnemyShipC2:
                    this.spriteType = SpriteType.EnemyShipC1;
                    break;
                default:
                    break;
            }
        }
    }
    private void updateColorBasedOnHealth() {
        double healthRatio = (double) this.hp / this.maxHp;
        this.color = adjustColorBrightness(this.baseColor, healthRatio);
    }
    private Color adjustColorBrightness(Color baseColor, double healthRatio) {
        float brightnessFactor = (float) healthRatio;

        int r = baseColor.getRed();
        int g = baseColor.getGreen();
        int b = baseColor.getBlue();

        int newR = (int) (r * brightnessFactor + 255 * (1 - brightnessFactor));
        int newG = (int) (g * brightnessFactor + 255 * (1 - brightnessFactor));
        int newB = (int) (b * brightnessFactor + 255 * (1 - brightnessFactor));

        newR = Math.min(255, Math.max(0, newR));
        newG = Math.min(255, Math.max(0, newG));
        newB = Math.min(255, Math.max(0, newB));

        return new Color(newR, newG, newB);
    }

    /**
     * Straight down movement - just falls vertically.
     */
    private void updateStraightDown() {
        this.positionY += (int) this.speedY;
    }

    /**
     * Zigzag down movement - falls while bouncing left and right.
     */
    private void updateZigzagDown() {
        this.positionY += (int) this.speedY;
        this.positionX += (int) (this.speedX * this.zigzagDirection);

        // Bounce off screen edges
        if (this.positionX <= 0) {
            this.positionX = 0;
            this.zigzagDirection = 1;
        } else if (this.positionX + this.width >= this.screenWidth) {
            this.positionX = this.screenWidth - this.width;
            this.zigzagDirection = -1;
        }
    }

    /**
     * Horizontal movement - moves across the screen.
     */
    private void updateHorizontal() {
        this.positionX += (int) this.speedX;
    }

    /**
     * Checks if enemy should be removed (off screen).
     *
     * @return True if should be removed
     */
    public boolean shouldDespawn() {
        switch (pattern) {
            case STRAIGHT_DOWN:
            case ZIGZAG_DOWN:
                // Remove if below screen
                return this.positionY > this.screenHeight;

            case HORIZONTAL_MOVE:
                // Remove if past opposite edge
                if (this.speedX > 0) {
                    return this.positionX > this.screenWidth;  // Moving right, past right edge
                } else {
                    return this.positionX + this.width < 0;    // Moving left, past left edge
                }

            default:
                return false;
        }
    }

    /**
     * Destroys the enemy with explosion effect.
     */
    public void destroy() {
        if (!this.isDestroyed) {
            this.isDestroyed = true;
            this.spriteType = SpriteType.Explosion;
            this.explosionCooldown.reset();
        }
    }

    /**
     * Applies damage to the enemy.
     *
     * @param damage Amount of damage
     */
    public void takeDamage(final int damage) {
        if (!this.isDestroyed) {
            this.hp -= damage;
            if (this.hp <= 0) {
                destroy();
            }
        }
    }

    public boolean canShoot() {
        return (!isDestroyed && shootingCooldown.checkFinished());
    }
    public void resetShootingCooldown() {
        shootingCooldown.reset();
    }
    public int getShootingPositionX() {
        return this.positionX + this.width / 2;
    }
    public int getShootingPositionY() {
        return this.positionY + this.height;
    }

    /**
     * Checks if the enemy is destroyed.
     *
     * @return True if destroyed
     */
    public boolean isDestroyed() {
        return this.isDestroyed;
    }

    /**
     * Checks if explosion animation is finished.
     *
     * @return True if finished
     */
    public boolean isExplosionFinished() {
        return this.isDestroyed && this.explosionCooldown.checkFinished();
    }

    /**
     * Gets point value of this enemy.
     *
     * @return Point value
     */
    public int getPointValue() {
        return this.pointValue;
    }

    /**
     * Gets current health.
     *
     * @return Current HP
     */
    public int getHealth() {
        return this.hp;
    }

    /**
     * Gets movement pattern.
     *
     * @return Movement pattern
     */
    public MovementPattern getPattern() {
        return this.pattern;
    }

    /**
     * Sets the health (for difficulty scaling).
     *
     * @param hp New health value
     */
    public void setHealth(final int hp) {
        this.hp = hp;
        this.maxHp = hp;
    }

    /**
     * Sets movement speed multiplier (for difficulty scaling).
     *
     * @param multiplier Speed multiplier
     */
    public void setSpeedMultiplier(final double multiplier) {
        this.speedX *= multiplier;
        this.speedY *= multiplier;
    }
}