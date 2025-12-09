package entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.Collections;
import java.awt.Color;

import screen.Screen;
import screen.GameScreen;
import engine.Cooldown;
import engine.Core;
import engine.DrawManager;
import engine.DrawManager.SpriteType;
import engine.GameSettings;
import engine.level.Level;
/**
 * Groups enemy ships into a formation that moves together.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class EnemyShipFormation implements Iterable<EnemyShip> {

	/** Initial position in the x-axis. */
	private static final int INIT_POS_X = 20;
	/** Initial position in the y-axis. */
	private static final int INIT_POS_Y = 100;
	/** Distance between ships. */
	private static final int SEPARATION_DISTANCE = 40;
	/** Proportion of C-type ships. */
	private static final double PROPORTION_C = 0.2;
	/** Proportion of B-type ships. */
	private static final double PROPORTION_B = 0.4;
	/** Downwards speed of the formation. */
	private static final int Y_SPEED = 4;
	/** Speed of the bullets shot by the members. */
	private static final int BULLET_SPEED = 4;
	/** Proportion of differences between shooting times. */
	private static final double SHOOTING_VARIANCE = .2;
	/** Margin on the sides of the screen. */
	private static final int SIDE_MARGIN = 20;
	/** Margin on the bottom of the screen. */
	private static final int BOTTOM_MARGIN = 80;
	/** Distance to go down each pass. */
	private static final int DESCENT_DISTANCE = 20;
	/** Minimum speed allowed. */
	private static final int MINIMUM_SPEED = 10;

	/** DrawManager instance. */
	private DrawManager drawManager;
	/** Application logger. */
	private Logger logger;
	/** Screen to draw ships on. */
	private Screen screen;
    /** Level reference to read enemyTypes/counts. */
    private Level levelObj;

	/** List of enemy ships forming the formation. */
	private List<List<EnemyShip>> enemyShips;
	/** Minimum time between shots. */
	private Cooldown shootingCooldown;
	/** Number of ships in the formation - horizontally. */
	private int nShipsWide;
	/** Number of ships in the formation - vertically. */
	private int nShipsHigh;
	/** Time between shots. */
	private int shootingInterval;
	/** Variance in the time between shots. */
	private int shootingVariance;
	/** Initial ship speed. */
	private int baseSpeed;
	/** Speed of the ships. */
	private int movementSpeed;
	/** Current direction the formation is moving on. */
	private Direction currentDirection;
	/** Direction the formation was moving previously. */
	private Direction previousDirection;
	/** Interval between movements, in frames. */
	private int movementInterval;
	/** Total width of the formation. */
	private int width;
	/** Total height of the formation. */
	private int height;
	/** Position in the x-axis of the upper left corner of the formation. */
	private int positionX;
	/** Position in the y-axis of the upper left corner of the formation. */
	private int positionY;
	/** Width of one ship. */
	private int shipWidth;
	/** Height of one ship. */
	private int shipHeight;
	/** List of ships that are able to shoot. */
	private List<EnemyShip> shooters;
	/** Number of not destroyed ships. */
	private int shipCount;
    /** Number of slowdown movement */
    private int slowDownCount;
    /** Flag to check if slowdown is active */
    private boolean isSlowedDown;
    /** Original X_SPEED value */
    private static final int ORIGINAL_X_SPEED = 8;
    /** Slowed down X_SPEED value */
    private static final int SLOWED_X_SPEED = 4;
    /** Duration of slowdown effect (in movement cycles) */
    private static final int SLOWDOWN_DURATION = 18;

    /** Directions the formation can move. */
    private enum Direction {
        /** Movement to the right-down diagonal. */
        DOWN_RIGHT,
        /** Movement to the left-down diagonal. */
        DOWN_LEFT,
        /** Movement to the right-up diagonal. */
        UP_RIGHT,
        /** Movement to the left-up diagonal. */
        UP_LEFT
    };

	/**
	 * Constructor, sets the initial conditions.
	 * 
	 * @param gameSettings
	 *            Current game settings.
	 */
	public EnemyShipFormation(final GameSettings gameSettings) {
		this.drawManager = Core.getDrawManager();
		this.logger = Core.getLogger();
		this.enemyShips = new ArrayList<List<EnemyShip>>();
		this.currentDirection = Direction.DOWN_RIGHT;
		this.movementInterval = 0;
		this.nShipsWide = gameSettings.getFormationWidth();
		this.nShipsHigh = gameSettings.getFormationHeight();
		this.shootingInterval = gameSettings.getShootingFrecuency();
		this.shootingVariance = (int) (gameSettings.getShootingFrecuency()
				* SHOOTING_VARIANCE);
		this.baseSpeed = gameSettings.getBaseSpeed();
		this.movementSpeed = this.baseSpeed;
		this.positionX = INIT_POS_X;
		this.positionY = INIT_POS_Y;
		this.shooters = new ArrayList<EnemyShip>();
		SpriteType spriteType;

		this.logger.info("Initializing " + nShipsWide + "x" + nShipsHigh
				+ " ship formation in (" + positionX + "," + positionY + ")");

		// Each sub-list is a column on the formation.
		for (int i = 0; i < this.nShipsWide; i++)
			this.enemyShips.add(new ArrayList<EnemyShip>());

		for (List<EnemyShip> column : this.enemyShips) {
			for (int i = 0; i < this.nShipsHigh; i++) {
				if (i / (float) this.nShipsHigh < PROPORTION_C)
					spriteType = SpriteType.EnemyShipC1;
				else if (i / (float) this.nShipsHigh < PROPORTION_B
						+ PROPORTION_C)
					spriteType = SpriteType.EnemyShipB1;
				else
					spriteType = SpriteType.EnemyShipA1;

				column.add(new EnemyShip((SEPARATION_DISTANCE 
						* this.enemyShips.indexOf(column))
								+ positionX, (SEPARATION_DISTANCE * i)
								+ positionY, spriteType));
				this.shipCount++;
			}
		}

		this.shipWidth = this.enemyShips.get(0).get(0).getWidth();
		this.shipHeight = this.enemyShips.get(0).get(0).getHeight();

		this.width = (this.nShipsWide - 1) * SEPARATION_DISTANCE
				+ this.shipWidth;
		this.height = (this.nShipsHigh - 1) * SEPARATION_DISTANCE
				+ this.shipHeight;

		for (List<EnemyShip> column : this.enemyShips)
			this.shooters.add(column.get(column.size() - 1));
	}

    /**
     * Constructor that uses Level directly (without GameSettings).
     * @param level Current level data.
     */
    public EnemyShipFormation(final Level level) {
        this.drawManager = Core.getDrawManager();
        this.logger = Core.getLogger();
        this.enemyShips = new ArrayList<List<EnemyShip>>();
        this.currentDirection = Direction.DOWN_RIGHT;
        this.movementInterval = 0;

        // Read values directly from Level
        this.nShipsWide = level.getFormationWidth();
        this.nShipsHigh = level.getFormationHeight();
        this.shootingInterval = level.getShootingFrecuency();
        this.shootingVariance = (int) (level.getShootingFrecuency() * SHOOTING_VARIANCE);
        this.baseSpeed = level.getBaseSpeed();
        this.movementSpeed = this.baseSpeed;
        this.positionX = INIT_POS_X;
        this.positionY = INIT_POS_Y;
        this.shooters = new ArrayList<EnemyShip>();
        this.levelObj = level;
        SpriteType spriteType;

        this.logger.info("Initializing " + nShipsWide + "x" + nShipsHigh
                + " ship formation in (" + positionX + "," + positionY + ")");

        // Each sub-list is a column on the formation.
        for (int i = 0; i < this.nShipsWide; i++)
            this.enemyShips.add(new ArrayList<EnemyShip>());

        final int cells = this.nShipsWide * this.nShipsHigh;
        List<SpriteType> spriteQueue = buildLayeredQueueFromLevel(level, this.nShipsWide, this.nShipsHigh);
        boolean useQueue = (spriteQueue != null && spriteQueue.size() == cells);
        int qIndex = 0;

        for (List<EnemyShip> column : this.enemyShips) {
            for (int i = 0; i < this.nShipsHigh; i++) {
                SpriteType chosen;
                if (useQueue) {
                    chosen = spriteQueue.get(qIndex++);
                } else {
                    // Fallback: legacy proportion-based selection
                    if (i / (float) this.nShipsHigh < PROPORTION_C)
                        chosen = SpriteType.EnemyShipC1;
                    else if (i / (float) this.nShipsHigh < PROPORTION_B + PROPORTION_C)
                        chosen = SpriteType.EnemyShipB1;
                    else
                        chosen = SpriteType.EnemyShipA1;
                }

                column.add(new EnemyShip(
                        (SEPARATION_DISTANCE * this.enemyShips.indexOf(column)) + positionX,
                        (SEPARATION_DISTANCE * i) + positionY,
                        chosen));
                this.shipCount++;
            }
        }

        this.shipWidth = this.enemyShips.get(0).get(0).getWidth();
        this.shipHeight = this.enemyShips.get(0).get(0).getHeight();

        this.width = (this.nShipsWide - 1) * SEPARATION_DISTANCE + this.shipWidth;
        this.height = (this.nShipsHigh - 1) * SEPARATION_DISTANCE + this.shipHeight;

        for (List<EnemyShip> column : this.enemyShips)
            this.shooters.add(column.get(column.size() - 1));
    }

	/**
	 * Associates the formation to a given screen.
	 *
	 * @param newScreen
	 *            Screen to attach.
	 */
	public final void attach(final Screen newScreen) {
		screen = newScreen;
	}

	/**
	 * Draws every individual component of the formation.
	 */
	public void draw() {
		for (List<EnemyShip> column : this.enemyShips)
			for (EnemyShip enemyShip : column)
				drawManager.drawEntity(enemyShip, enemyShip.getPositionX(),
						enemyShip.getPositionY());
	}

	/**
	 * Updates the position of the ships.
	 */
	public void update() {
		if(this.shootingCooldown == null) {
			this.shootingCooldown = Core.getVariableCooldown(shootingInterval,
					shootingVariance);
			this.shootingCooldown.reset();
		}

		cleanUp();

		int movementX = 0;
		int movementY = 0;
		double remainingProportion = (double) this.shipCount
				/ (this.nShipsHigh * this.nShipsWide);
		this.movementSpeed = (int) (Math.pow(remainingProportion, 2)
				* this.baseSpeed);
		this.movementSpeed += MINIMUM_SPEED;

		movementInterval++;
		if (movementInterval >= this.movementSpeed) {
			movementInterval = 0;

            updateSlowdown();

			boolean isAtBottom = positionY
					+ this.height > GameScreen.getItemsSeparationLineHeight();
			boolean isAtRightSide = positionX
					+ this.width >= screen.getWidth() - SIDE_MARGIN;
			boolean isAtLeftSide = positionX <= SIDE_MARGIN;
            boolean isAtTop = positionY <= INIT_POS_Y;

            // Diagonal movement direction change logic
            if (currentDirection == Direction.DOWN_RIGHT) {
                if (isAtBottom && isAtRightSide) {
                    currentDirection = Direction.UP_LEFT;
                    this.logger.info("Formation now moving up-left (hit corner)");
                } else if (isAtBottom) {
                    currentDirection = Direction.UP_RIGHT;
                    this.logger.info("Formation now moving up-right (hit bottom)");
                } else if (isAtRightSide) {
                    currentDirection = Direction.DOWN_LEFT;
                    this.logger.info("Formation now moving down-left (hit right wall)");
                }
            } else if (currentDirection == Direction.DOWN_LEFT) {
                if (isAtBottom && isAtLeftSide) {
                    currentDirection = Direction.UP_RIGHT;
                    this.logger.info("Formation now moving up-right (hit corner)");
                } else if (isAtBottom) {
                    currentDirection = Direction.UP_LEFT;
                    this.logger.info("Formation now moving up-left (hit bottom)");
                } else if (isAtLeftSide) {
                    currentDirection = Direction.DOWN_RIGHT;
                    this.logger.info("Formation now moving down-right (hit left wall)");
                }
            } else if (currentDirection == Direction.UP_RIGHT) {
                if (isAtTop && isAtRightSide) {
                    currentDirection = Direction.DOWN_LEFT;
                    this.logger.info("Formation now moving down-left (hit corner)");
                } else if (isAtTop) {
                    currentDirection = Direction.DOWN_RIGHT;
                    this.logger.info("Formation now moving down-right (back to top)");
                } else if (isAtRightSide) {
                    currentDirection = Direction.UP_LEFT;
                    this.logger.info("Formation now moving up-left (hit right wall)");
                }
            } else if (currentDirection == Direction.UP_LEFT) {
                if (isAtTop && isAtLeftSide) {
                    currentDirection = Direction.DOWN_RIGHT;
                    this.logger.info("Formation now moving down-right (hit corner)");
                } else if (isAtTop) {
                    currentDirection = Direction.DOWN_LEFT;
                    this.logger.info("Formation now moving down-left (back to top)");
                } else if (isAtLeftSide) {
                    currentDirection = Direction.UP_RIGHT;
                    this.logger.info("Formation now moving up-right (hit left wall)");
                }
            }

            int currentXSpeed = getCurrentXSpeed();
            if (currentDirection == Direction.DOWN_RIGHT) {
                movementX = currentXSpeed;   // right
                movementY = Y_SPEED;   // down
            } else if (currentDirection == Direction.DOWN_LEFT) {
                movementX = -currentXSpeed;  // left
                movementY = Y_SPEED;   // down
            } else if (currentDirection == Direction.UP_RIGHT) {
                movementX = currentXSpeed;   // right
                movementY = -Y_SPEED;  // up
            } else if (currentDirection == Direction.UP_LEFT) {
                movementX = -currentXSpeed;  // left
                movementY = -Y_SPEED;  // up
            }

			positionX += movementX;
			positionY += movementY;

			// Cleans explosions.
			List<EnemyShip> destroyed;
			for (List<EnemyShip> column : this.enemyShips) {
				destroyed = new ArrayList<EnemyShip>();
				for (EnemyShip ship : column) {
					if (ship != null && ship.isExplosionFinished()) {
						destroyed.add(ship);
						this.logger.info("Removed enemy "
								+ column.indexOf(ship) + " from column "
								+ this.enemyShips.indexOf(column));
					}
				}

				column.removeAll(destroyed);
			}

			for (List<EnemyShip> column : this.enemyShips)
				for (EnemyShip enemyShip : column) {
					enemyShip.move(movementX, movementY);
					enemyShip.update();
				}
		}
	}

	/**
	 * Cleans empty columns, adjusts the width and height of the formation.
	 */
	private void cleanUp() {
		Set<Integer> emptyColumns = new HashSet<Integer>();
		int maxColumn = 0;
		int minPositionY = Integer.MAX_VALUE;
		for (List<EnemyShip> column : this.enemyShips) {
			if (!column.isEmpty()) {
				// Height of this column
				int columnSize = column.get(column.size() - 1).positionY
						- this.positionY + this.shipHeight;
				maxColumn = Math.max(maxColumn, columnSize);
				minPositionY = Math.min(minPositionY, column.get(0)
						.getPositionY());
			} else {
				// Empty column, we remove it.
				emptyColumns.add(this.enemyShips.indexOf(column));
			}
		}
		for (int index : emptyColumns) {
			this.enemyShips.remove(index);
			logger.info("Removed column " + index);
		}

		int leftMostPoint = 0;
		int rightMostPoint = 0;

		for (List<EnemyShip> column : this.enemyShips) {
			if (!column.isEmpty()) {
				if (leftMostPoint == 0)
					leftMostPoint = column.get(0).getPositionX();
				rightMostPoint = column.get(0).getPositionX();
			}
		}

		this.width = rightMostPoint - leftMostPoint + this.shipWidth;
		this.height = maxColumn;

		this.positionX = leftMostPoint;
		this.positionY = minPositionY;
	}

	/**
	 * Shoots a bullet downwards.
	 *
	 * @param bullets
	 *            Bullets set to add the bullet being shot.
	 */
	public void shoot(final Set<Bullet> bullets) {
		// For now, only ships in the bottom row are able to shoot.
		if (this.shooters.isEmpty()) {return; }
		int index = (int) (Math.random() * this.shooters.size());
		EnemyShip shooter = this.shooters.get(index);

		if (this.shootingCooldown.checkFinished()) {
			this.shootingCooldown.reset();
			bullets.add(BulletPool.getBullet(shooter.getPositionX()
					+ shooter.width / 2, shooter.getPositionY(), BULLET_SPEED));
		}
	}

    /**
	 * Destroys a ship.
	 *
	 * @param destroyedShip
	 *            Ship to be destroyed.
	 */
	public void destroy(final EnemyShip destroyedShip) {
		for (List<EnemyShip> column : this.enemyShips)
			for (int i = 0; i < column.size(); i++)
				if (column.get(i).equals(destroyedShip)) {
					column.get(i).destroy();
					this.logger.info("Destroyed ship in ("
							+ this.enemyShips.indexOf(column) + "," + i + ")");
				}

		// Updates the list of ships that can shoot the player.
		if (this.shooters.contains(destroyedShip)) {
			int destroyedShipIndex = this.shooters.indexOf(destroyedShip);
			int destroyedShipColumnIndex = -1;

			for (List<EnemyShip> column : this.enemyShips)
				if (column.contains(destroyedShip)) {
					destroyedShipColumnIndex = this.enemyShips.indexOf(column);
					break;
				}

			EnemyShip nextShooter = getNextShooter(this.enemyShips
					.get(destroyedShipColumnIndex));

			if (nextShooter != null)
				this.shooters.set(destroyedShipIndex, nextShooter);
			else {
				this.shooters.remove(destroyedShipIndex);
				this.logger.info("Shooters list reduced to "
						+ this.shooters.size() + " members.");
			}
		}

		this.shipCount--;
	}

	/**
	 * Gets the ship on a given column that will be in charge of shooting.
	 *
	 * @param column
	 *            Column to search.
	 * @return New shooter ship.
	 */
	public final EnemyShip getNextShooter(final List<EnemyShip> column) {
		Iterator<EnemyShip> iterator = column.iterator();
		EnemyShip nextShooter = null;
		while (iterator.hasNext()) {
			EnemyShip checkShip = iterator.next();
			if (checkShip != null && !checkShip.isDestroyed())
				nextShooter = checkShip;
		}

		return nextShooter;
	}

	/**
	 * Returns an iterator over the ships in the formation.
	 *
	 * @return Iterator over the enemy ships.
	 */
	@Override
	public Iterator<EnemyShip> iterator() {
		Set<EnemyShip> enemyShipsList = new HashSet<EnemyShip>();

		for (List<EnemyShip> column : this.enemyShips)
			for (EnemyShip enemyShip : column)
				enemyShipsList.add(enemyShip);

		return enemyShipsList.iterator();
	}

	/**
	 * Destroy all ships in the formation.
	 *
	 * @return The number of destroyed ships.
	 */

	public final int destroyAll() {
		int destroyed = 0;
		for (List<EnemyShip> column : this.enemyShips) {
			for (EnemyShip enemyShip : column) {
				if (!enemyShip.isDestroyed()) {
					enemyShip.destroy();
					destroyed++;
				}
			}
		}
		this.shipCount = 0;
		return destroyed;
	}

	/**
	 * Checks if there are any ships remaining.
	 *
	 * @return True when all ships have been destroyed.
	 */
	public boolean isEmpty() {
		return this.shipCount <= 0;
	}

    /**
     * Activates slowdown effect on the formation.
     */
    public void activateSlowdown() {
        this.isSlowedDown = true;
        this.slowDownCount = 0;
        this.logger.info("Enemy formation slowed down!");
    }

    /**
     * Gets the current movement speed based on slowdown status.
     *
     * @return Current X_SPEED value
     */
    private int getCurrentXSpeed() {
        if (isSlowedDown) {
            return SLOWED_X_SPEED;
        }
        return ORIGINAL_X_SPEED;
    }

    /**
     * Updates slowdown counter and checks if effect should end.
     * Call this in the update() method when formation moves.
     */
    private void updateSlowdown() {
        if (isSlowedDown) {
            slowDownCount++;
            if (slowDownCount >= SLOWDOWN_DURATION) {
                isSlowedDown = false;
                slowDownCount = 0;
                this.logger.info("Slowdown effect ended.");
            }
        }
    }

	public void clear() {
		for (List<EnemyShip> column : this.enemyShips) {
			column.clear();
		}
		this.enemyShips.clear();
		this.shipCount = 0;
	}

   private List<SpriteType> buildLayeredQueueFromLevel(final Level level, final int width, final int height) {
        final int cells = width * height;
        List<SpriteType> rowMajor = new ArrayList<>(cells);

        if (level == null || level.getEnemyTypes() == null || level.getEnemyTypes().isEmpty()) {
            return new ArrayList<>(); // empty -> caller will fallback
        }

        int countA = 0, countB = 0, countC = 0;
        for (engine.level.EnemyType t : level.getEnemyTypes()) {
            String kind = (t.getType() == null) ? "enemya" : t.getType().trim().toLowerCase();
            int cnt = Math.max(0, t.getCount());
            switch (kind) {
                case "enemya":
                case "a":
                    countA += cnt; break;
                case "enemyb":
                case "b":
                    countB += cnt; break;
                case "enemyc":
                case "c":
                    countC += cnt; break;
                default:
                    countA += cnt;
            }
        }

        int total = countA + countB + countC;
        if (total < cells) countA += (cells - total);


        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (countC > 0) {
                    rowMajor.add(SpriteType.EnemyShipC1);
                    countC--;
                } else if (countB > 0) {
                    rowMajor.add(SpriteType.EnemyShipB1);
                    countB--;
                } else if (countA > 0) {
                    rowMajor.add(SpriteType.EnemyShipA1);
                    countA--;
                } else {
                    // Safety pad
                    rowMajor.add(SpriteType.EnemyShipA1);
                }
            }
        }

        // Convert to column-major order because constructor consumes by column then row.
        List<SpriteType> columnMajor = new ArrayList<>(cells);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                columnMajor.add(rowMajor.get(row * width + col));
            }
        }

        // Clamp/pad to exact cells size for safety.
        if (columnMajor.size() > cells) {
            return new ArrayList<>(columnMajor.subList(0, cells));
        }
        while (columnMajor.size() < cells) {
            columnMajor.add(SpriteType.EnemyShipA1);
        }
        return columnMajor;
    }
}
