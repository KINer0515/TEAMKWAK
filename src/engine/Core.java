package engine;

import audio.SoundManager;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import entity.ShopItem;
import engine.level.LevelManager;
import screen.*;

/**
 * Implements core game logic.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public final class Core {

	/** Width of current screen. */
	private static final int WIDTH = 448;
	/** Height of current screen. */
	private static final int HEIGHT = 520;
	/** Max fps of current screen. */
	private static final int FPS = 60;

	/** Max lives. */
	private static final int MAX_LIVES = 3;
	/** Levels between extra life. */
	private static final int EXTRA_LIFE_FRECUENCY = 3;

	/** Frame to draw the screen on. */
	private static Frame frame;
	/** Screen currently shown. */
	private static Screen currentScreen;
	/** Level manager for loading level settings. */
	private static LevelManager levelManager;
	/** Application logger. */
	private static final Logger LOGGER = Logger.getLogger(Core.class
			.getSimpleName());
	/** Logger handler for printing to disk. */
	private static Handler fileHandler;
	/** Logger handler for printing to console. */
	private static ConsoleHandler consoleHandler;
	/** Currently logged-in user. */
	private static User currentUser;


	/**
	 * Application entry point that initializes logging, subsystems, and runs the main game loop.
	 *
	 * <p>Sets up log handlers, creates the main Frame and DrawManager, initializes levels,
	 * and drives the central screen/state machine that handles title, game, shop, high-score,
	 * achievement, credits, and login flows. Persists global high scores at the end of runs
	 * and exits the JVM when the loop terminates.</p>
	 *
	 * @param args program arguments (ignored)
	 */
	public static void main(final String[] args) {
		try {

			LOGGER.setUseParentHandlers(false);

			fileHandler = new FileHandler("log");
			fileHandler.setFormatter(new MinimalFormatter());

			consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(new MinimalFormatter());

			LOGGER.addHandler(fileHandler);
			LOGGER.addHandler(consoleHandler);
			LOGGER.setLevel(Level.ALL);

		} catch (Exception e) {
			// TODO handle exception
			e.printStackTrace();
		}

		frame = new Frame(WIDTH, HEIGHT);
		DrawManager.getInstance().setFrame(frame);
		int width = frame.getWidth();
		int height = frame.getHeight();

		levelManager = new LevelManager();
		GameState gameState;
		boolean isTwoPlayerMode = false;


        int returnCode = 9;
		do {
			ShopItem.resetAllItems();
			switch (returnCode) {
                case 1:
                    // Main menu.
                    currentScreen = new TitleScreen(width, height, FPS);
					SoundManager.stopAll();
					SoundManager.playLoop("sfx/menu_music.wav");
                    LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
                            + " title screen at " + FPS + " fps.");
                    returnCode = frame.setScreen(currentScreen);
                    LOGGER.info("Closing title screen.");
					if (returnCode == 2) {
						currentScreen = new ModeSelectScreen(width, height, FPS);
						LOGGER.info("Starting" + WIDTH + "x" + HEIGHT + " mode select screen at " + FPS + " fps.");
						returnCode = frame.setScreen(currentScreen);
						LOGGER.info("Closing mode select screen.");
					}
                    break;
                case 2:
					isTwoPlayerMode = false;
					gameState = new GameState(1, 0, MAX_LIVES, 0, 0, 0,0, isTwoPlayerMode);
                    do {
                        // One extra life every few levels
                        boolean bonusLife = gameState.getLevel()
                                % EXTRA_LIFE_FRECUENCY == 0
                                && gameState.getLivesRemaining() < MAX_LIVES;

						// Music for each level
						SoundManager.stopAll();
						SoundManager.playLoop("sfx/level" + gameState.getLevel() + ".wav");

                        engine.level.Level currentLevel = levelManager.getLevel(gameState.getLevel());

                        // TODO: Handle case where level is not found after JSON loading is implemented.
                        if (currentLevel == null) {
                          // For now, we can just break or default to level 1 if we run out of levels.
                          // This will be important when the number of levels is defined by maps.json
                          break;
                        }

						SoundManager.stopAll();
						SoundManager.playLoop("sfx/level" + gameState.getLevel() + ".wav");

                        // Start a new level
                        currentScreen = new GameScreen(
                                gameState,
                                currentLevel,
                                bonusLife,
                                MAX_LIVES,
                                width,
                                height,
                                FPS
                        );

                        LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
                                + " game screen at " + FPS + " fps.");
                        frame.setScreen(currentScreen);
                        LOGGER.info("Closing game screen.");
                        gameState = ((GameScreen) currentScreen).getGameState();
                        if (gameState.getLivesRemaining() > 0 || gameState.getLivesRemainingP2() > 0) {
							SoundManager.stopAll();
							SoundManager.play("sfx/levelup.wav");

							LOGGER.info("Opening shop screen with "
                                    + gameState.getCoin() + " coins.");

                            //Launch the ShopScreen (between levels)
                            currentScreen = new ShopScreen(gameState, width, height, FPS, true);

                            frame.setScreen(currentScreen);
                            LOGGER.info("Closing shop screen.");

                            gameState = new GameState(
                                    gameState.getLevel() + 1,          // Increment level
                                    gameState.getScore(),              // Keep current score
                                    gameState.getLivesRemaining(),     // Keep remaining lives
									gameState.getLivesRemainingP2(),   // Keep remaining livesP2
                                    gameState.getBulletsShot(),        // Keep bullets fired
                                    gameState.getShipsDestroyed(),     // Keep ships destroyed
                                    gameState.getCoin(),                // Keep current coins
									isTwoPlayerMode
                            );
                        }
                        // Loop while player still has lives and levels remaining
                    } while (gameState.getLivesRemaining() > 0);

					// Save score to global high scores
					{
						String playerName = (currentUser != null) ? currentUser.getUsername() : "GUEST";

						// Calculate accuracy
						float accuracy = 0;
						if (gameState.getBulletsShot() > 0) {
							accuracy = (float) gameState.getShipsDestroyed() / gameState.getBulletsShot() * 100;
						}

						Score newScore = new Score(
								playerName,
								gameState.getScore(),
								gameState.getLevel(),
								gameState.getShipsDestroyed(),
								gameState.getBulletsShot(),
								accuracy
						);

						java.util.List<Score> highScores = FileManager.getInstance().getHighScores();
						highScores.add(newScore);
						java.util.Collections.sort(highScores);

						// Trim the list if it exceeds the max number of scores
						if (highScores.size() > 7) { // Using hardcoded MAX_SCORES
							highScores.subList(7, highScores.size()).clear();
						}

						// Save the updated global high scores
						try {
							FileManager.getInstance().saveHighScores();
							LOGGER.info("Saved global high score " + gameState.getScore() + " for player " + playerName);
						} catch (java.io.IOException e) {
							LOGGER.warning("Could not save global high scores: " + e.getMessage());
						}
					}

					SoundManager.stopAll();
					SoundManager.play("sfx/gameover.wav");
                    LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
                            + " score screen at " + FPS + " fps, with a score of "
                            + gameState.getScore() + ", "
                            + gameState.getLivesRemaining() + " lives remaining, "
                            + gameState.getBulletsShot() + " bullets shot and "
                            + gameState.getShipsDestroyed() + " ships destroyed.");

                    currentScreen = new ScoreScreen(width, height, FPS, gameState);
                    returnCode = frame.setScreen(currentScreen);
                    LOGGER.info("Closing score screen.");
                    break;
                case 3:
                    // High scores
                    currentScreen = new HighScoreScreen(width, height, FPS);
                    LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
                            + " high score screen at " + FPS + " fps.");
                    returnCode = frame.setScreen(currentScreen);
                    LOGGER.info("Closing high score screen.");
                    break;
                case 4:
                    // Shop opened manually from main menu
					gameState = new GameState(1, 0, MAX_LIVES,MAX_LIVES, 0, 0, 0, isTwoPlayerMode);
                    currentScreen = new ShopScreen(gameState, width, height, FPS, false);
                    LOGGER.info("Starting shop screen (menu) with " + gameState.getCoin() + " coins.");
                    returnCode = frame.setScreen(currentScreen);
                    LOGGER.info("Closing shop screen (menu).");
                    break;
                case 6:
                    // Achievements
                    currentScreen = new AchievementScreen(width, height, FPS);
                    LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
                            + " achievement screen at " + FPS + " fps.");
                    returnCode = frame.setScreen(currentScreen);
                    LOGGER.info("Closing achievement screen.");
                    break;
				case 7:
					isTwoPlayerMode = true;
					gameState = new GameState(1, 0, MAX_LIVES, MAX_LIVES, 0, 0,0, isTwoPlayerMode);
					do {
						// One extra life every few levels
						boolean bonusLife = gameState.getLevel()
								% EXTRA_LIFE_FRECUENCY == 0
								&& gameState.getLivesRemaining() < MAX_LIVES;

						// Music for each level
						SoundManager.stopAll();
						SoundManager.playLoop("sfx/level" + gameState.getLevel() + ".wav");

						engine.level.Level currentLevel = levelManager.getLevel(gameState.getLevel());

						// TODO: Handle case where level is not found after JSON loading is implemented.
						if (currentLevel == null) {
							// For now, we can just break or default to level 1 if we run out of levels.
							// This will be important when the number of levels is defined by maps.json
							break;
						}

						SoundManager.stopAll();
						SoundManager.playLoop("sfx/level" + gameState.getLevel() + ".wav");

						// Start a new level
						currentScreen = new GameScreen(
								gameState,
								currentLevel,
								bonusLife,
								MAX_LIVES,
								width,
								height,
								FPS
						);

						LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
								+ " game screen at " + FPS + " fps.");
						frame.setScreen(currentScreen);
						LOGGER.info("Closing game screen.");
						gameState = ((GameScreen) currentScreen).getGameState();
						if (gameState.getLivesRemaining() > 0 || gameState.getLivesRemainingP2() > 0) {
							SoundManager.stopAll();
							SoundManager.play("sfx/levelup.wav");

							LOGGER.info("Opening shop screen with "
									+ gameState.getCoin() + " coins.");

							//Launch the ShopScreen (between levels)
							currentScreen = new ShopScreen(gameState, width, height, FPS, true);

							frame.setScreen(currentScreen);
							LOGGER.info("Closing shop screen.");

							gameState = new GameState(
									gameState.getLevel() + 1,          // Increment level
									gameState.getScore(),              // Keep current score
									gameState.getLivesRemaining(),     // Keep remaining lives
									gameState.getLivesRemainingP2(),   // Keep remaining livesP2
									gameState.getBulletsShot(),        // Keep bullets fired
									gameState.getShipsDestroyed(),     // Keep ships destroyed
									gameState.getCoin(),                // Keep current coins
									isTwoPlayerMode
							);
						}
						// Loop while player still has lives and levels remaining
					} while (gameState.getLivesRemaining() > 0 || gameState.getLivesRemainingP2() > 0);

					SoundManager.stopAll();
					SoundManager.play("sfx/gameover.wav");
					LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
							+ " score screen at " + FPS + " fps, with a score of "
							+ gameState.getScore() + ", "
							+ gameState.getLivesRemaining() + " lives remaining, "
							+ gameState.getBulletsShot() + " bullets shot and "
							+ gameState.getShipsDestroyed() + " ships destroyed.");

					currentScreen = new ScoreScreen(width, height, FPS, gameState);
					returnCode = frame.setScreen(currentScreen);
					LOGGER.info("Closing score screen.");
					break;
				case 8: // CreditScreen(Easter egg)
					currentScreen = new CreditScreen(width, height, FPS);
					LOGGER.info("Starting " + currentScreen.getClass().getSimpleName() + " screen.");
					returnCode = frame.setScreen(currentScreen);
					break;
				case 9:
					// Login screen
					currentScreen = new LoginScreen(width, height, FPS);
					LOGGER.info("Starting " + WIDTH + "x" + HEIGHT + " login screen at " + FPS + " fps.");
					returnCode = frame.setScreen(currentScreen);
					LOGGER.info("Closing login screen.");
					break;
                default:
                    break;
            }

        } while (returnCode != 0);

        fileHandler.flush();
        fileHandler.close();
        System.exit(0);
	}

	/**
	 * Constructor, not called.
	 */
	private Core() {

	}

	/**
	 * Controls access to the logger.
	 * 
	 * @return Application logger.
	 */
	public static Logger getLogger() {
		return LOGGER;
	}

	/**
	 * Controls access to the drawing manager.
	 * 
	 * @return Application draw manager.
	 */
	public static DrawManager getDrawManager() {
		return DrawManager.getInstance();
	}

	/**
	 * Controls access to the input manager.
	 * 
	 * @return Application input manager.
	 */
	public static InputManager getInputManager() {
		return InputManager.getInstance();
	}

	/**
	 * Controls access to the file manager.
	 * 
	 * @return Application file manager.
	 */
	public static FileManager getFileManager() {
		return FileManager.getInstance();
	}

	/**
	 * Controls creation of new cooldowns.
	 * 
	 * @param milliseconds
	 *            Duration of the cooldown.
	 * @return A new cooldown.
	 */
	public static Cooldown getCooldown(final int milliseconds) {
		return new Cooldown(milliseconds);
	}

	/**
	 * Create a Cooldown configured with a base duration and allowed variance.
	 *
	 * @param milliseconds base duration of the cooldown in milliseconds
	 * @param variance maximum variation of the cooldown duration in milliseconds
	 * @return the Cooldown configured with the specified base duration and variance
	 */
	public static Cooldown getVariableCooldown(final int milliseconds,
			final int variance) {
		return new Cooldown(milliseconds, variance);
	}

	/**
	 * Retrieves the currently logged-in user.
	 *
	 * @return the currently logged-in User, or `null` if no user is set
	 */
	public static User getCurrentUser() {
		return currentUser;
	}

	/**
	 * Sets the application-wide current user.
	 *
	 * If {@code user} is {@code null}, the current user is cleared (no user is logged in).
	 *
	 * @param user the User to set as the current logged-in user, or {@code null} to clear it
	 */
	public static void setCurrentUser(final User user) {
		currentUser = user;
	}
}