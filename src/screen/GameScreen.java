package screen;

import engine.*;
import engine.dto.StatePacket;
import engine.level.Level;
import entity.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;


/**
 * Implements the game screen, where the action happens.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class GameScreen extends Screen implements CollisionContext {

	/** Milliseconds until the screen accepts user input. */
	private static final int INPUT_DELAY = 6000;
	/** Bonus score for each life remaining at the end of the level. */
	private static final int LIFE_SCORE = 100;
	/** Minimum time between bonus ship's appearances. */
	private static final int BONUS_SHIP_INTERVAL = 20000;
	/** Maximum variance in the time between bonus ship's appearances. */
	private static final int BONUS_SHIP_VARIANCE = 10000;
	/** Time until bonus ship explosion disappears. */
	private static final int BONUS_SHIP_EXPLOSION = 500;
	/** Time until bonus ship explosion disappears. */
	private static final int BOSS_EXPLOSION = 600;
	/** Time from finishing the level to screen change. */
	private static final int SCREEN_CHANGE_INTERVAL = 1500;
	/** Height of the interface separation line. */
	private static final int SEPARATION_LINE_HEIGHT = 45;
	/** Height of the items separation line (above items). */
	private static final int ITEMS_SEPARATION_LINE_HEIGHT = 400;
    /** Returns the Y-coordinate of the bottom boundary for enemies (above items HUD) */
    public static int getItemsSeparationLineHeight() {
        return ITEMS_SEPARATION_LINE_HEIGHT;
    }

    /** Current level data (direct from Level system). */
    private Level currentLevel;
	/** Current difficulty level number. */
	private int level;
	/** Formation of enemy ships. */
	private EnemyShipFormation enemyShipFormation;
	/** Formation of special enemy ships. */
	private EnemyShipSpecialFormation enemyShipSpecialFormation;
	/** Player's ship. */
	private Ship ship;
	/** Second Player's ship. */
	private Ship shipP2;
	/** Bonus enemy ship that appears sometimes. */
	private EnemyShip enemyShipSpecial;
	/** Minimum time between bonus ship appearances. */
	private Cooldown enemyShipSpecialCooldown;
	/** team drawing may implement */
	private FinalBoss finalBoss;
	/** Time until bonus ship explosion disappears. */
	private Cooldown enemyShipSpecialExplosionCooldown;
	/** Time until Boss explosion disappears. */
	private Cooldown bossExplosionCooldown;
	/** Time from finishing the level to screen change. */
	private Cooldown screenFinishedCooldown;
	/** OmegaBoss */
	private MidBoss omegaBoss;
	/** Set of all bullets fired by on-screen ships. */
	private Set<Bullet> bullets;
	/** Set of all dropItems dropped by on screen ships. */
	private Set<DropItem> dropItems;
	/** Current score. */
	private int score;
    // === [ADD] Independent scores for two players ===
    private int scoreP1 = 0;
    private int scoreP2 = 0;
	/** current level parameter */
	public Level currentlevel;
    /** Player lives left. */
	private int livesP1;
	private int livesP2;
	/** Total bullets shot by the player. */
	private int bulletsShot;
	/** Total ships destroyed by the player. */
	private int shipsDestroyed;
	/** Moment the game starts. */
	private long gameStartTime;
	/** Checks if the level is finished. */
	private boolean levelFinished;
	/** Checks if a bonus life is received. */
	private boolean bonusLife;
    /** Maximum number of lives. */
	private int maxLives;
	/** Current coin. */
	private int coin;
	/** check 1p or 2p mode. */
	private boolean isTwoPlayerMode;
    /** Manages collisions between entities. */
    private CollisionManager collisionManager;

    // Unified scoring entry: maintains both P1/P2 and legacy this.score (total score)
    public void addPointsFor(Bullet bullet, int pts) {
        Integer owner = (bullet != null ? bullet.getOwnerId() : null);
        if (owner != null && owner == 2) {
            this.scoreP2 += pts;   // P2
        } else {
            this.scoreP1 += pts;   // Default to P1 (for null compatibility)

        }
        this.score += pts;        // Keep maintaining the total score, for legacy process compatibility

    }

    /** bossBullets carry bullets which Boss fires */
	private Set<BossBullet> bossBullets;
	/** Is the bullet on the screen erased */
    private boolean is_cleared = false;
    /** Timer to track elapsed time. */
    private GameTimer gameTimer;
    /** Elapsed time since the game started. */
    private long elapsedTime;
    // Achievement popup
    private String achievementText;
    private Cooldown achievementPopupCooldown;
    private enum StagePhase{wave, boss_wave};
    private StagePhase currentPhase;
    /** Health change popup. */
    private String healthPopupText;
    private Cooldown healthPopupCooldown;

    private GameState gameState;

    // === External control state for HTTP/AI controller (Player 1) ===
    /** Whether external control is enabled for player 1. */
    private boolean isAIMode = false;
    /** Latest external horizontal movement (-1: left, 0: none, 1: right). */
    private int externalMoveX = 0;
    /** Latest external vertical movement (-1: up, 0: none, 1: down). */
    private int externalMoveY = 0;
    /** Latest external shooting flag. */
    private boolean externalShoot = false;

    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param gameState
     *            Current game state.	 * @param level
	 *            Current level settings.
	 * @param bonusLife
	 *            Checks if a bonus life is awarded this level.
	 * @param maxLives
	 *            Maximum number of lives.
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 */
	public GameScreen(final GameState gameState,
			final Level level, final boolean bonusLife, final int maxLives,
			final int width, final int height, final int fps) {
		super(width, height, fps);

        this.currentLevel = level;
		this.bonusLife = bonusLife;
        this.currentlevel = level;
        this.maxLives = maxLives;
		this.level = gameState.getLevel();
		this.score = gameState.getScore();
		this.scoreP1 = gameState.getScoreP1();
		if (gameState.isTwoPlayerMode())
			this.scoreP2 = gameState.getScoreP2();
		else
			this.scoreP2 = 0;
        this.coin = gameState.getCoin();
	    this.livesP1 = gameState.getLivesRemaining();
		this.livesP2 = gameState.getLivesRemainingP2();
		this.gameState = gameState;
		if (this.bonusLife) {
			this.livesP1++;
			this.livesP2++;
		}
		this.bulletsShot = gameState.getBulletsShot();
		this.shipsDestroyed = gameState.getShipsDestroyed();
		this.isTwoPlayerMode = gameState.isTwoPlayerMode();
        this.isAIMode = gameState.isAIMode();
	}

	/**
	 * Initializes basic screen properties, and adds necessary elements.
	 */
	public final void initialize() {
		super.initialize();

        this.collisionManager = new CollisionManager(this);
		/** Initialize the bullet Boss fired */
		this.bossBullets = new HashSet<>();
        enemyShipFormation = new EnemyShipFormation(this.currentLevel);
		enemyShipFormation.attach(this);
		this.ship = new Ship(this.width / 2, ITEMS_SEPARATION_LINE_HEIGHT - 20,Color.green);
		this.ship.setPlayerId(1);   //=== [ADD] Player 1 ===

		if(this.isTwoPlayerMode) {
			this.ship.setPositionX(this.width / 2 - 100);
			this.shipP2 = new Ship(this.width / 2 + 100, ITEMS_SEPARATION_LINE_HEIGHT - 20, Color.pink);
			this.shipP2.setPlayerId(2); // === [ADD] Player2 ===
		}
//		this.scoreP1 = gameState.getScoreP1();
//		this.scoreP2 = gameState.getScoreP2();
        // special enemy initial
		enemyShipSpecialFormation = new EnemyShipSpecialFormation(this.currentLevel,
				Core.getVariableCooldown(BONUS_SHIP_INTERVAL, BONUS_SHIP_VARIANCE),
				Core.getCooldown(BONUS_SHIP_EXPLOSION));
		enemyShipSpecialFormation.attach(this);
		this.bossExplosionCooldown = Core
				.getCooldown(BOSS_EXPLOSION);
		this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
		this.bullets = new HashSet<Bullet>();
        this.dropItems = new HashSet<DropItem>();

		// Special input delay / countdown.
		this.gameStartTime = System.currentTimeMillis();
		this.inputDelay = Core.getCooldown(INPUT_DELAY);
		this.inputDelay.reset();


		this.gameTimer = new GameTimer();
        this.elapsedTime = 0;
		this.finalBoss = null;
		this.omegaBoss = null;
		this.currentPhase = StagePhase.wave;
	}

	/**
	 * Starts the action.
	 *
	 * @return Next screen code.
	 */
	public final int run() {
		super.run();

		this.scoreP1 += LIFE_SCORE * (this.livesP1 - 1);
		this.score += LIFE_SCORE * (this.livesP1 - 1);
		if(this.isTwoPlayerMode) {
			this.scoreP2 += LIFE_SCORE * (this.livesP2 - 1);
			this.score += LIFE_SCORE * (this.livesP2 - 1);
		}
		this.logger.info("@@Screen cleared with a score of " + this.scoreP1 + ",2p :" + this.scoreP2 + ", total :" + this.score);

		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();

		if (this.inputDelay.checkFinished() && !this.levelFinished) {

			if (!this.gameTimer.isRunning()) {
				this.gameTimer.start();
			}

			if (this.livesP1 > 0 && !this.ship.isDestroyed()) {
				boolean p1Right = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_D);
				boolean p1Left  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_A);
				boolean p1Up    = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_W);
				boolean p1Down  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_S);
				boolean p1Fire  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_SPACE);
				if (!this.isTwoPlayerMode) {
					p1Right = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_D) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_RIGHT);
					p1Left  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_A) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_LEFT);
					p1Up    = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_W) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_UP);
					p1Down  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_S) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_DOWN);
					p1Fire  = inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_SPACE) || inputManager.isP1KeyDown(java.awt.event.KeyEvent.VK_ENTER);
				}

				boolean isRightBorder = this.ship.getPositionX()
						+ this.ship.getWidth() + this.ship.getSpeed() > this.width - 1;
				boolean isLeftBorder = this.ship.getPositionX() - this.ship.getSpeed() < 1;
				boolean isUpBorder = this.ship.getPositionY() - this.ship.getSpeed() < SEPARATION_LINE_HEIGHT;
				boolean isDownBorder = this.ship.getPositionY()
						+ this.ship.getHeight() + this.ship.getSpeed() > ITEMS_SEPARATION_LINE_HEIGHT;

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

			if (this.isTwoPlayerMode && this.shipP2 != null && this.livesP2 > 0 && !this.shipP2.isDestroyed()) {
				boolean p2Right = false;
				boolean p2Left  = false;
				boolean p2Up    = false;
				boolean p2Down  = false;
				boolean p2Fire  = false;

                if (this.isAIMode) {
                    if (externalMoveX == 1) {
                        p2Right = true;
                    } else if (externalMoveX == -1) {
                        p2Left = true;
                    }
                    if (externalMoveY == 1) {
                        p2Down = true;
                    } else if (externalMoveY == -1) {
                        p2Up = true;
                    }
                    p2Fire = externalShoot;
                }
                else {
                    p2Right = inputManager.isP2KeyDown(java.awt.event.KeyEvent.VK_RIGHT);
                    p2Left  = inputManager.isP2KeyDown(java.awt.event.KeyEvent.VK_LEFT);
                    p2Up    = inputManager.isP2KeyDown(java.awt.event.KeyEvent.VK_UP);
                    p2Down  = inputManager.isP2KeyDown(java.awt.event.KeyEvent.VK_DOWN);
                    p2Fire  = inputManager.isP2KeyDown(java.awt.event.KeyEvent.VK_ENTER);
                }

				boolean p2RightBorder = this.shipP2.getPositionX()
						+ this.shipP2.getWidth() + this.shipP2.getSpeed() > this.width - 1;
				boolean p2LeftBorder = this.shipP2.getPositionX() - this.shipP2.getSpeed() < 1;
				boolean p2UpBorder = this.shipP2.getPositionY() - this.shipP2.getSpeed() < SEPARATION_LINE_HEIGHT;
				boolean p2DownBorder = this.shipP2.getPositionY()
						+ this.shipP2.getHeight() + this.shipP2.getSpeed() > ITEMS_SEPARATION_LINE_HEIGHT;

				if (p2Right && !p2RightBorder) this.shipP2.moveRight();
				if (p2Left  && !p2LeftBorder)  this.shipP2.moveLeft();
				if (p2Up    && !p2UpBorder)    this.shipP2.moveUp();
				if (p2Down  && !p2DownBorder)  this.shipP2.moveDown();

				if (p2Fire) {
					if (this.shipP2.shoot(this.bullets)) {
						this.bulletsShot++;
					}
				}
			}
			switch (this.currentPhase) {
				case wave:
					if (!DropItem.isTimeFreezeActive()) {
						this.enemyShipFormation.update();
						this.enemyShipFormation.shoot(this.bullets);
					}
					if (this.enemyShipFormation.isEmpty()) {
						this.currentPhase = StagePhase.boss_wave;
					}
					break;
				case boss_wave:
					if (this.finalBoss == null && this.omegaBoss == null){
						bossReveal();
						this.enemyShipFormation.clear();
					}
					if(this.finalBoss != null){
						finalbossManage();
					}
					else if (this.omegaBoss != null){
						this.omegaBoss.update();
						if (this.omegaBoss.isDestroyed()) {
							if ("omegaAndFinal".equals(this.currentlevel.getBossId())) {
								this.omegaBoss = null;
                                this.finalBoss = new FinalBoss(this.width / 2 - 50, 50, this.width, this.height);
                                this.logger.info("Final Boss has spawned!");
							} else {
								this.levelFinished = true;
								this.screenFinishedCooldown.reset();
							}
						}
					}
					else{
						if(!this.levelFinished){
							this.levelFinished = true;
							this.screenFinishedCooldown.reset();
						}
					}
					break;
			}
			this.ship.update();
			if (this.isTwoPlayerMode && this.shipP2 != null) {
				this.shipP2.update();
			}
			// special enemy update
			this.enemyShipSpecialFormation.update();
		}

		if (this.gameTimer.isRunning()) {
            this.elapsedTime = this.gameTimer.getElapsedTime();
				AchievementManager.getInstance().onTimeElapsedSeconds((int)(this.elapsedTime / 1000));
        }
        cleanItems();
        collisionManager.manageCollisions();
		cleanBullets();
		draw();

        if (Core.isAITraining && this.livesP2 <= 0 && !this.levelFinished) {

            // Core 루프 종료 위해 P1도 0으로 만들어준다
            this.livesP1 = 0;

            this.levelFinished = true;
            this.screenFinishedCooldown.reset();
            if (this.gameTimer.isRunning()) {
                this.gameTimer.stop();
            }
        }

		if ((this.livesP1 == 0 && (!this.isTwoPlayerMode || this.livesP2 == 0)) && !this.levelFinished) {
			this.levelFinished = true;
			this.screenFinishedCooldown.reset();
			if (this.gameTimer.isRunning()) {
				this.gameTimer.stop();
			}

			if ((this.livesP1 > 0) || (this.isTwoPlayerMode && this.shipP2 != null && this.livesP2 > 0)) {
				if (this.level == 1) {
					AchievementManager.getInstance().unlockAchievement("Beginner");
				} else if (this.level == 3) {
					AchievementManager.getInstance().unlockAchievement("Intermediate");
				}
			}
		}
		if (this.levelFinished && this.screenFinishedCooldown.checkFinished()) {
			if (this.livesP1 > 0 || (this.isTwoPlayerMode && this.shipP2 != null && this.livesP2 > 0)) { // Check for win condition
				if (this.currentlevel.getCompletionBonus() != null) {
					this.coin += this.currentlevel.getCompletionBonus().getCurrency();
					this.logger.info("Awarded " + this.currentlevel.getCompletionBonus().getCurrency() + " coins for level completion.");
				}

				String achievement = this.currentlevel.getAchievementTrigger();
				if (achievement != null && !achievement.isEmpty()) {
					AchievementManager.getInstance().unlockAchievement(achievement);
					this.logger.info("Unlocked achievement: " + achievement);
				}
			}
			this.isRunning = false;
		}

        for (EnemyShip enemy : enemyShipFormation) {
            int enemyHp = enemy.getHealth();
            int enemyMaxHp = enemy.getMaxHealth();

            Color color = getColorForHealth(enemyHp, enemyMaxHp);
            enemy.setColor(color);
        }
	}


	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);

		if (this.livesP1 > 0) {
			drawManager.drawEntity(this.ship, this.ship.getPositionX(),
					this.ship.getPositionY());
		}

		if (this.isTwoPlayerMode && this.shipP2 != null && this.livesP2 > 0) {
			drawManager.drawEntity(this.shipP2, this.shipP2.getPositionX(), this.shipP2.getPositionY());
		}

		// special enemy draw
		enemyShipSpecialFormation.draw();

		/** draw final boss at the field */
		/** draw final boss bullets */
		if(this.finalBoss != null && !this.finalBoss.isDestroyed()){
			for (BossBullet bossBullet : bossBullets) {
				drawManager.drawEntity(bossBullet, bossBullet.getPositionX(), bossBullet.getPositionY());
			}
			drawManager.drawEntity(finalBoss, finalBoss.getPositionX(), finalBoss.getPositionY());
			drawManager.drawBossHealthBar(finalBoss.getPositionX(),finalBoss.getPositionY(), "FINAL",
					finalBoss.getHealPoint(), finalBoss.getMaxHp());
		}

		enemyShipFormation.draw();

		if(this.omegaBoss != null) {
			this.omegaBoss.draw(drawManager);
		}

		if(this.omegaBoss != null && !this.omegaBoss.isDestroyed()) {
			this.omegaBoss.draw(drawManager);
			drawManager.drawBossHealthBar(omegaBoss.getPositionX(),omegaBoss.getPositionY(), "OMEGA",
					this.omegaBoss.getHealPoint(), this.omegaBoss.getMaxHp());
		}

		for (Bullet bullet : this.bullets)
			drawManager.drawEntity(bullet, bullet.getPositionX(),
					bullet.getPositionY());

		for (DropItem dropItem : this.dropItems)
			drawManager.drawEntity(dropItem, dropItem.getPositionX(), dropItem.getPositionY());

		// Interface.
        drawManager.drawScore(this, this.scoreP1);   // Top line still displays P1
		if(this.isTwoPlayerMode) {
			drawManager.drawScoreP2(this, this.scoreP2); // Added second line for P2
		}
        drawManager.drawCoin(this,this.coin);
		drawManager.drawLives(this, this.livesP1);
		if(this.isTwoPlayerMode) {
			drawManager.drawLivesP2(this, this.livesP2);
		}
		drawManager.drawTime(this, this.elapsedTime);
		drawManager.drawItemsHUD(this);
		drawManager.drawLevel(this, this.currentLevel.getLevelName());
		drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);
		drawManager.drawHorizontalLine(this, ITEMS_SEPARATION_LINE_HEIGHT);

		if (this.achievementText != null && !this.achievementPopupCooldown.checkFinished()) {
			drawManager.drawAchievementPopup(this, this.achievementText);
		} else {
			this.achievementText = null; // clear once expired
		}

		// Health notification popup
		if(this.healthPopupText != null && !this.healthPopupCooldown.checkFinished()) {
			drawManager.drawHealthPopup(this, this.healthPopupText);
		} else {
			this.healthPopupText = null;
		}

		// Countdown to game start.
		if (!this.inputDelay.checkFinished()) {
			int countdown = (int) ((INPUT_DELAY
					- (System.currentTimeMillis()
					- this.gameStartTime)) / 1000);
			drawManager.drawCountDown(this, this.level, countdown,
					this.bonusLife);
			drawManager.drawHorizontalLine(this, this.height / 2 - this.height
					/ 12);
			drawManager.drawHorizontalLine(this, this.height / 2 + this.height
					/ 12);
		}

		drawManager.completeDrawing(this);
	}


	/**
	 * Cleans bullets that go off screen.
	 */
	private void cleanBullets() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets) {
			bullet.update();
			if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT
					|| bullet.getPositionY() > this.height)
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
            if (dropItem.getPositionY() < SEPARATION_LINE_HEIGHT
                    || dropItem.getPositionY() > this.height)
                recyclable.add(dropItem);
        }
        this.dropItems.removeAll(recyclable);
        ItemPool.recycle(recyclable);
    }
    /**
     * Shows an achievement popup message on the HUD.
     *
     * @param message
     *      Text to display in the popup.
     */
    public void showAchievement(String message) {
        this.achievementText = message;
        this.achievementPopupCooldown = Core.getCooldown(2500); // Show for 2.5 seconds
        this.achievementPopupCooldown.reset();
    }

    /**
     * Displays a notification popup when the player gains or loses health
     *
     * @param message
     *          Text to display in the popup
     */

    public void showHealthPopup(String message) {
        this.healthPopupText = message;
        this.healthPopupCooldown = Core.getCooldown(500);
        this.healthPopupCooldown.reset();
    }

    /**
	 * Returns a GameState object representing the status of the game.
	 * 
	 * @return Current game state.
	 */
	    public final GameState getGameState() {
	        if (this.coin > 2000) {
	            AchievementManager.getInstance().unlockAchievement("Mr. Greedy");
	        }
	        return new GameState(this.level, this.score, this.scoreP1,this.scoreP2,this.livesP1,this.livesP2,
	                this.bulletsShot, this.shipsDestroyed,this.coin, this.isTwoPlayerMode, this.isAIMode);
	    }
	/**
	 * Adds one life to the player.
	 */
	public final void gainLife() {
		if (this.livesP1 < this.maxLives) {
			this.livesP1++;
		}
	}

	public final void gainLifeP2() {
		if (this.livesP2 < this.maxLives) {
			this.livesP2++;
		}
	}

	private void bossReveal() {
		String bossName = this.currentlevel.getBossId();

		if (bossName == null || bossName.isEmpty()) {
			this.logger.info("No boss for this level. Proceeding to finish.");
			return;
		}

		this.logger.info("Spawning boss: " + bossName);
		switch (bossName) {
			case "finalBoss":
				this.finalBoss = new FinalBoss(this.width / 2 - 50, 50, this.width, this.height);
				this.logger.info("Final Boss has spawned!");
				break;
			case "omegaBoss":
			case "omegaAndFinal":
				this.omegaBoss = new OmegaBoss(Color.ORANGE, ITEMS_SEPARATION_LINE_HEIGHT);
				omegaBoss.attach(this);
				this.logger.info("Omega Boss has spawned!");
				break;
			default:
				this.logger.warning("Unknown bossId: " + bossName);
				break;
		}
	}


	public void finalbossManage(){
		if (this.finalBoss != null && !this.finalBoss.isDestroyed()) {
			this.finalBoss.update();
			/** called the boss shoot logic */
			if (this.finalBoss.getHealPoint() > this.finalBoss.getMaxHp() / 4) {
				bossBullets.addAll(this.finalBoss.shoot1());
				bossBullets.addAll(this.finalBoss.shoot2());
			} else {
				/** Is the bullet on the screen erased */
				if (!is_cleared) {
					bossBullets.clear();
					is_cleared = true;
					logger.info("boss is angry");
				} else {
					bossBullets.addAll(this.finalBoss.shoot3());
				}
			}

			/** bullets to erase */
			Set<BossBullet> bulletsToRemove = new HashSet<>();

			for (BossBullet b : bossBullets) {
				b.update();
				/** If the bullet goes off the screen */
				if (b.isOffScreen(width, height)) {
					/** bulletsToRemove carry bullet */
					bulletsToRemove.add(b);
				}
			}
			/** all bullets are removed */
			bossBullets.removeAll(bulletsToRemove);

		}
		if (this.finalBoss != null && this.finalBoss.isDestroyed()) {
			this.levelFinished = true;
			this.screenFinishedCooldown.reset();
		}
	}

    public Color getColorForHealth(final int health, final int maxHealth) {
        double ratio = (double) health / maxHealth;

        if (ratio > 0.75) {
            return new Color(0x3DDC84); // Green: Full HP
        } else if (ratio > 0.5) {
            return new Color(0xFFC107); // Yellow: Middle HP
        } else if (ratio > 0.25) {
            return new Color(0xFF9800); // Orange: Low HP
        } else {
            return new Color(0xF44336); // Red: Critical HP
        }
    }
    /**
     * Updates external control input for player 1.
     * This method is called from Core via HTTP API.
     *
     * @param moveX Horizontal movement axis (-1, 0, 1).
     * @param moveY Vertical movement axis (-1, 0, 1).
     * @param shoot Whether the player should shoot.
     */
    public void handleExternalAction(final int moveX, final int moveY, final boolean shoot) {
        this.externalMoveX = moveX;
        this.externalMoveY = moveY;
        this.externalShoot = shoot;
    }

    public void setAImode(final boolean aimode) {
        isAIMode = aimode;
    }

    // === Enemy damage buffer for RL ===
    private final List<List<Integer>> enemyDamageEvents = new ArrayList<>();

    public void recordEnemyDamage(int enemyId, int damage) {
        List<Integer> ev = new ArrayList<>();
        ev.add(enemyId);
        ev.add(damage);
        enemyDamageEvents.add(ev);
    }


    public StatePacket buildStatePacket() {
        StatePacket packet = new StatePacket();

        // 1. Frame index
        packet.frame = (int)(System.currentTimeMillis() / 16);  // 대략 60fps 기준

        // 2. Player info
        if (this.shipP2 != null) {
            packet.playerX = this.shipP2.getPositionX();
            packet.playerY = this.shipP2.getPositionY();
            packet.playerHp = this.livesP2;
        } else {
            // fallback or safe default
            packet.playerX = -1;
            packet.playerY = -1;
            packet.playerHp = 0;
        }

        // 3. Bullets info
        packet.bullets = new ArrayList<>();
        for (Bullet b : this.bullets) {
            List<Integer> bullet_info = new ArrayList<>();
            bullet_info.add(b.getPositionX());
            bullet_info.add(b.getPositionY());
            bullet_info.add(b.getOwnerId());
            packet.bullets.add(bullet_info);
        }
        for (BossBullet b : this.bossBullets) {
            List<Integer> bullet_info = new ArrayList<>();
            bullet_info.add(b.getPositionX());
            bullet_info.add(b.getPositionY());
            bullet_info.add(-1);
            packet.bullets.add(bullet_info);
        }


        // 4. Enemies info
        packet.enemies = new ArrayList<>();
        for (EnemyShip e : this.enemyShipFormation) {
            if (!e.isDestroyed()) {
                List<Integer> enemy_info = new ArrayList<>();
                enemy_info.add(e.getPositionX());
                enemy_info.add(e.getPositionY());
                enemy_info.add(e.getHealth());
                int enemy_ship_type = switch (e.getEnemyType()) {
                    case "enemyA" -> 1;
                    case "enemyB" -> 2;
                    case "enemyC" -> 3;
                    default -> 0;
                };
                enemy_info.add(enemy_ship_type);
                packet.enemies.add(enemy_info);
            }
        }
        if (this.finalBoss != null && !this.finalBoss.isDestroyed()) {
            List<Integer> enemy_info = new ArrayList<>();
            enemy_info.add(finalBoss.getPositionX());
            enemy_info.add(finalBoss.getPositionY());
            enemy_info.add(finalBoss.getHealPoint());
            int enemy_ship_type = 1;
            enemy_info.add(enemy_ship_type);
            packet.enemies.add(enemy_info);
        }if (this.omegaBoss != null && !this.omegaBoss.isDestroyed()) {
            List<Integer> enemy_info = new ArrayList<>();
            enemy_info.add(omegaBoss.getPositionX());
            enemy_info.add(omegaBoss.getPositionY());
            enemy_info.add(omegaBoss.getHealPoint());
            int enemy_ship_type = 1;
            enemy_info.add(enemy_ship_type);
            packet.enemies.add(enemy_info);
        }

        // 5. Items info
        packet.items = new ArrayList<>();
        for (DropItem d : this.dropItems) {
            List<String> item_info = new ArrayList<>();
            item_info.add(String.valueOf(d.getPositionX()));
            item_info.add(String.valueOf(d.getPositionY()));
            item_info.add(d.getItemType().toString());
            packet.items.add(item_info);
        }

        // 6. Score
        packet.score = this.scoreP2;

        // === 7. Enemy Damage Events ===
        packet.enemyDamageEvents = new ArrayList<>(enemyDamageEvents);
        enemyDamageEvents.clear();

        return packet;
    }

    // Getters and Setters for CollisionManager
	@Override
	public InfiniteEnemyFormation getInfiniteEnemyFormation() {
		return null;
	}
    public Set<Bullet> getBullets() { return this.bullets; }
    public Set<BossBullet> getBossBullets() { return this.bossBullets; }
    public int getLivesP1() { return this.livesP1; }
    public void setLivesP1(int lives) { this.livesP1 = lives; }
    public Ship getShip() { return this.ship; }
    public boolean isLevelFinished() { return !this.levelFinished; }
    public Logger getLogger() { return this.logger; }
    public boolean isTwoPlayerMode() { return this.isTwoPlayerMode; }
    public Ship getShipP2() { return this.shipP2; }
    public int getLivesP2() { return this.livesP2; }
    public void setLivesP2(int lives) { this.livesP2 = lives; }
    public EnemyShipFormation getEnemyShipFormation() { return this.enemyShipFormation; }
    public int getCoin() { return this.coin; }
    public void setCoin(int coin) { this.coin = coin; }
    public int getShipsDestroyed() { return this.shipsDestroyed; }
    public void setShipsDestroyed(int shipsDestroyed) { this.shipsDestroyed = shipsDestroyed; }
    public Level getCurrentLevel() { return this.currentLevel; }
    public Set<DropItem> getDropItems() { return this.dropItems; }
    public EnemyShipSpecialFormation getEnemyShipSpecialFormation() { return this.enemyShipSpecialFormation; }
    public MidBoss getOmegaBoss() { return this.omegaBoss; }
    public FinalBoss getFinalBoss() { return this.finalBoss; }


    //Setters for Testing
    public void setShipP2(Ship shipP2) { this.shipP2 = shipP2; }
    public void setBullets(Set<Bullet> bullets) { this.bullets = bullets; }
    public void setDropItems(Set<DropItem> dropItems) { this.dropItems = dropItems; }
    public void setBossBullets(Set<BossBullet> bullets) { this.bossBullets = bullets; }
    public void setEnemyShipFormation(EnemyShipFormation enemyShipFormation) {this.enemyShipFormation = enemyShipFormation; };
    public void setFinalBoss(FinalBoss finalBoss) { this.finalBoss = finalBoss; };
    public void setScoreP2(int scoreP2) { this.scoreP2 = scoreP2; }
}
