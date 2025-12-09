package engine;

import engine.level.Level;
import entity.*;
import java.util.Set;
import java.util.logging.Logger;

public interface    CollisionContext {
    Set<Bullet> getBullets();
    Set<BossBullet> getBossBullets();
    EnemyShipFormation getEnemyShipFormation();
    EnemyShipSpecialFormation getEnemyShipSpecialFormation();
    InfiniteEnemyFormation getInfiniteEnemyFormation();
    Set<DropItem> getDropItems();

    Ship getShip();
    Ship getShipP2();

    void setLivesP1(int v);
    void setLivesP2(int v);
    int getLivesP1();
    int getLivesP2();
    void gainLife();
    void gainLifeP2();

    boolean isTwoPlayerMode();
    boolean isLevelFinished();

    MidBoss getOmegaBoss();
    FinalBoss getFinalBoss();

    void addPointsFor(Bullet b, int p);
    void setCoin(int c);
    int getCoin();
    void setShipsDestroyed(int v);
    int getShipsDestroyed();

    GameState getGameState();
    Logger getLogger();
    Level getCurrentLevel();
    void showHealthPopup(String msg);
}
