package engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import entity.Entity;
import entity.FinalBoss;
import entity.Ship;
import engine.Achievement;
import screen.CreditScreen;
import screen.GameScreen;
import screen.Screen;
import engine.Score;
import engine.AnimatedBackground.Star;
import engine.AnimatedBackground.ShootingStar;

/**
 * Manages screen drawing.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public final class DrawManager {

	/** Singleton instance of the class. */
	private static DrawManager instance;
	/** Current frame. */
	private static Frame frame;
	/** FileManager instance. */
	private static FileManager fileManager;
	/** Application logger. */
	private static final Logger logger = Core.getLogger();
	/** Graphics context. */
	private static Graphics graphics;
	/** Buffer Graphics. */
	private static Graphics backBufferGraphics;
	/** Buffer image. */
	private static BufferedImage backBuffer;
	/** Normal sized font. */
	private static Font fontRegular;
	/** Normal sized font properties. */
	private static FontMetrics fontRegularMetrics;
	/** Big sized font. */
	private static Font fontBig;
	/** Big sized font properties. */
	private static FontMetrics fontBigMetrics;
	/** Small sized font for credits. */
    private static Font fontSmall;
    /** Small sized font properties. */
    private static FontMetrics fontSmallMetrics;

	/** Sprite types mapped to their images. */
	private static Map<SpriteType, boolean[][]> spriteMap;

//	public void drawBossHealthBar(GameScreen gameScreen, String omega, int healPoint, int maxHp) {
//	}

	/** Sprite types. */
	public static enum SpriteType {
		Ship, ShipDestroyed, Bullet, EnemyBullet, EnemyShipA1, EnemyShipA2,
		EnemyShipB1, EnemyShipB2, EnemyShipC1, EnemyShipC2, EnemyShipSpecial,
		FinalBoss1, FinalBoss2,FinalBossBullet,FinalBossDeath,OmegaBoss1, OmegaBoss2,OmegaBossDeath, Explosion, SoundOn, SoundOff, Item_MultiShot,
		Item_Atkspeed, Item_Penetrate, Item_Explode, Item_Slow, Item_Stop,
		Item_Push, Item_Shield, Item_Heal
	}

	/**
	 * Private constructor.
	 */
	private DrawManager() {
		fileManager = Core.getFileManager();
		logger.info("Started loading resources.");

		try {
			spriteMap = new LinkedHashMap<SpriteType, boolean[][]>();

			spriteMap.put(SpriteType.Ship, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipDestroyed, new boolean[13][8]);
			spriteMap.put(SpriteType.Bullet, new boolean[3][5]);
			spriteMap.put(SpriteType.EnemyBullet, new boolean[3][5]);
			spriteMap.put(SpriteType.EnemyShipA1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipA2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipB1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipB2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipC1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipC2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipSpecial, new boolean[16][7]);
			spriteMap.put(SpriteType.Explosion, new boolean[13][7]);
			spriteMap.put(SpriteType.SoundOn, new boolean[15][15]);
			spriteMap.put(SpriteType.SoundOff, new boolean[15][15]);
			spriteMap.put(SpriteType.Item_Explode, new boolean[5][5]);
			spriteMap.put(SpriteType.Item_Slow, new boolean[5][5]);
			spriteMap.put(SpriteType.Item_Stop, new boolean[5][5]);
			spriteMap.put(SpriteType.Item_Push, new boolean[5][5]);
			spriteMap.put(SpriteType.Item_Shield, new boolean[5][5]);
			spriteMap.put(SpriteType.Item_Heal, new boolean[5][5]);
			spriteMap.put(SpriteType.FinalBoss1, new boolean[50][40]);
			spriteMap.put(SpriteType.FinalBoss2, new boolean[50][40]);
			spriteMap.put(SpriteType.FinalBossBullet,new boolean[3][5]);
			spriteMap.put(SpriteType.FinalBossDeath, new boolean[50][40]);
			spriteMap.put(SpriteType.OmegaBoss1, new boolean[32][14]);
			spriteMap.put(SpriteType.OmegaBoss2, new boolean[32][14]);
			spriteMap.put(SpriteType.OmegaBossDeath, new boolean[16][16]);
			fileManager.loadSprite(spriteMap);
			logger.info("Finished loading the sprites.");

			fontRegular = fileManager.loadFont(14f);
			fontBig = fileManager.loadFont(24f);
			fontSmall = fileManager.loadFont(9f);
			logger.info("Finished loading the fonts.");

		} catch (IOException e) {
			logger.warning("Loading failed.");
		} catch (FontFormatException e) {
			logger.warning("Font formatting failed.");
		}
	}

	/**
	 * Returns shared instance of DrawManager.
	 */
	public static DrawManager getInstance() {
		if (instance == null)
			instance = new DrawManager();
		return instance;
	}

	/**
	 * Sets the frame to draw the image on.
	 */
	public void setFrame(final Frame currentFrame) {
		frame = currentFrame;
	}

	/**
	 * First part of the drawing process.
	 */
	public void initDrawing(final Screen screen) {
		backBuffer = new BufferedImage(screen.getWidth(), screen.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		graphics = frame.getGraphics();
		backBufferGraphics = backBuffer.getGraphics();

		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics.fillRect(0, 0, screen.getWidth(), screen.getHeight());

		fontRegularMetrics = backBufferGraphics.getFontMetrics(fontRegular);
		fontBigMetrics = backBufferGraphics.getFontMetrics(fontBig);
		fontSmallMetrics = backBufferGraphics.getFontMetrics(fontSmall);
	}

	/**
	 * Draws the completed drawing on screen.
	 */
	public void completeDrawing(final Screen screen) {
		graphics.drawImage(backBuffer, frame.getInsets().left,
				frame.getInsets().top, frame);
	}

	/**
	 * Draws an entity.
	 */
	public void drawEntity(final Entity entity, final int positionX, final int positionY) {
		boolean[][] image = spriteMap.get(entity.getSpriteType());
		backBufferGraphics.setColor(entity.getColor());
		for (int i = 0; i < image.length; i++)
			for (int j = 0; j < image[i].length; j++)
				if (image[i][j])
					backBufferGraphics.drawRect(positionX + i * 2, positionY + j * 2, 1, 1);


	}

	/**
	 * Draws current score on screen.
	 */
	public void drawScore(final Screen screen, final int score) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		String scoreString = String.format("P1:%04d", score);
		backBufferGraphics.drawString(scoreString, screen.getWidth() - 120, 25);
	}
    //  === [ADD] Draw P2's score on the line below P1's score ===
    public void drawScoreP2(final Screen screen, final int scoreP2) {
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(Color.WHITE);
        String text = String.format("P2:%04d", scoreP2);
        //  Y coordinate is 15px lower than P1 score to avoid overlapping
        backBufferGraphics.drawString(text, screen.getWidth() - 120, 40);
    }

    /**
     * Draws the elapsed time on screen.
     */
    public void drawTime(final Screen screen, final long milliseconds) {
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(Color.GRAY);
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        String timeString = String.format("Time: %02d:%02d", minutes, seconds);
		int x = 10;
		int y = screen.getHeight() - 20;
		backBufferGraphics.drawString(timeString, x, y);
    }

    /**
     * Draws current coin on screen.
     */
    public void drawCoin(final Screen screen, final int coin) {
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(Color.WHITE);
        String coinString = String.format("%03d$", coin);
        int x = screen.getWidth() / 2 - fontRegularMetrics.stringWidth(coinString) / 2;
        int y = screen.getHeight() - 50;
        backBufferGraphics.drawString(coinString, x, y);
    }

	/**
	 * Draws number of remaining lives on screen.
	 */
	public void drawLives(final Screen screen, final int lives) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		// backBufferGraphics.drawString("P1:" + Integer.toString(lives), 10, 25);
		backBufferGraphics.drawString("P1:", 15, 25);
		Ship dummyShip = new Ship(0, 0,Color.green);
		for (int i = 0; i < lives; i++)
			drawEntity(dummyShip, 40 + 35 * i, 10);
	}

	public void drawLivesP2(final Screen screen, final int lives) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		// backBufferGraphics.drawString("P2:" + Integer.toString(lives), 10, 40);
		backBufferGraphics.drawString("P2:", 15, 40);

		Ship dummyShip = new Ship(0, 0,Color.pink);
		for (int i = 0; i < lives; i++) {
			drawEntity(dummyShip, 40 + 35 * i, 30);
		}
	}


	/**
	 * Draws the items HUD.
	 */
	public void drawItemsHUD(final Screen screen) {
		ItemHUDManager itemHUD = ItemHUDManager.getInstance();
		itemHUD.initialize(screen);
		itemHUD.drawItems(screen, backBufferGraphics);
	}

    /**
     * Draws the current level on the bottom-left of the screen.
     */
    public void drawLevel(final Screen screen, final String levelName) {
        final int paddingX = 20;
        final int paddingY = 50;
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(Color.WHITE);
        int yPos = screen.getHeight() - paddingY;
        backBufferGraphics.drawString(levelName, paddingX, yPos);
    }

    /**
     * Draws an achievement pop-up message on the screen.
     */
    public void drawAchievementPopup(final Screen screen, final String text) {
        int popupWidth = 250;
        int popupHeight = 50;
        int x = screen.getWidth() / 2 - popupWidth / 2;
        int y = 80;
        backBufferGraphics.setColor(new Color(0, 0, 0, 200));
        backBufferGraphics.fillRoundRect(x, y, popupWidth, popupHeight, 15, 15);
        backBufferGraphics.setColor(Color.YELLOW);
        backBufferGraphics.drawRoundRect(x, y, popupWidth, popupHeight, 15, 15);
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(Color.WHITE);
        drawCenteredRegularString(screen, text, y + popupHeight / 2 + 5);
    }

    /**
     * Draws a notification popup for changes in health.
     */
    public void drawHealthPopup(final Screen screen, final String text) {
        int popupWidth = 250;
        int popupHeight = 40;
        int x = screen.getWidth() / 2 - popupWidth / 2;
        int y = 100;
        backBufferGraphics.setColor(new Color(0, 0, 0, 200));
        backBufferGraphics.fillRoundRect(x, y, popupWidth, popupHeight, 15, 15);
        Color textColor;
        if (text.startsWith("+")) {
            textColor = new Color(50, 255, 50);
        } else {
            textColor = new Color(255, 50, 50);
        }
        backBufferGraphics.setColor(textColor);
        drawCenteredBigString(screen, text, y + popupHeight / 2 + 5);
    }

	/**
	 * Draws a thick line from side to side of the screen.
	 */
	public void drawHorizontalLine(final Screen screen, final int positionY) {
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawLine(0, positionY, screen.getWidth(), positionY);
		backBufferGraphics.drawLine(0, positionY + 1, screen.getWidth(), positionY + 1);
	}

	/**
	 * Draws game title.
	 */
	public void drawTitle(final Screen screen) {
		String titleString = "Invaders";
		String instructionsString = "select with w+s / arrows, confirm with space";
		backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, instructionsString, screen.getHeight() / 2);
		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, titleString, screen.getHeight() / 3);
	}

	/**
	 * Draws main menu.
	 */
	public void drawMenu(final Screen screen, final int option) {
		String playString = "Play";
		String highScoresString = "High scores";
		String achievementsString = "Achievements";
		String shopString = "Shop";
		String exitString = "Exit";

		// Pulsing color for selected item
		float pulse = (float) ((Math.sin(System.currentTimeMillis() / 200.0) + 1.0) / 2.0);
		Color pulseColor = new Color(0, 0.5f + pulse * 0.5f, 0);

		if (option == 2) backBufferGraphics.setColor(pulseColor);
		else backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, playString, screen.getHeight() / 3 * 2);

		if (option == 3) backBufferGraphics.setColor(pulseColor);
		else backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, highScoresString, screen.getHeight() / 3 * 2 + fontRegularMetrics.getHeight() * 1);

		if (option == 6) backBufferGraphics.setColor(pulseColor);
		else backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, achievementsString, screen.getHeight() / 3 * 2 + fontRegularMetrics.getHeight() * 2);

		if (option == 4) backBufferGraphics.setColor(pulseColor);
		else backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, shopString, screen.getHeight() / 3 * 2 + fontRegularMetrics.getHeight() * 3);

		if (option == 0) backBufferGraphics.setColor(pulseColor);
		else backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, exitString, screen.getHeight() / 3 * 2 + fontRegularMetrics.getHeight() * 4);

	}

	/**
	 * Draws mode select screen.
	 */
	public void drawModeSelect(final Screen screen, final int option) {
		String playString1 = "1P Mode";
		String playString2 = "2P Mode";
        String AIMode = "AI Mode";
		String playStringInfinite = "Infinite Mode";
		String backString = "Back";

		// Pulsing color for selected item
		float pulse = (float) ((Math.sin(System.currentTimeMillis() / 200.0) + 1.0) / 2.0);
		Color pulseColor = new Color(0, 0.5f + pulse * 0.5f, 0);

		if (option == 2) backBufferGraphics.setColor(pulseColor);
		else backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, playString1, screen.getHeight() / 3 * 2);

		if (option == 7) backBufferGraphics.setColor(pulseColor);
		else backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, playString2, screen.getHeight() / 3 * 2 + fontRegularMetrics.getHeight() * 1);

		if(option == 9) backBufferGraphics.setColor(pulseColor);
		else backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, playStringInfinite, screen.getHeight() / 3 * 2 + fontRegularMetrics.getHeight() * 2);

        if (option == 5) backBufferGraphics.setColor(pulseColor);
        else backBufferGraphics.setColor(Color.WHITE);
        drawCenteredRegularString(screen, AIMode, screen.getHeight() / 3 * 2 + fontRegularMetrics.getHeight() * 3);

		if (option == 1) backBufferGraphics.setColor(pulseColor);
		else backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, backString, screen.getHeight() / 3 * 2 + fontRegularMetrics.getHeight() * 4);
	}

	/**
	 * Draws game results.
	 */
	public void drawResults(final Screen screen, final int score, final int livesRemaining, final int shipsDestroyed, final float accuracy, final boolean isNewRecord) {
		String scoreString = String.format("score %04d", score);
		String livesRemainingString = "lives remaining " + livesRemaining;
		String shipsDestroyedString = "enemies destroyed " + shipsDestroyed;
		String accuracyString = String.format("accuracy %.2f%%", accuracy * 100);
		int height = isNewRecord ? 4 : 2;
		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, scoreString, screen.getHeight() / height);
		drawCenteredRegularString(screen, livesRemainingString, screen.getHeight() / height + fontRegularMetrics.getHeight() * 2);
		drawCenteredRegularString(screen, shipsDestroyedString, screen.getHeight() / height + fontRegularMetrics.getHeight() * 4);
		drawCenteredRegularString(screen, accuracyString, screen.getHeight() / height + fontRegularMetrics.getHeight() * 6);
	}

	/**
	 * Draws interactive characters for name input.
	 */
	public void drawNameInput(final Screen screen, final char[] name, final int nameCharSelected) {
		String newRecordString = "New Record!";
		String introduceNameString = "Introduce name:";
		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredRegularString(screen, newRecordString, screen.getHeight() / 4 + fontRegularMetrics.getHeight() * 10);
		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, introduceNameString, screen.getHeight() / 4 + fontRegularMetrics.getHeight() * 12);

		int positionX = screen.getWidth() / 2 - (fontRegularMetrics.getWidths()[name[0]] + fontRegularMetrics.getWidths()[name[1]] + fontRegularMetrics.getWidths()[name[2]] + fontRegularMetrics.getWidths()[' ']) / 2;

		for (int i = 0; i < 3; i++) {
			if (i == nameCharSelected) backBufferGraphics.setColor(Color.GREEN);
			else backBufferGraphics.setColor(Color.WHITE);
			positionX += fontRegularMetrics.getWidths()[name[i]] / 2;
			positionX = i == 0 ? positionX : positionX + (fontRegularMetrics.getWidths()[name[i - 1]] + fontRegularMetrics.getWidths()[' ']) / 2;
			backBufferGraphics.drawString(Character.toString(name[i]), positionX, screen.getHeight() / 4 + fontRegularMetrics.getHeight() * 14);
		}
	}

	/**
	 * Draws basic content of game over screen.
	 */
	public void drawGameOver(final Screen screen, final boolean acceptsInput, final boolean isNewRecord) {
		String gameOverString = "Game Over";
		String continueOrExitString = "Press Space to play again, Escape to exit";
		int height = isNewRecord ? 4 : 2;
		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, gameOverString, screen.getHeight() / height - fontBigMetrics.getHeight() * 2);
		if (acceptsInput) backBufferGraphics.setColor(Color.GREEN);
		else backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, continueOrExitString, screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 10);
	}

	/**
	 * Draws high score screen title and instructions.
	 */
	public void drawHighScoreMenu(final Screen screen) {
		String highScoreString = "High Scores";
		String instructionsString = "Press Space to return";
		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, highScoreString, screen.getHeight() / 8);
		backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, instructionsString, screen.getHeight() / 5);
	}

	/**
	 * Draws high scores.
	 */
    public void drawHighScores(final Screen screen, final List<Score> highScores) {
        backBufferGraphics.setColor(Color.WHITE);
        int i = 0;
        String scoreString = "";
        for (Score score : highScores) {
            scoreString = String.format("%s        %04d", score.getName(), score.getScore());
            drawCenteredRegularString(screen, scoreString, screen.getHeight() / 4 + fontRegularMetrics.getHeight() * (i + 1) * 2);
            i++;
        }
    }

    public void drawAchievements(final Screen screen, final List<Achievement> achievements) {
        backBufferGraphics.setColor(Color.GREEN);
        drawCenteredBigString(screen, "Achievements", screen.getHeight() / 8);
        int i = 0;
        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked()) {
                backBufferGraphics.setColor(Color.GREEN);
            } else {
                backBufferGraphics.setColor(Color.WHITE);
            }
            drawCenteredRegularString(screen, achievement.getName() + " - " + achievement.getDescription(), screen.getHeight() / 5 + fontRegularMetrics.getHeight() * (i + 1) * 2);
            i++;
        }
        backBufferGraphics.setColor(Color.GRAY);
        drawCenteredRegularString(screen, "Press ESC to return", screen.getHeight() - 50);
    }

	/**
	 * Draws the credits screen title and instructions.
	 */
	public void drawCreditsMenu(final Screen screen) {
		String creditsString = "Credits";
		String instructionsString = "Press Space to return";
		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, creditsString, screen.getHeight() / 8);
		backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, instructionsString, screen.getHeight() / 5);
	}

	/**
	 * Draws the list of credits on the screen.
	 */
	public void drawCredits(final Screen screen, final List<CreditScreen.Credit> creditList) {
		backBufferGraphics.setFont(fontSmall);
		int yPosition = screen.getHeight() / 4;
		final int xPosition = screen.getWidth() / 10;
		final int lineSpacing = fontSmallMetrics.getHeight() + 1;
		final int teamSpacing = lineSpacing + 5;
		for (CreditScreen.Credit credit : creditList) {
			backBufferGraphics.setColor(Color.GREEN);
			String teamInfo = String.format("%s - %s", credit.getTeamName(), credit.getRole());
			backBufferGraphics.drawString(teamInfo, xPosition, yPosition);
			yPosition += lineSpacing;
			yPosition += teamSpacing;
		}
	}

	/**
	 * Draws a centered string on regular font.
	 */
	public void drawCenteredRegularString(final Screen screen, final String string, final int height) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.drawString(string, screen.getWidth() / 2 - fontRegularMetrics.stringWidth(string) / 2, height);
	}

	/**
	 * Draws a centered string on big font.
	 */
	public void drawCenteredBigString(final Screen screen, final String string, final int height) {
		backBufferGraphics.setFont(fontBig);
		backBufferGraphics.drawString(string, screen.getWidth() / 2 - fontBigMetrics.stringWidth(string) / 2, height);
	}

	/**
	 * Countdown to game start.
	 */
	public void drawCountDown(final Screen screen, final int level, final int number, final boolean bonusLife) {
		int rectWidth = screen.getWidth();
		int rectHeight = screen.getHeight() / 6;
		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics.fillRect(0, screen.getHeight() / 2 - rectHeight / 2, rectWidth, rectHeight);
		backBufferGraphics.setColor(Color.GREEN);
		if (number >= 4) {
			if (!bonusLife) {
				drawCenteredBigString(screen, "Level " + level, screen.getHeight() / 2 + fontBigMetrics.getHeight() / 3);
			} else {
				drawCenteredBigString(screen, "Level " + level + " - Bonus life!", screen.getHeight() / 2 + fontBigMetrics.getHeight() / 3);
			}
		} else if (number != 0) {
			drawCenteredBigString(screen, Integer.toString(number), screen.getHeight() / 2 + fontBigMetrics.getHeight() / 3);
		} else {
			drawCenteredBigString(screen, "GO!", screen.getHeight() / 2 + fontBigMetrics.getHeight() / 3);
		}
	}

	/**
	 * Draws the complete shop screen with all items and levels.
	 */
	public void drawShopScreen(final Screen screen, final int coinBalance, final int selectedItem, final int selectionMode, final int selectedLevel, final int totalItems, final String[] itemNames, final String[] itemDescriptions, final int[][] itemPrices, final int[] maxLevels, final screen.ShopScreen shopScreen) {
		// Draw title
		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, "SHOP", screen.getHeight() / 8);
		// Draw coin balance
		backBufferGraphics.setColor(Color.YELLOW);
		String balanceString = String.format("Your Balance: %d coins", coinBalance);
		drawCenteredRegularString(screen, balanceString, 120);
		// Draw instructions based on mode
		backBufferGraphics.setColor(Color.GRAY);
		String instructions = "";
		if (selectionMode == 0) {
			instructions = "W/S: Navigate | SPACE: Select | ESC: Exit";
		} else {
			instructions = "A/D: Change Level | SPACE: Buy | ESC: Back";
		}
		drawCenteredRegularString(screen, instructions, 145);

		int headerHeight = 165;
		int footerHeight = 50;
		int availableHeight = screen.getHeight() - headerHeight - footerHeight;

		int currentY = 170;
		int baseSpacing = 58;
		int expandedExtraSpace = 55;

		boolean hasExpandedItem = (selectionMode == 1);

		int totalRequiredHeight = totalItems * baseSpacing;
		if (hasExpandedItem) {
			totalRequiredHeight += expandedExtraSpace;
		}

		int adjustedSpacing = baseSpacing;
		if (totalRequiredHeight > availableHeight) {
			int overflow = totalRequiredHeight - availableHeight;
			adjustedSpacing = baseSpacing - (overflow / totalItems);
			if (adjustedSpacing < 48) {
				adjustedSpacing = 48;
			}
		}

		for (int i = 0; i < totalItems; i++) {
			boolean isSelected = (i == selectedItem) && (selectionMode == 0);
			boolean isLevelSelection = (i == selectedItem && selectionMode == 1);
			int currentLevel = shopScreen.getItemCurrentLevel(i);
			drawShopItem(screen, itemNames[i], itemDescriptions[i], itemPrices[i], maxLevels[i], currentLevel, currentY, isSelected, coinBalance, isLevelSelection, selectedLevel);
			if (isLevelSelection) {
				currentY += adjustedSpacing + expandedExtraSpace;
			} else {
				currentY += adjustedSpacing;
			}
		}

		int exitY = screen.getHeight() - 30;
		if (selectedItem == totalItems && selectionMode == 0) {
			backBufferGraphics.setColor(Color.GREEN);
		} else {
			backBufferGraphics.setColor(Color.WHITE);
		}

		if (shopScreen.betweenLevels) {
			drawCenteredRegularString(screen, "< Back to Game >", exitY);
		} else {
			drawCenteredRegularString(screen, "< Back to Main Menu >", exitY);
		}
	}

	/**
	 * Draws a single shop item with level indicators.
	 */
	public void drawShopItem(final Screen screen, final String itemName, final String description, final int[] prices, final int maxLevel, final int currentLevel, final int yPosition, final boolean isSelected, final int playerCoins, final boolean isLevelSelection, final int selectedLevel) {
		if (isSelected || isLevelSelection) {
			backBufferGraphics.setColor(Color.GREEN);
		} else {
			backBufferGraphics.setColor(Color.WHITE);
		}
		String levelInfo = currentLevel > 0 ? " [Lv." + currentLevel + "/" + maxLevel + "]" : " [Not Owned]";
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.drawString(itemName + levelInfo, 30, yPosition);

		if (isSelected || isLevelSelection) {
			backBufferGraphics.setColor(Color.GRAY);
			backBufferGraphics.drawString(description, 30, yPosition + 15);
		}

		if (isLevelSelection) {
			int levelStartX = 30;
			int levelY = yPosition + 35;
			int maxWidth = screen.getWidth() - 60;
			int currX = levelStartX;
			int currY = levelY;
			int spaceBetween = 18;

			for (int lvl = 1; lvl <= maxLevel; lvl++) {
				int price = prices[lvl - 1];
				boolean canAfford = playerCoins >= price;
				boolean isOwned = currentLevel >= lvl;
				boolean isThisLevel = (lvl == selectedLevel);

				if (isOwned) {
					backBufferGraphics.setColor(Color.DARK_GRAY);
				} else if (isThisLevel) {
					backBufferGraphics.setColor(Color.GREEN);
				} else if (canAfford) {
					backBufferGraphics.setColor(Color.WHITE);
				} else {
					backBufferGraphics.setColor(Color.RED);
				}
				String levelText = "Lv." + lvl + (isOwned ? " [OWNED]" : " (" + price + "$)");
				int textWidth = fontRegularMetrics.stringWidth(levelText);

				if (currX + textWidth > levelStartX + maxWidth) {
					currX = levelStartX;
					currY += fontRegularMetrics.getHeight() + 3;
				}
				backBufferGraphics.drawString(levelText, currX, currY);
				currX += textWidth + spaceBetween;
			}
		}
	}
	
		/**
		 * Draws the login screen.
		 *
		 * @param screen         The screen to draw on.
		 * @param username       The current username text.
		 * @param password       The current password text.
		 * @param selectedField  The currently selected UI element.
		 */
		public void drawLoginScreen(final Screen screen, final String username, final String password, final int selectedField, final String errorMessage) {
			// Title
			drawCenteredBigString(screen, "LOGIN", screen.getHeight() / 4);
	
			int yPos = screen.getHeight() / 2 - 50;
			int lineHeight = 35; // A fixed value for line height and padding
	
			// Username field
			if (selectedField == 0) {
				backBufferGraphics.setColor(Color.GREEN);
			} else {
				backBufferGraphics.setColor(Color.WHITE);
			}
			drawCenteredRegularString(screen, "Username:", yPos);
			drawCenteredRegularString(screen, username.isEmpty() ? "_______" : username, yPos + lineHeight);
	
			// Password field
			yPos += lineHeight * 2;
			if (selectedField == 1) {
				backBufferGraphics.setColor(Color.GREEN);
			} else {
				backBufferGraphics.setColor(Color.WHITE);
			}
			drawCenteredRegularString(screen, "Password:", yPos);
			String maskedPassword = new String(new char[password.length()]).replace('\0', '*');
			drawCenteredRegularString(screen, maskedPassword.isEmpty() ? "_______" : maskedPassword, yPos + lineHeight);
	
			// Login button
			yPos += lineHeight * 2;
			if (selectedField == 2) {
				backBufferGraphics.setColor(Color.GREEN);
			} else {
				backBufferGraphics.setColor(Color.WHITE);
			}
			drawCenteredRegularString(screen, "LOGIN", yPos);

			// Draw error message if present
			if (errorMessage != null) {
				backBufferGraphics.setColor(Color.RED);
				drawCenteredRegularString(screen, errorMessage, screen.getHeight() - 100);
			}
		}
	
		/**
		 * Draws purchase feedback message.
		 */
		public void drawShopFeedback(final Screen screen, final String message) {
			int popupWidth = 300;
			int popupHeight = 50;
		int x = screen.getWidth() / 2 - popupWidth / 2;
		int y = 70;

		backBufferGraphics.setColor(new Color(0, 0, 0, 200));
		backBufferGraphics.fillRoundRect(x, y, popupWidth, popupHeight, 15, 15);

		if (message.contains("Purchased")) {
			backBufferGraphics.setColor(Color.GREEN);
		} else if (message.contains("Not enough") || message.contains("failed")) {
			backBufferGraphics.setColor(Color.RED);
		} else {
			backBufferGraphics.setColor(Color.YELLOW);
		}
		backBufferGraphics.drawRoundRect(x, y, popupWidth, popupHeight, 15, 15);

		backBufferGraphics.setFont(fontRegular);
		drawCenteredRegularString(screen, message, y + popupHeight / 2 + 5);
	}

	/**
	 * Draws the starfield background.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param stars
	 *            List of stars to draw.
	 * @param angle
	 *            Current rotation angle.
	 */
	public void drawStars(final Screen screen, final List<Star> stars, final float angle) {
		final int centerX = screen.getWidth() / 2;
		final int centerY = screen.getHeight() / 2;
		final double angleRad = Math.toRadians(angle);
		final double cosAngle = Math.cos(angleRad);
		final double sinAngle = Math.sin(angleRad);

		for (Star star : stars) {
			float relX = star.baseX - centerX;
			float relY = star.baseY - centerY;

			double rotatedX = relX * cosAngle - relY * sinAngle;
			double rotatedY = relX * sinAngle + relY * cosAngle;

			int screenX = (int) (rotatedX + centerX);
			int screenY = (int) (rotatedY + centerY);

			// Use star's brightness to set its color for twinkling effect
			float b = star.brightness;
			if (b < 0) b = 0;
			if (b > 1) b = 1;
			backBufferGraphics.setColor(new Color(b, b, b));
			backBufferGraphics.drawRect(screenX, screenY, 1, 1);
		}
	}

	public void drawShootingStars(final Screen screen, final List<ShootingStar> shootingStars, final float angle) {    }

	public void drawBossHealthBar(final int positionX,final int positionY, final String bossName,
								  final int currentHealth, final int maxHealth) {
		// Health bar dimensions
		int barWidth = 105;
		int barHeight = 5;
		final int barX = positionX;
		final int barY = positionY - 6;

		if (Objects.equals(bossName, "FINAL")){
			barWidth = 105;
		}
		else if(Objects.equals(bossName, "OMEGA")){barWidth = 65;}

		// Calculate health percentage
		float healthPercent = (float) currentHealth / maxHealth;
		if (healthPercent < 0) healthPercent = 0;
		if (healthPercent > 1) healthPercent = 1;

		int currentBarWidth = (int) (barWidth * healthPercent);

		// Draw background (empty health bar) - dark gray
		backBufferGraphics.setColor(new Color(40, 40, 40));
		backBufferGraphics.fillRect(barX, barY, barWidth, barHeight);

		// Draw current health bar - green
		if (currentBarWidth > 0) {
			Color healthColor = getHealthBarColor(healthPercent);
			backBufferGraphics.setColor(healthColor);
			backBufferGraphics.fillRect(barX, barY, currentBarWidth, barHeight);
		}

		// Draw border - white
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawRect(barX - 1, barY - 1, barWidth + 1, barHeight + 1);

		// Draw health text (current / max)
		backBufferGraphics.setFont(fontSmall);
		backBufferGraphics.setColor(Color.WHITE);
		String healthText = currentHealth + " / " + maxHealth;
		int textWidth = fontRegularMetrics.stringWidth(healthText);
		backBufferGraphics.drawString(healthText,
				barX + (barWidth - textWidth / 2) / 2,
				barY + barHeight - 5);
	}

    public Graphics getBackBufferGraphics() {
        return backBufferGraphics;
    }

    private Color getHealthBarColor(float healthPercent) {
		if (healthPercent > 0.90f) {
			// 100% - 90%: Green
			return new Color(0, 255, 0);
		} else if (healthPercent > 0.5f) {
			// 90% - 50%: Green to Yellow
			float ratio = (0.90f - healthPercent) / 0.4f;
			return interpolateColor(
					new Color(0, 255, 0),      // Green
					new Color(255, 255, 0),    // Yellow
					ratio
			);
		} else if (healthPercent > 0.25f) {
			// 50% - 25%: Yellow to Orange
			float ratio = (0.5f - healthPercent) / 0.25f;
			return interpolateColor(
					new Color(255, 255, 0),    // Yellow
					new Color(255, 165, 0),    // Orange
					ratio
			);
		} else {
			// 25% - 0%: Orange to Red
			float ratio = (0.25f - healthPercent) / 0.25f;
			return interpolateColor(
					new Color(255, 165, 0),    // Orange
					new Color(255, 0, 0),      // Red
					ratio
			);
		}
	}

	private Color interpolateColor(Color color1, Color color2, float ratio) {
		if (ratio < 0) ratio = 0;
		if (ratio > 1) ratio = 1;

		int r = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * ratio);
		int g = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * ratio);
		int b = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * ratio);

		return new Color(r, g, b);
	}
}