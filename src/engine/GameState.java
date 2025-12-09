package engine;

/**
 * Implements an object that stores the state of the game between levels.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class GameState {

	/** Current game level. */
	private int level;
	/** Current score. */
	private int score;
	private int scoreP1;
	private int scoreP2;
	/** Lives currently remaining. */
	private int livesRemaining;
	private int livesRemainingP2;
	/** Bullets shot until now. */
	private int bulletsShot;
	/** Ships destroyed until now. */
	private int shipsDestroyed;
    /** Current coin. */
    private int coin;
	/** check 1p or 2p mode. */
	private boolean isTwoPlayerMode;
    /** check AI mode or not. */
    private boolean isAIMode;


	/**
	 * Constructor.
	 * 
	 * @param level
	 *            Current game level.
	 * @param score
	 *            Current score.
     * @param coin
     *            Current coin.
	 * @param livesRemaining
	 *            Lives currently remaining.
	 * @param livesRemainingP2
	 *            Lives currently remainingP2.
	 * @param bulletsShot
	 *            Bullets shot until now.
	 * @param shipsDestroyed
	 *            Ships destroyed until now.
	 * @param isTwoPlayerMode
	 * 			  check 1p or 2p mode.
     * @param isAIMode
     *            check AI mode or not.
	 */
	public GameState(final int level, final int score, final int scoreP1, final int scoreP2,
			final int livesRemaining,final int livesRemainingP2, final int bulletsShot,
			final int shipsDestroyed, final int coin, final boolean isTwoPlayerMode, final boolean isAIMode) {
		this.level = level;
		this.score = score;
		this.scoreP1 = scoreP1;
		this.scoreP2 = scoreP2;
		this.livesRemaining = livesRemaining;
		this.livesRemainingP2 = livesRemainingP2;
		this.bulletsShot = bulletsShot;
        this.shipsDestroyed = shipsDestroyed;
        this.coin = coin;
		this.isTwoPlayerMode = isTwoPlayerMode;
		this.isAIMode = isAIMode;
	}
	public GameState(final int level, final int score,
					 final int livesRemaining, final int livesRemainingP2, final int bulletsShot,
					 final int shipsDestroyed, final int coin, final boolean isTwoPlayerMode, final boolean isAIMode) {
		this(level, score, score, 0, livesRemaining, livesRemainingP2,	bulletsShot, shipsDestroyed, coin, isTwoPlayerMode,isAIMode);
    }

    public final boolean isAIMode() { return isAIMode; }

	public final boolean isTwoPlayerMode() { return isTwoPlayerMode; }

	/**
	 * @return the level
	 */
	public final int getLevel() {
		return level;
	}

	/**
	 * @return the score
	 */
	public final int getScore() {
		return score;
	}

	/**
	 * @return score
	 */
	public final int getScoreP1() {
		return scoreP1;
	}

	public final int getScoreP2() {
		return scoreP2;
	}

	/**
	 * @return the livesRemaining
	 */
	public final int getLivesRemaining() {
		return livesRemaining;
	}

	public final int getLivesRemainingP2() {
		return livesRemainingP2;
	}

	/**
	 * @return the bulletsShot
	 */
	public final int getBulletsShot() {
		return bulletsShot;
	}

	/**
	 * @return the shipsDestroyed
	 */
	public final int getShipsDestroyed() {
		return shipsDestroyed;
	}

    public final int getCoin() { return coin; }

	public final boolean deductCoins(final int amount) {
		if (amount < 0) {
			return false;
		}
		if (this.coin >= amount) {
			this.coin -= amount;
			return true;
		}
		return false;
	}

	public final void addCoins(final int amount) {
		if (amount > 0) {
			this.coin += amount;
		}
	}

	public final void setCoins(final int amount) {
		if (amount >= 0) {
			this.coin = amount;
		}
	}

	public final int calculateSkinCoin(){
		return this.coin / 10;
	}
}
