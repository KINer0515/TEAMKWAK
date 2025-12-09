package entity;
import audio.SoundManager;

import java.awt.Color;
import java.util.Set;

import engine.Cooldown;
import engine.Core;
import engine.DrawManager.SpriteType;

/**
 * Implements a ship, to be controlled by the player.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class Ship extends Entity {

	/** Time between shots. */
	private static final int SHOOTING_INTERVAL = 750;
	/** Speed of the bullets shot by the ship. */
	private static final int BULLET_SPEED = -6;
	/** Movement of the ship for each unit of time. */
	private static final int SPEED = 2;
	
	/** Minimum time between shots. */
	private Cooldown shootingCooldown;
	/** Time spent inactive between hits. */
	private Cooldown destructionCooldown;
	/** Cooldown for the invincibility shield. */
	private Cooldown shieldCooldown;
	/** Checks if the ship is invincible. */
	private boolean isInvincible;
	/** Original color of the ship. */
	private Color originalColor;
    // === [ADD] Which player: 1 = P1, 2 = P2 (default 1 for single-player compatibility) ===
    private int playerId = 1;
    public void setPlayerId(int pid) { this.playerId = pid; }
    public int getPlayerId() { return this.playerId; }

	/**
	 * Constructor, establishes the ship's properties.
	 * 
	 * @param positionX
	 *            Initial position of the ship in the X axis.
	 * @param positionY
	 *            Initial position of the ship in the Y axis.
	 */
	public Ship(final int positionX, final int positionY,final Color color) {
		super(positionX, positionY, 13 * 2, 8 * 2, color);

		this.originalColor = color;
		this.spriteType = SpriteType.Ship;
		this.shootingCooldown = Core.getCooldown(ShopItem.getShootingInterval());
		this.destructionCooldown = Core.getCooldown(1000);
		this.shieldCooldown = Core.getCooldown(0);
		this.isInvincible = false;

	}

	/**
	 * Moves the ship speed uni ts right, or until the right screen border is
	 * reached.
	 */
	public final void moveRight() {
		int shipspeed = ShopItem.getSHIPSpeedCOUNT();
		this.positionX += SPEED*(1+shipspeed/10);
	}

	/**
	 * Moves the ship speed units left, or until the left screen border is
	 * reached.
	 */
	public final void moveLeft() {
		int shipspeed = ShopItem.getSHIPSpeedCOUNT();
		this.positionX -= SPEED*(1+shipspeed/10);
	}

    /**
     * Moves the ship speed units up, or until the SEPARATION_LINE_HEIGHT is
     * reached.
     */
    public final void moveUp() {
		int shipspeed = ShopItem.getSHIPSpeedCOUNT();
		this.positionY -= SPEED*(1+shipspeed/10);
    }

    /**
     * Moves the ship speed units down, or until the down screen border is
     * reached.
     */
    public final void moveDown() {
		int shipspeed = ShopItem.getSHIPSpeedCOUNT();
		this.positionY += SPEED*(1+shipspeed/10);
    }

	/**
	 * Shoots a bullet upwards.
	 * 
	 * @param bullets
	 *            List of bullets on screen, to add the new bullet.
	 * @return Checks if the bullet was shot correctly.
	 */
	public final boolean shoot(final Set<Bullet> bullets) {
		if (this.shootingCooldown.checkFinished()) {
			this.shootingCooldown.reset();

			// Get Spread Shot information from the DropItem class
			int bulletCount = ShopItem.getMultiShotBulletCount();
			int spacing = ShopItem.getMultiShotSpacing();

			int centerX = positionX + this.width / 2;
			int centerY = positionY;

			if (bulletCount == 1) {
				// Normal shot (when Spread Shot is not purchased)
				Bullet b = BulletPool.getBullet(centerX, centerY, BULLET_SPEED);
				SoundManager.stop("sfx/laser.wav");
                SoundManager.play("sfx/laser.wav");
                b.setOwnerId(this.playerId);  // === [ADD] Ownership flag: 1 = P1, 2 = P2, null for legacy logic ===

                bullets.add(b);
			} else {
				// Fire Spread Shot
				int startOffset = -(bulletCount / 2) * spacing;

				for (int i = 0; i < bulletCount; i++) {
					int offsetX = startOffset + (i * spacing);
                    Bullet b = BulletPool.getBullet(centerX + offsetX, centerY, BULLET_SPEED);
                    b.setOwnerId(this.playerId);   // Ownership flag

                    bullets.add(b);

                    // might consider putting a different sound
					SoundManager.stop("sfx/laser.wav");
                    SoundManager.play("sfx/laser.wav");
                }
			}
			return true;
		}
		return false;
	}

	/**
	 * Updates status of the ship.
	 */
	public final void update() {
        if (this.isInvincible && this.shieldCooldown.checkFinished()) {
            this.isInvincible = false;
            this.setColor(this.originalColor);
        }

        if (!this.destructionCooldown.checkFinished())
            this.spriteType = SpriteType.ShipDestroyed;
        else
            this.spriteType = SpriteType.Ship;
	}

	/**
	 * Switches the ship to its destroyed state.
	 */
	public final void destroy() {
        if (!this.isInvincible) {
			SoundManager.stop("sfx/impact.wav");
            SoundManager.play("sfx/impact.wav");
            this.destructionCooldown.reset();
        }
    }

	/**
	 * Checks if the ship is destroyed.
	 * 
	 * @return True if the ship is currently destroyed.
	 */
	public final boolean isDestroyed() {
		return !this.destructionCooldown.checkFinished();
	}

	/**
	 * Getter for the ship's speed.
	 * 
	 * @return Speed of the ship.
	 */
	public final int getSpeed() {
		return SPEED;
	}

    /**
     * Getter for the ship's invincibility state.
     *
     * @return True if the ship is currently invincible.
     */
    public final boolean isInvincible() {
        return this.isInvincible;
    }

    /**
     * Activates the ship's invincibility shield for a given duration.
     *
     * @param duration
     *            Duration of the invincibility in milliseconds.
     */
    public final void activateInvincibility(final int duration) {
        this.isInvincible = true;
        this.shieldCooldown.setMilliseconds(duration);
        this.shieldCooldown.reset();
        this.setColor(Color.BLUE);
    }
}
