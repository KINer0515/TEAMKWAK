package entity;

import engine.Core;
import screen.Screen;

import java.awt.*;
import java.util.logging.Logger;

/**
 * Implements a game middle boss entity.
 *
 * @author <a href="developer.ksb@gmail.com">Seungbeom Kim</a>
 *
 */
public abstract class MidBoss extends Entity implements BossEntity {

	protected int healPoint=10;
	protected int maxHp=healPoint;
	protected int pointValue=500;
	protected boolean isDestroyed=false;
	protected int pattern=1;
	protected Screen screen;
	protected Logger logger;

	/**
	 * Constructor, establishes the boss entity's generic properties.
	 *
	 * @param positionX Initial position of the entity in the X axis.
	 * @param positionY Initial position of the entity in the Y axis.
	 * @param width     Width of the entity.
	 * @param height    Height of the entity.
	 * @param healPoint    HP of the entity.
	 * @param pointValue    point of the entity.
	 * @param color     Color of the entity.
	 */
	public MidBoss(int positionX, int positionY, int width, int height, int healPoint, int pointValue, Color color) {
		super(positionX, positionY, width, height, color);
		this.healPoint=healPoint;
		this.maxHp=healPoint;
		this.pointValue=pointValue;
		this.logger = Core.getLogger();
	}

	@Override
	public int getHealPoint() { return this.healPoint; }

	@Override
	public int getPointValue() { return this.pointValue; }

	@Override
	public boolean isDestroyed() { return this.isDestroyed; }

	public int getMaxHp() { return this.maxHp; }

	/**
	 * Associates the formation to a given screen.
	 *
	 * @param newScreen
	 *            Screen to attach.
	 */
	public final void attach(final Screen newScreen) {
		screen = newScreen;
	}

    public void setHealth(int health) {
        this.healPoint = health;
        this.maxHp = health;
    }
}
