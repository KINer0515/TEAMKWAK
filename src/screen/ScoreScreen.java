package screen;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import engine.*;

/**
 * Implements the score screen.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class ScoreScreen extends Screen {

	/** Milliseconds between changes in user selection. */
	private static final int SELECTION_TIME = 200;
	/** Maximum number of high scores. */
	private static final int MAX_HIGH_SCORE_NUM = 7;

	/** Current score. */
	private int score;
	/** Player lives left. */
	private int livesRemaining;
	/** Total bullets shot by the player. */
	private int bulletsShot;
	/** Total ships destroyed by the player. */
	private int shipsDestroyed;
	/** List of past high scores. */
	private List<Score> highScores;
	/** Checks if current score is a new high score. */
	private boolean isNewRecord;
	/** Current logged-in user. */
	private User currentUser;

	/**
	 * Create a score screen that captures end-of-game statistics and determines high-score status.
	 *
	 * Initializes this screen with the final score, remaining lives, bullets fired, and ships destroyed
	 * taken from the provided GameState; loads the global high-score list and sets `isNewRecord`
	 * when the current score qualifies; and records the currently logged-in user.
	 *
	 * @param width the screen width in pixels
	 * @param height the screen height in pixels
	 * @param fps the target frames per second for the screen
	 * @param gameState the final game state containing score, lives remaining, bullets shot, and ships destroyed
	 */
	public ScoreScreen(final int width, final int height, final int fps,
			final GameState gameState) {
		super(width, height, fps);

		this.score = gameState.getScore();
		this.livesRemaining = gameState.getLivesRemaining();
		this.bulletsShot = gameState.getBulletsShot();
		this.shipsDestroyed = gameState.getShipsDestroyed();
		this.isNewRecord = false;
		this.currentUser = Core.getCurrentUser(); // Current user is still needed for achievement logic if any, and for name.

		// Check against global high scores
		this.highScores = new ArrayList<>(Core.getFileManager().getHighScores());
		if (this.highScores.size() < MAX_HIGH_SCORE_NUM
				|| this.highScores.get(this.highScores.size() - 1).getScore() < this.score) {
			this.isNewRecord = true;
		}
	}

	/**
	 * Starts the action.
	 * 
	 * @return Next screen code.
	 */
	public final int run() {
		super.run();

		return this.returnCode;
	}

	/**
	 * Render the score screen and handle navigation input.
	 *
	 * Draws the current screen and, once the input delay has finished, processes keyboard input:
	 * pressing Escape sets the return code to 1 and stops the screen (return to main menu);
	 * pressing Space sets the return code to 2 and stops the screen (play again).
	 */
	protected final void update() {
		super.update();

		draw();
		if (this.inputDelay.checkFinished()) {
			if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
				// Return to main menu.
				this.returnCode = 1;
				this.isRunning = false;
			} else if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
				// Play again.
				this.returnCode = 2;
				this.isRunning = false;
			}
		}
	}



	/**
	 * Render the game-over screen and end-of-game statistics.
	 *
	 * Draws the game-over banner and the results (score, lives remaining, ships destroyed,
	 * accuracy) and indicates whether the current score is a new high score. The name-entry
	 * UI is intentionally not drawn because the current user is known.
	 */
	private void draw() {
		drawManager.initDrawing(this);

		drawManager.drawGameOver(this, this.inputDelay.checkFinished(),
				this.isNewRecord);
		drawManager.drawResults(this, this.score, this.livesRemaining,
				this.shipsDestroyed, (float) this.shipsDestroyed
						/ this.bulletsShot, this.isNewRecord);

		// Name input is no longer drawn as the user is known.

		drawManager.completeDrawing(this);
	}
}
