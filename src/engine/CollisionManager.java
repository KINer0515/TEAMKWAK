package engine;

import entity.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CollisionManager {

    private CollisionContext context;

    public CollisionManager(CollisionContext context) {
        this.context = context;
    }

    public void manageCollisions() {
        manageBulletShipCollisions();
        manageBossBulletCollisions();
        manageShipEnemyCollisions();
        manageItemCollisions();
    }

    private void manageBossBulletCollisions() {
        Set<BossBullet> bulletsToRemove = new HashSet<>();

        for (BossBullet b : context.getBossBullets()) {
            // Collision with ship 1
            if (context.getLivesP1() > 0 && checkCollision(b, context.getShip())) {
                if (!context.getShip().isDestroyed()) {
                    context.getShip().destroy();
                    context.setLivesP1(context.getLivesP1() - 1);
                    context.getLogger().info("Hit on player ship, " + context.getLivesP1() + " lives remaining.");
                }
                bulletsToRemove.add(b);
            }
            // Collision with ship 2
            else if (context.isTwoPlayerMode() && context.getShipP2() != null && context.getLivesP2() > 0 && !context.getShipP2().isDestroyed() && checkCollision(b, context.getShipP2())) {
                if (!context.getShipP2().isDestroyed()) {
                    context.getShipP2().destroy();
                    context.setLivesP2(context.getLivesP2() - 1);
                    context.getLogger().info("Hit on player ship, " + context.getLivesP2() + " lives remaining.");
                }
                bulletsToRemove.add(b);
            }
        }
        context.getBossBullets().removeAll(bulletsToRemove);
    }

    /**
     * Manages collisions between bullets and ships.
     */
    private void manageBulletShipCollisions() {
        Set<Bullet> recyclable = new HashSet<Bullet>();
        for (Bullet bullet : this.context.getBullets())
            if (bullet.getSpeed() > 0) {
                if (this.context.getLivesP1() > 0 && checkCollision(bullet, this.context.getShip()) && this.context.isLevelFinished()) {
                    recyclable.add(bullet);
                    if (!this.context.getShip().isInvincible()) {
                        if (!this.context.getShip().isDestroyed()) {
                            this.context.getShip().destroy();
                            this.context.setLivesP1(this.context.getLivesP1() - 1);
                            this.context.showHealthPopup("-1 Health");
                            this.context.getLogger().info("Hit on player ship, " + this.context.getLivesP1()
                                    + " lives remaining.");
                        }
                    }
                } else if (this.context.isTwoPlayerMode() && this.context.getShipP2() != null && this.context.getLivesP2() > 0 && !this.context.getShipP2().isDestroyed()
                        && checkCollision(bullet, this.context.getShipP2()) && this.context.isLevelFinished()) {
                    recyclable.add(bullet);
                    if (!this.context.getShipP2().isInvincible()) {
                        if (!this.context.getShipP2().isDestroyed()) {
                            this.context.getShipP2().destroy();
                            this.context.setLivesP2(this.context.getLivesP2() - 1);
                            this.context.showHealthPopup("-1 Health");
                            this.context.getLogger().info("Hit on player ship, " + this.context.getLivesP2()
                                    + " lives remaining.");
                        }
                    }
                }
            } else {
                // normal mode
                if (this.context.getEnemyShipFormation() != null) {
                    for (EnemyShip enemyShip : this.context.getEnemyShipFormation())
                        if (!enemyShip.isDestroyed()
                                && checkCollision(bullet, enemyShip)) {

                            boolean beforeHit = enemyShip.getHealth() != 0;
                            if (!bullet.checkAlreadyHit(enemyShip)) {
                                bullet.addEnemyShip(enemyShip);
                                enemyShip.takeDamage(1);
                            }
                            else
                                break;
                            boolean afterHit = enemyShip.getHealth() == 0;

                            if (beforeHit && afterHit) {
                                int pts = enemyShip.getPointValue();
                                this.context.addPointsFor(bullet, pts);
                                this.context.setCoin(this.context.getCoin() + (pts / 10));
                                this.context.setShipsDestroyed(this.context.getShipsDestroyed() + 1);
                                String enemyType = enemyShip.getEnemyType();
                                this.context.getEnemyShipFormation().destroy(enemyShip);
                                AchievementManager.getInstance().checkKillAchievements(this.context.getGameState());
                                if (enemyType != null && this.context.getCurrentLevel() != null && this.context.getCurrentLevel().getItemDrops() != null) {
                                    List<engine.level.ItemDrop> potentialDrops = new ArrayList<>();
                                    for (engine.level.ItemDrop itemDrop : this.context.getCurrentLevel().getItemDrops()) {
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
                                            this.context.getDropItems().add(newDropItem);
                                            this.context.getLogger().info("An item (" + droppedType + ") dropped");
                                        }
                                    }
                                }
                            }

                            if (!bullet.penetration()) {
                                recyclable.add(bullet);
                                break;
                            }
                        }
                }

                // inf mode
                if (this.context.getInfiniteEnemyFormation() != null) {
                    for (InfiniteEnemyShip infiniteEnemy : this.context.getInfiniteEnemyFormation()) {
                        if (!infiniteEnemy.isDestroyed() && checkCollision(bullet, infiniteEnemy)) {
                            infiniteEnemy.takeDamage(1);

                            if (infiniteEnemy.isDestroyed()) {
                                int pts = infiniteEnemy.getPointValue();
                                this.context.addPointsFor(bullet, pts);
                                this.context.setCoin(this.context.getCoin() + (pts / 10));
                                this.context.setShipsDestroyed(this.context.getShipsDestroyed() + 1);
                                AchievementManager.getInstance().checkKillAchievements(this.context.getGameState());
                                this.context.getLogger().info("Infinite enemy destroyed! Points: " + pts);

                                // inf mode item drop
                                if (Math.random() < 0.15) {
                                    DropItem.ItemType droppedType = DropItem.ItemType.selectItemType();
                                    if (droppedType != null) {
                                        final int ITEM_DROP_SPEED = 2;
                                        DropItem newDropItem = ItemPool.getItem(
                                                infiniteEnemy.getPositionX() + infiniteEnemy.getWidth() / 2,
                                                infiniteEnemy.getPositionY() + infiniteEnemy.getHeight() / 2,
                                                ITEM_DROP_SPEED,
                                                droppedType
                                        );
                                        this.context.getDropItems().add(newDropItem);
                                        this.context.getLogger().info("Infinite mode: An item (" + droppedType + ") dropped");
                                    }
                                }
                            }

                            if (!bullet.penetration()) {
                                recyclable.add(bullet);
                                break;
                            }
                        }
                    }
                }

                // special enemy bullet event
                if (this.context.getEnemyShipSpecialFormation() != null) {
                    for (EnemyShip enemyShipSpecial : this.context.getEnemyShipSpecialFormation())
                        if (enemyShipSpecial != null && !enemyShipSpecial.isDestroyed()
                                && checkCollision(bullet, enemyShipSpecial)) {
                            int pts = enemyShipSpecial.getPointValue();
                            this.context.addPointsFor(bullet, pts);
                            this.context.setCoin(this.context.getCoin() + (pts / 10));
                            this.context.setShipsDestroyed(this.context.getShipsDestroyed() + 1);
                            this.context.getEnemyShipSpecialFormation().destroy(enemyShipSpecial);
                            recyclable.add(bullet);
                        }
                }

                if (this.context.getOmegaBoss() != null
                        && !this.context.getOmegaBoss().isDestroyed()
                        && checkCollision(bullet, this.context.getOmegaBoss())) {
                    this.context.getOmegaBoss().takeDamage(2);
                    if(this.context.getOmegaBoss().getHealPoint() <= 0) {
                        this.context.setShipsDestroyed(this.context.getShipsDestroyed() + 1);
                        int pts = this.context.getOmegaBoss().getPointValue();
                        this.context.addPointsFor(bullet, pts);
                        this.context.setCoin(this.context.getCoin() + (pts / 10));
                        this.context.getOmegaBoss().destroy();
                        AchievementManager.getInstance().unlockAchievement("Boss Slayer");
                    }
                    recyclable.add(bullet);
                }

                /** when final boss collide with bullet */
                if(this.context.getFinalBoss() != null && !this.context.getFinalBoss().isDestroyed() && checkCollision(bullet,this.context.getFinalBoss())){
                    this.context.getFinalBoss().takeDamage(1);
                    if(this.context.getFinalBoss().getHealPoint() <= 0){
                        int pts = this.context.getFinalBoss().getPointValue();
                        this.context.addPointsFor(bullet, pts);
                        this.context.setCoin(this.context.getCoin() + (pts / 10));
                        this.context.getFinalBoss().destroy();
                        AchievementManager.getInstance().unlockAchievement("Boss Slayer");
                    }
                    recyclable.add(bullet);
                }
            }
        this.context.getBullets().removeAll(recyclable);
        BulletPool.recycle(recyclable);
    }

    /**
     * Manages collisions between player ship and enemy ships.
     * Player loses a life immediately upon collision with any enemy.
     */
    private void manageShipEnemyCollisions() {
        // ===== P1 collision check =====
        if (this.context.isLevelFinished() && this.context.getLivesP1() > 0 && !this.context.getShip().isDestroyed()
                && !this.context.getShip().isInvincible()) {

            // Check collision with normal enemy ships
            if (this.context.getEnemyShipFormation() != null) {
                for (EnemyShip enemyShip : this.context.getEnemyShipFormation()) {
                    if (!enemyShip.isDestroyed() && checkCollision(this.context.getShip(), enemyShip)) {
                        this.context.getEnemyShipFormation().destroy(enemyShip);
                        this.context.getShip().destroy();
                        this.context.setLivesP1(this.context.getLivesP1() - 1);
                        this.context.showHealthPopup("-1 Life (Collision!)");
                        this.context.getLogger().info("Ship collided with enemy! " + this.context.getLivesP1()
                                + " lives remaining.");
                        return;
                    }
                }
            }

            // Check collision with infinite enemies
            if (this.context.getInfiniteEnemyFormation() != null) {
                for (InfiniteEnemyShip infiniteEnemy : this.context.getInfiniteEnemyFormation()) {
                    if (!infiniteEnemy.isDestroyed() && checkCollision(this.context.getShip(), infiniteEnemy)) {
                        this.context.getInfiniteEnemyFormation().destroy(infiniteEnemy);
                        this.context.getShip().destroy();
                        this.context.setLivesP1(this.context.getLivesP1() - 1);
                        this.context.showHealthPopup("-1 Life (Collision!)");
                        this.context.getLogger().info("Ship collided with infinite enemy! " + this.context.getLivesP1()
                                + " lives remaining.");
                        return;
                    }
                }
            }

            // Check collision with special enemy formation (red/blue ships)
            if (this.context.getEnemyShipSpecialFormation() != null) {
                for (EnemyShip enemyShipSpecial : this.context.getEnemyShipSpecialFormation()) {
                    if (enemyShipSpecial != null && !enemyShipSpecial.isDestroyed()
                            && checkCollision(this.context.getShip(), enemyShipSpecial)) {
                        enemyShipSpecial.destroy();
                        this.context.getShip().destroy();
                        this.context.setLivesP1(this.context.getLivesP1() - 1);
                        this.context.showHealthPopup("-1 Life (Collision!)");
                        this.context.getLogger().info("Ship collided with special enemy formation! "
                                + this.context.getLivesP1() + " lives remaining.");
                        return;
                    }
                }
            }

            // Check collision with omega boss (mid boss - yellow/pink ship)
            if (this.context.getOmegaBoss() != null && !this.context.getOmegaBoss().isDestroyed()
                    && checkCollision(this.context.getShip(), this.context.getOmegaBoss())) {
                this.context.getShip().destroy();
                this.context.setLivesP1(this.context.getLivesP1() - 1);
                this.context.showHealthPopup("-1 Life (Boss Collision!)");
                this.context.getLogger().info("Ship collided with omega boss! " + this.context.getLivesP1()
                        + " lives remaining.");
                return;
            }

            // Check collision with final boss
            if (this.context.getFinalBoss() != null && !this.context.getFinalBoss().isDestroyed()
                    && checkCollision(this.context.getShip(), this.context.getFinalBoss())) {
                this.context.getShip().destroy();
                this.context.setLivesP1(this.context.getLivesP1() - 1);
                this.context.showHealthPopup("-1 Life (Boss Collision!)");
                this.context.getLogger().info("Ship collided with final boss! " + this.context.getLivesP1()
                        + " lives remaining.");
                return;
            }
        }

        // ===== P2 collision check =====
        if (this.context.isTwoPlayerMode() && this.context.isLevelFinished() && this.context.getShipP2() != null && this.context.getLivesP2() > 0
                && !this.context.getShipP2().isDestroyed() && !this.context.getShipP2().isInvincible()) {
            // Check collision with normal enemy ships
            if (this.context.getEnemyShipFormation() != null) {
                for (EnemyShip enemyShip : this.context.getEnemyShipFormation()) {
                    if (!enemyShip.isDestroyed() && checkCollision(this.context.getShipP2(), enemyShip)) {
                        this.context.getEnemyShipFormation().destroy(enemyShip);
                        this.context.getShipP2().destroy();
                        this.context.setLivesP2(this.context.getLivesP2() - 1);
                        this.context.showHealthPopup("-1 Life (Collision!)");
                        this.context.getLogger().info("Ship P2 collided with enemy! " + this.context.getLivesP2()
                                + " lives remaining.");
                        return;
                    }
                }
            }

            // Check collision with special enemy formation
            if (this.context.getEnemyShipSpecialFormation() != null) {
                for (EnemyShip enemyShipSpecial : this.context.getEnemyShipSpecialFormation()) {
                    if (enemyShipSpecial != null && !enemyShipSpecial.isDestroyed()
                            && checkCollision(this.context.getShipP2(), enemyShipSpecial)) {
                        enemyShipSpecial.destroy();
                        this.context.getShipP2().destroy();
                        this.context.setLivesP2(this.context.getLivesP2() - 1);
                        this.context.showHealthPopup("-1 Life (Collision!)");
                        this.context.getLogger().info("Ship P2 collided with special enemy formation! "
                                + this.context.getLivesP2() + " lives remaining.");
                        return;
                    }
                }
            }

            // Check collision with omega boss
            if (this.context.getOmegaBoss() != null && !this.context.getOmegaBoss().isDestroyed()
                    && checkCollision(this.context.getShipP2(), this.context.getOmegaBoss())) {
                this.context.getShipP2().destroy();
                this.context.setLivesP2(this.context.getLivesP2() - 1);
                this.context.showHealthPopup("-1 Life (Boss Collision!)");
                this.context.getLogger().info("Ship P2 collided with omega boss! " + this.context.getLivesP2()
                        + " lives remaining.");
                return;
            }

            // Check collision with final boss
            if (this.context.getFinalBoss() != null && !this.context.getFinalBoss().isDestroyed()
                    && checkCollision(this.context.getShipP2(), this.context.getFinalBoss())) {
                this.context.getShipP2().destroy();
                this.context.setLivesP2(this.context.getLivesP2() - 1);
                this.context.showHealthPopup("-1 Life (Boss Collision!)");
                this.context.getLogger().info("Ship P2 collided with final boss! " + this.context.getLivesP2()
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

        boolean hasP2 = this.context.isTwoPlayerMode();
        Ship shipP2 = hasP2 ? this.context.getShipP2() : null;

        if (this.context.isLevelFinished() && ((this.context.getLivesP1() > 0 && !this.context.getShip().isDestroyed())
                || (hasP2 && shipP2 != null && this.context.getLivesP2() > 0 && !shipP2.isDestroyed()))) {
            for (DropItem dropItem : this.context.getDropItems()) {

                if (this.context.getLivesP1() > 0 && !this.context.getShip().isDestroyed() && checkCollision(this.context.getShip(), dropItem)) {
                    this.context.getLogger().info("Player acquired dropItem: " + dropItem.getItemType());

                    ItemHUDManager.getInstance().addDroppedItem(dropItem.getItemType());

                    switch (dropItem.getItemType()) {
                        case Heal:
                            this.context.gainLife();
                            break;
                        case Shield:
                            this.context.getShip().activateInvincibility(5000); // 5 seconds of invincibility
                            break;
                        case Stop:
                            DropItem.applyTimeFreezeItem(3000);
                            break;
                        case Push:
                            if (this.context.getEnemyShipFormation() != null) {
                                DropItem.PushbackItem(this.context.getEnemyShipFormation(), 20);
                            }
                            break;
                        case Explode:
                            int pts = 0;
                            // normal mode
                            if (this.context.getEnemyShipFormation() != null) {
                                for (EnemyShip enemyShip : this.context.getEnemyShipFormation()) {
                                    if (enemyShip != null && !enemyShip.isDestroyed()) {
                                        enemyShip.takeDamage(1);
                                        boolean afterHit = enemyShip.getHealth() == 0;
                                        if (afterHit) {
                                            pts += enemyShip.getPointValue();
                                            this.context.setShipsDestroyed(this.context.getShipsDestroyed() + 1);
                                            this.context.getEnemyShipFormation().destroy(enemyShip);
                                        }
                                    }
                                }
                            }
                            // inf mode
                            if (this.context.getInfiniteEnemyFormation() != null) {
                                for (InfiniteEnemyShip infiniteEnemy : this.context.getInfiniteEnemyFormation()) {
                                    if (infiniteEnemy != null && !infiniteEnemy.isDestroyed()) {
                                        infiniteEnemy.takeDamage(1);
                                        if (infiniteEnemy.isDestroyed()) {
                                            pts += infiniteEnemy.getPointValue();
                                            this.context.setShipsDestroyed(this.context.getShipsDestroyed() + 1);
                                        }
                                    }
                                }
                            }
                            this.context.addPointsFor(null, pts);
                            this.context.setCoin(this.context.getCoin() + (pts / 10));
                            break;
                        case Slow:
                            if (this.context.getEnemyShipFormation() != null) {
                                this.context.getEnemyShipFormation().activateSlowdown();
                                this.context.getLogger().info("Enemy formation slowed down!");
                            }
                            break;
                        default:
                            break;
                    }
                    acquiredDropItems.add(dropItem);
                } else if (hasP2 && shipP2 != null && this.context.getLivesP2() > 0 && !shipP2.isDestroyed()
                        && checkCollision(shipP2, dropItem)) {
                    this.context.getLogger().info("Player acquired dropItem: " + dropItem.getItemType());


                    ItemHUDManager.getInstance().addDroppedItem(dropItem.getItemType());

                    switch (dropItem.getItemType()) {
                        case Heal:
                            this.context.gainLifeP2();
                            break;
                        case Shield:
                            shipP2.activateInvincibility(5000); // 5 seconds of invincibility
                            break;
                        case Stop:
                            DropItem.applyTimeFreezeItem(3000);
                            break;
                        case Push:
                            if (this.context.getEnemyShipFormation() != null) {
                                DropItem.PushbackItem(this.context.getEnemyShipFormation(), 20);
                            }
                            break;
                        case Explode:
                            int pts2 = 0;

                            if (this.context.getEnemyShipFormation() != null) {
                                for (EnemyShip enemyShip : this.context.getEnemyShipFormation()) {
                                    if (enemyShip != null && !enemyShip.isDestroyed()) {
                                        enemyShip.takeDamage(1);
                                        boolean afterHit = enemyShip.getHealth() == 0;
                                        if (afterHit) {
                                            pts2 += enemyShip.getPointValue();
                                            this.context.setShipsDestroyed(this.context.getShipsDestroyed() + 1);
                                            this.context.getEnemyShipFormation().destroy(enemyShip);
                                        }
                                    }
                                }
                            }
                            this.context.addPointsFor(null, pts2);
                            this.context.setCoin(this.context.getCoin() + (pts2 / 10));
                            break;
                        case Slow:
                            if (this.context.getEnemyShipFormation() != null) {
                                this.context.getEnemyShipFormation().activateSlowdown();
                                this.context.getLogger().info("Enemy formation slowed down!");
                            }
                            break;
                        default:
                            break;
                    }
                    acquiredDropItems.add(dropItem);
                }
            }
            this.context.getDropItems().removeAll(acquiredDropItems);
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