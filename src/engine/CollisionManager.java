package engine;

import entity.*;
import screen.GameScreen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import entity.BossBullet;


public class CollisionManager {

    private GameScreen gameScreen;

    public CollisionManager(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    public void manageCollisions() {
        manageBulletShipCollisions();
        manageBossBulletCollisions();
        manageShipEnemyCollisions();
        manageItemCollisions();
    }

    private void manageBossBulletCollisions() {
        Set<BossBullet> bulletsToRemove = new HashSet<>();

        for (BossBullet b : gameScreen.getBossBullets()) {
            // Collision with ship 1
            if (gameScreen.getLivesP1() > 0 && checkCollision(b, gameScreen.getShip())) {
                if (!gameScreen.getShip().isDestroyed()) {
                    gameScreen.getShip().destroy();
                    gameScreen.setLivesP1(gameScreen.getLivesP1() - 1);
                    gameScreen.getLogger().info("Hit on player ship, " + gameScreen.getLivesP1() + " lives remaining.");
                }
                bulletsToRemove.add(b);
            }
            // Collision with ship 2
            else if (gameScreen.isTwoPlayerMode() && gameScreen.getShipP2() != null && gameScreen.getLivesP2() > 0 && !gameScreen.getShipP2().isDestroyed() && checkCollision(b, gameScreen.getShipP2())) {
                if (!gameScreen.getShipP2().isDestroyed()) {
                    gameScreen.getShipP2().destroy();
                    gameScreen.setLivesP2(gameScreen.getLivesP2() - 1);
                    gameScreen.getLogger().info("Hit on player ship, " + gameScreen.getLivesP2() + " lives remaining.");
                }
                bulletsToRemove.add(b);
            }
        }
        gameScreen.getBossBullets().removeAll(bulletsToRemove);
    }

    /**
     * Handles all interactions between bullets and ships or enemy entities on the game screen.
     *
     * Processes collisions for player and enemy bullets: applies damage to ships, enemies, and bosses; decrements player lives and shows health feedback when player ships are hit (respecting invincibility and destroyed states); awards points and coins and increments destroyed counters for defeated enemies; triggers item drops from defeated enemies; unlocks or checks achievements when appropriate; and collects bullets that should be removed or recycled (respecting bullet penetration behavior).
     */
	private void manageBulletShipCollisions() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.gameScreen.getBullets())
			if (bullet.getSpeed() > 0) {
				if (this.gameScreen.getLivesP1() > 0 && checkCollision(bullet, this.gameScreen.getShip()) && !this.gameScreen.isLevelFinished()) {
					recyclable.add(bullet);
					if (!this.gameScreen.getShip().isInvincible()) {
						if (!this.gameScreen.getShip().isDestroyed()) {
							this.gameScreen.getShip().destroy();
							this.gameScreen.setLivesP1(this.gameScreen.getLivesP1() - 1);
							this.gameScreen.showHealthPopup("-1 Health");
							this.gameScreen.getLogger().info("Hit on player ship, " + this.gameScreen.getLivesP1()
									+ " lives remaining.");
						}
					}
				} else if (this.gameScreen.isTwoPlayerMode() && this.gameScreen.getShipP2() != null && this.gameScreen.getLivesP2() > 0 && !this.gameScreen.getShipP2().isDestroyed()
						&& checkCollision(bullet, this.gameScreen.getShipP2()) && !this.gameScreen.isLevelFinished()) {
					recyclable.add(bullet);
					if (!this.gameScreen.getShipP2().isInvincible()) {
						if (!this.gameScreen.getShipP2().isDestroyed()) {
							this.gameScreen.getShipP2().destroy();
							this.gameScreen.setLivesP2(this.gameScreen.getLivesP2() - 1);
							this.gameScreen.showHealthPopup("-1 Health");
							this.gameScreen.getLogger().info("Hit on player ship, " + this.gameScreen.getLivesP2()
									+ " lives remaining.");
						}
					}
				}
			} else {
				for (EnemyShip enemyShip : this.gameScreen.getEnemyShipFormation())
					if (!enemyShip.isDestroyed()
							&& checkCollision(bullet, enemyShip)) {

                        boolean beforeHit = enemyShip.getHealth() != 0;
						if (!bullet.checkAlreadyHit(enemyShip)) {
							bullet.addEnemyShip(enemyShip);
							enemyShip.takeDamage(1); // Implement user ship damage
						}
						else
							break;
                        boolean afterHit = enemyShip.getHealth() == 0;

                        if (beforeHit && afterHit) {
                            int pts = enemyShip.getPointValue();
                            this.gameScreen.addPointsFor(bullet, pts);
                            this.gameScreen.setCoin(this.gameScreen.getCoin() + (pts / 10));
                            this.gameScreen.setShipsDestroyed(this.gameScreen.getShipsDestroyed() + 1);
                            String enemyType = enemyShip.getEnemyType();
                            this.gameScreen.getEnemyShipFormation().destroy(enemyShip);
                            AchievementManager.getInstance().checkKillAchievements(this.gameScreen.getGameState());
                            if (enemyType != null && this.gameScreen.getCurrentLevel().getItemDrops() != null) {
                                List<engine.level.ItemDrop> potentialDrops = new ArrayList<>();
                                for (engine.level.ItemDrop itemDrop : this.gameScreen.getCurrentLevel().getItemDrops()) {
                                    if (enemyType.equals(itemDrop.getEnemyType())) {
                                        potentialDrops.add(itemDrop);
                                    }
                                }

                                List<engine.level.ItemDrop> successfulDrops = new ArrayList<>();
                                for (engine.level.ItemDrop itemDrop : potentialDrops) {
                                    if (Math.random() < itemDrop.getDropChance()) {
                                        successfulDrops.add(itemDrop);
                                    }
                                }

                                if (!successfulDrops.isEmpty()) {
                                    engine.level.ItemDrop selectedDrop = successfulDrops.get((int) (Math.random() * successfulDrops.size()));
                                    DropItem.ItemType droppedType = DropItem.fromString(selectedDrop.getItemId());
                                    if (droppedType != null) {
                                        final int ITEM_DROP_SPEED = 2;

                                        DropItem newDropItem = ItemPool.getItem(
                                                enemyShip.getPositionX() + enemyShip.getWidth() / 2,
                                                enemyShip.getPositionY() + enemyShip.getHeight() / 2,
                                                ITEM_DROP_SPEED,
                                                droppedType
                                        );
                                        this.gameScreen.getDropItems().add(newDropItem);
                                        this.gameScreen.getLogger().info("An item (" + droppedType + ") dropped");
                                    }
                                }
                            }
                        }

						if (!bullet.penetration()) {
							recyclable.add(bullet);
							break;
						}
					}

				// special enemy bullet event
				for (EnemyShip enemyShipSpecial : this.gameScreen.getEnemyShipSpecialFormation())
					if (enemyShipSpecial != null && !enemyShipSpecial.isDestroyed()
							&& checkCollision(bullet, enemyShipSpecial)) {
                        int pts = enemyShipSpecial.getPointValue();
                        this.gameScreen.addPointsFor(bullet, pts);
                        this.gameScreen.setCoin(this.gameScreen.getCoin() + (pts / 10));
                        this.gameScreen.setShipsDestroyed(this.gameScreen.getShipsDestroyed() + 1);
						this.gameScreen.getEnemyShipSpecialFormation().destroy(enemyShipSpecial);
						recyclable.add(bullet);
					}
				if (this.gameScreen.getOmegaBoss() != null
						&& !this.gameScreen.getOmegaBoss().isDestroyed()
						&& checkCollision(bullet, this.gameScreen.getOmegaBoss())) {
					this.gameScreen.getOmegaBoss().takeDamage(2);
					if(this.gameScreen.getOmegaBoss().getHealPoint() <= 0) {
						this.gameScreen.setShipsDestroyed(this.gameScreen.getShipsDestroyed() + 1);
                        int pts = this.gameScreen.getOmegaBoss().getPointValue();
                        this.gameScreen.addPointsFor(bullet, pts);
                        this.gameScreen.setCoin(this.gameScreen.getCoin() + (pts / 10));
                        this.gameScreen.getOmegaBoss().destroy();
						AchievementManager.getInstance().unlockAchievement("Boss Slayer");
					}
					recyclable.add(bullet);
				}

				/** when final boss collide with bullet */
				if(this.gameScreen.getFinalBoss() != null && !this.gameScreen.getFinalBoss().isDestroyed() && checkCollision(bullet,this.gameScreen.getFinalBoss())){
					this.gameScreen.getFinalBoss().takeDamage(1);
					if(this.gameScreen.getFinalBoss().getHealPoint() <= 0){
                        int pts = this.gameScreen.getFinalBoss().getPointValue();
                        this.gameScreen.addPointsFor(bullet, pts);
                        this.gameScreen.setCoin(this.gameScreen.getCoin() + (pts / 10));
						this.gameScreen.getFinalBoss().destroy();
                        AchievementManager.getInstance().unlockAchievement("Boss Slayer");
					}
					recyclable.add(bullet);
				}
            }
        this.gameScreen.getBullets().removeAll(recyclable);
        BulletPool.recycle(recyclable);
    }

    /**
     * Manages collisions between player ship and enemy ships.
     * Player loses a life immediately upon collision with any enemy.
     */
    private void manageShipEnemyCollisions() {
        // ===== P1 collision check =====
        if (!this.gameScreen.isLevelFinished() && this.gameScreen.getLivesP1() > 0 && !this.gameScreen.getShip().isDestroyed()
                && !this.gameScreen.getShip().isInvincible()) {
            // Check collision with normal enemy ships
            for (EnemyShip enemyShip : this.gameScreen.getEnemyShipFormation()) {
                if (!enemyShip.isDestroyed() && checkCollision(this.gameScreen.getShip(), enemyShip)) {
                    this.gameScreen.getEnemyShipFormation().destroy(enemyShip);
                    this.gameScreen.getShip().destroy();
                    this.gameScreen.setLivesP1(this.gameScreen.getLivesP1() - 1);
                    this.gameScreen.showHealthPopup("-1 Life (Collision!)");
                    this.gameScreen.getLogger().info("Ship collided with enemy! " + this.gameScreen.getLivesP1()
                            + " lives remaining.");
                    return;
                }
            }

            // Check collision with special enemy formation (red/blue ships)
            for (EnemyShip enemyShipSpecial : this.gameScreen.getEnemyShipSpecialFormation()) {
                if (enemyShipSpecial != null && !enemyShipSpecial.isDestroyed()
                        && checkCollision(this.gameScreen.getShip(), enemyShipSpecial)) {
                    enemyShipSpecial.destroy();
                    this.gameScreen.getShip().destroy();
                    this.gameScreen.setLivesP1(this.gameScreen.getLivesP1() - 1);
                    this.gameScreen.showHealthPopup("-1 Life (Collision!)");
                    this.gameScreen.getLogger().info("Ship collided with special enemy formation! "
                            + this.gameScreen.getLivesP1() + " lives remaining.");
                    return;
                }
            }

            // Check collision with omega boss (mid boss - yellow/pink ship)
            if (this.gameScreen.getOmegaBoss() != null && !this.gameScreen.getOmegaBoss().isDestroyed()
                    && checkCollision(this.gameScreen.getShip(), this.gameScreen.getOmegaBoss())) {
                this.gameScreen.getShip().destroy();
                this.gameScreen.setLivesP1(this.gameScreen.getLivesP1() - 1);
                this.gameScreen.showHealthPopup("-1 Life (Boss Collision!)");
                this.gameScreen.getLogger().info("Ship collided with omega boss! " + this.gameScreen.getLivesP1()
                        + " lives remaining.");
                return;
            }

            // Check collision with final boss
            if (this.gameScreen.getFinalBoss() != null && !this.gameScreen.getFinalBoss().isDestroyed()
                    && checkCollision(this.gameScreen.getShip(), this.gameScreen.getFinalBoss())) {
                this.gameScreen.getShip().destroy();
                this.gameScreen.setLivesP1(this.gameScreen.getLivesP1() - 1);
                this.gameScreen.showHealthPopup("-1 Life (Boss Collision!)");
                this.gameScreen.getLogger().info("Ship collided with final boss! " + this.gameScreen.getLivesP1()
                        + " lives remaining.");
                return;
            }
        }

        // ===== P2 collision check =====
        if (this.gameScreen.isTwoPlayerMode() && !this.gameScreen.isLevelFinished() && this.gameScreen.getShipP2() != null && this.gameScreen.getLivesP2() > 0
                && !this.gameScreen.getShipP2().isDestroyed() && !this.gameScreen.getShipP2().isInvincible()) {
            // Check collision with normal enemy ships
            for (EnemyShip enemyShip : this.gameScreen.getEnemyShipFormation()) {
                if (!enemyShip.isDestroyed() && checkCollision(this.gameScreen.getShipP2(), enemyShip)) {
	                this.gameScreen.getEnemyShipFormation().destroy(enemyShip);
                    this.gameScreen.getShipP2().destroy();
                    this.gameScreen.setLivesP2(this.gameScreen.getLivesP2() - 1);
                    this.gameScreen.showHealthPopup("-1 Life (Collision!)");
                    this.gameScreen.getLogger().info("Ship P2 collided with enemy! " + this.gameScreen.getLivesP2()
                            + " lives remaining.");
                    return;
                }
            }

            // Check collision with special enemy formation
            for (EnemyShip enemyShipSpecial : this.gameScreen.getEnemyShipSpecialFormation()) {
                if (enemyShipSpecial != null && !enemyShipSpecial.isDestroyed()
                        && checkCollision(this.gameScreen.getShipP2(), enemyShipSpecial)) {
                    enemyShipSpecial.destroy();
                    this.gameScreen.getShipP2().destroy();
                    this.gameScreen.setLivesP2(this.gameScreen.getLivesP2() - 1);
                    this.gameScreen.showHealthPopup("-1 Life (Collision!)");
                    this.gameScreen.getLogger().info("Ship P2 collided with special enemy formation! "
                            + this.gameScreen.getLivesP2() + " lives remaining.");
                    return;
                }
            }

            // Check collision with omega boss
            if (this.gameScreen.getOmegaBoss() != null && !this.gameScreen.getOmegaBoss().isDestroyed()
                    && checkCollision(this.gameScreen.getShipP2(), this.gameScreen.getOmegaBoss())) {
                this.gameScreen.getShipP2().destroy();
                this.gameScreen.setLivesP2(this.gameScreen.getLivesP2() - 1);
                this.gameScreen.showHealthPopup("-1 Life (Boss Collision!)");
                this.gameScreen.getLogger().info("Ship P2 collided with omega boss! " + this.gameScreen.getLivesP2()
                        + " lives remaining.");
                return;
            }

            // Check collision with final boss
            if (this.gameScreen.getFinalBoss() != null && !this.gameScreen.getFinalBoss().isDestroyed()
                    && checkCollision(this.gameScreen.getShipP2(), this.gameScreen.getFinalBoss())) {
                this.gameScreen.getShipP2().destroy();
                this.gameScreen.setLivesP2(this.gameScreen.getLivesP2() - 1);
                this.gameScreen.showHealthPopup("-1 Life (Boss Collision!)");
                this.gameScreen.getLogger().info("Ship P2 collided with final boss! " + this.gameScreen.getLivesP2()
                        + " lives remaining.");
                return;
            }
        }
    }

    /**
     * Manages collisions between player ship and dropped items.
     * Applies item effects when player collects them.
     */
    private void manageItemCollisions() {
        Set<DropItem> acquiredDropItems = new HashSet<DropItem>();

		if (!this.gameScreen.isLevelFinished() && ((this.gameScreen.getLivesP1() > 0 && !this.gameScreen.getShip().isDestroyed())
				|| (this.gameScreen.getShipP2() != null && this.gameScreen.getLivesP2() > 0 && !this.gameScreen.getShipP2().isDestroyed()))) {
			for (DropItem dropItem : this.gameScreen.getDropItems()) {

				if (this.gameScreen.getLivesP1() > 0 && !this.gameScreen.getShip().isDestroyed() && checkCollision(this.gameScreen.getShip(), dropItem)) {
					this.gameScreen.getLogger().info("Player acquired dropItem: " + dropItem.getItemType());

					// Add item to HUD display
					ItemHUDManager.getInstance().addDroppedItem(dropItem.getItemType());

					switch (dropItem.getItemType()) {
						case Heal:
							this.gameScreen.gainLife();
							break;
						case Shield:
							this.gameScreen.getShip().activateInvincibility(5000); // 5 seconds of invincibility
							break;
						case Stop:
							DropItem.applyTimeFreezeItem(3000);
							break;
						case Push:
							DropItem.PushbackItem(this.gameScreen.getEnemyShipFormation(),20);
							break;
						case Explode:
							int pts = 0;
                            // Circuit all enemies
                            for (EnemyShip enemyShip : this.gameScreen.getEnemyShipFormation()) {

                                if (enemyShip != null && !enemyShip.isDestroyed()) {

                                    enemyShip.takeDamage(1);
                                    boolean afterHit = enemyShip.getHealth() == 0;
                                    if (afterHit) {
                                        pts += enemyShip.getPointValue();
                                        this.gameScreen.setShipsDestroyed(this.gameScreen.getShipsDestroyed() + 1);
                                        this.gameScreen.getEnemyShipFormation().destroy(enemyShip);
                                    }
                                }
                            }
                            this.gameScreen.addPointsFor(null, pts);
                            break;
						case Slow:
							this.gameScreen.getEnemyShipFormation().activateSlowdown();
							this.gameScreen.getLogger().info("Enemy formation slowed down!");
							break;
						default:
							// For other dropItem types. Free to add!
							break;
					}
					acquiredDropItems.add(dropItem);
				} else if (this.gameScreen.isTwoPlayerMode() && this.gameScreen.getShipP2() != null && this.gameScreen.getLivesP2() > 0 && !this.gameScreen.getShipP2().isDestroyed()
						&& checkCollision(this.gameScreen.getShipP2(), dropItem)) {
					this.gameScreen.getLogger().info("Player acquired dropItem: " + dropItem.getItemType());

					// Add item to HUD display
					ItemHUDManager.getInstance().addDroppedItem(dropItem.getItemType());

					switch (dropItem.getItemType()) {
						case Heal:
							this.gameScreen.gainLifeP2();
							break;
						case Shield:
							this.gameScreen.getShipP2().activateInvincibility(5000); // 5 seconds of invincibility
							break;
						case Stop:
							DropItem.applyTimeFreezeItem(3000);
							break;
						case Push:
							DropItem.PushbackItem(this.gameScreen.getEnemyShipFormation(),20);
							break;
						case Explode:
                            int pts2 = 0;
                            // Circuit all enemies
                            for (EnemyShip enemyShip : this.gameScreen.getEnemyShipFormation()) {

                                if (enemyShip != null && !enemyShip.isDestroyed()) {

                                    enemyShip.takeDamage(1);
                                    boolean afterHit = enemyShip.getHealth() == 0;
                                    if (afterHit) {
                                        pts2 += enemyShip.getPointValue();
                                        this.gameScreen.setShipsDestroyed(this.gameScreen.getShipsDestroyed() + 1);
                                        this.gameScreen.getEnemyShipFormation().destroy(enemyShip);
                                    }
                                }
                            }
                            this.gameScreen.addPointsFor(null, pts2);
                            break;
						case Slow:
							this.gameScreen.getEnemyShipFormation().activateSlowdown();
							this.gameScreen.getLogger().info("Enemy formation slowed down!");
							break;
						default:
							// For other dropItem types. Free to add!
							break;
					}
					acquiredDropItems.add(dropItem);
				}
			}
			this.gameScreen.getDropItems().removeAll(acquiredDropItems);
			ItemPool.recycle(acquiredDropItems);
		}
	}


	/**
	 * Checks if two entities are colliding.
	 *
	 * @param a
	 *            First entity, the bullet.
	 * @param b
	 *            Second entity, the ship.
	 * @return Result of the collision test.
	 */
	private boolean checkCollision(final Entity a, final Entity b) {
		// Calculate center point of the entities in both axis.
		int centerAX = a.getPositionX() + a.getWidth() / 2;
		int centerAY = a.getPositionY() + a.getHeight() / 2;
		int centerBX = b.getPositionX() + b.getWidth() / 2;
		int centerBY = b.getPositionY() + b.getHeight() / 2;
		// Calculate maximum distance without collision.
		int maxDistanceX = a.getWidth() / 2 + b.getWidth() / 2;
		int maxDistanceY = a.getHeight() / 2 + b.getHeight() / 2;
		// Calculates distance.
		int distanceX = Math.abs(centerAX - centerBX);
		int distanceY = Math.abs(centerAY - centerBY);

		return distanceX < maxDistanceX && distanceY < maxDistanceY;
	}
}