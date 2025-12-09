package engine;

import engine.DrawManager.SpriteType;
import engine.level.JsonLoader;

import java.awt.*;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manages files used in the application.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public final class FileManager {

    private static FileManager instance;
    private static Logger logger;
    private static final String USERS_DIR = "res";
    private static final String USERS_FILE_PATH = USERS_DIR + java.io.File.separator + "users.json";
    private static final String HIGHSCORES_FILE_PATH = USERS_DIR + java.io.File.separator + "highscores.json";
    private static List<Score> highScores;

    private FileManager() {
        logger = Core.getLogger();
        File dir = new File(USERS_DIR);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                logger.info("Created user data directory at: " + USERS_DIR);
            } else {
                logger.severe("Failed to create user data directory at: " + USERS_DIR);
            }
        }
        loadHighScores();
    }

    protected static FileManager getInstance() {
        if (instance == null)
            instance = new FileManager();
        return instance;
    }

    public List<Score> getHighScores() {
		return highScores;
	}

	/**
	 * Loads sprites from disk.
	 *
	 * @param spriteMap
	 *            Mapping of sprite type and empty boolean matrix that will
	 *            contain the image.
	 * @throws IOException
	 *             In case of loading problems.
	 */
	public void loadSprite(final Map<SpriteType, boolean[][]> spriteMap)
			throws IOException {
		InputStream inputStream = null;

		try {
			inputStream = DrawManager.class.getClassLoader()
                    .getResourceAsStream("graphics");
            char c;

			// Sprite loading.
			for (Map.Entry<SpriteType, boolean[][]> sprite : spriteMap
					.entrySet()) {
				for (int i = 0; i < sprite.getValue().length; i++)
					for (int j = 0; j < sprite.getValue()[i].length; j++) {
						do
							c = (char) inputStream.read();
						while (c != '0' && c != '1');

						if (c == '1')
							sprite.getValue()[i][j] = true;
						else
							sprite.getValue()[i][j] = false;
					}
				logger.fine("Sprite " + sprite.getKey() + " loaded.");
			}
			if (inputStream != null)
				inputStream.close();
		} finally {
			if (inputStream != null)
				inputStream.close();
		}
	}

	/**
	 * Loads a font of a given size.
	 * 
	 * @param size
	 *            Point size of the font.
	 * @return New font.
	 * @throws IOException
	 *             In case of loading problems.
	 * @throws FontFormatException
	 *             In case of incorrect font format.
	 */
	public Font loadFont(final float size) throws IOException,
			FontFormatException {
		InputStream inputStream = null;
		Font font;

		try {
			// Font loading.
			inputStream = FileManager.class.getClassLoader()
					.getResourceAsStream("font.ttf");
			font = Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(
					size);
		} finally {
			if (inputStream != null)
				inputStream.close();
		}

		return font;
	}

	@SuppressWarnings("unchecked")
	public Map<String, User> loadUsers() throws IOException {
		Map<String, User> users = new HashMap<>();

		try (InputStream inputStream = new FileInputStream(USERS_FILE_PATH);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {

			StringBuilder jsonContent = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				jsonContent.append(line);
			}

			Map<String, Object> root = JsonLoader.parseGeneric(jsonContent.toString());
			List<Map<String, Object>> userMaps = (List<Map<String, Object>>) root.get("users");

			for (Map<String, Object> userMap : userMaps) {
				String username = (String) userMap.get("username");
				String password = (String) userMap.get("password");
				User user = new User(username, password);

				Map<String, Boolean> achievementsMap = (Map<String, Boolean>) userMap.get("achievements");
				for (Map.Entry<String, Boolean> entry : achievementsMap.entrySet()) {
					user.setAchievementStatus(entry.getKey(), entry.getValue());
				}

				users.put(username, user);
			}
			logger.info("User data loaded from JSON.");

		} catch (FileNotFoundException e) {
			logger.warning("users.json not found. A new one will be created upon saving.");
		} catch (Exception e) {
			logger.severe("Failed to parse users.json: " + e.getMessage());
			return new HashMap<>();
		}

		return users;
	}

	public void saveUsers(final Map<String, User> users)
			throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(USERS_FILE_PATH, false), "UTF-8"))) {
			logger.info("Saving user data to JSON.");
			StringBuilder jsonBuilder = new StringBuilder();
			jsonBuilder.append("{\n  \"users\": [\n");

			boolean firstUser = true;
			for (User user : users.values()) {
				if (!firstUser) {
					jsonBuilder.append(",\n");
				}
				jsonBuilder.append("    {\n");
				jsonBuilder.append("      \"username\": \"").append(escapeJson(user.getUsername())).append("\",\n");
				jsonBuilder.append("      \"password\": \"").append(escapeJson(user.getPassword())).append("\",\n");
				jsonBuilder.append("      \"achievements\": {\n");
				boolean firstAchievement = true;
				for (Map.Entry<String, Boolean> entry : user.getAchievements().entrySet()) {
					if (!firstAchievement) {
						jsonBuilder.append(",\n");
					}
					jsonBuilder.append("        \"").append(escapeJson(entry.getKey())).append("\": ").append(entry.getValue());
					firstAchievement = false;
				}
				jsonBuilder.append("\n      }\n");
				jsonBuilder.append("    }");
				firstUser = false;
			}

			jsonBuilder.append("\n  ]\n}");
			writer.write(jsonBuilder.toString());
		}
	}

    /**
     * Loads infinite mode high scores from file.
     *
     * @return Sorted list of scores - players.
     * @throws IOException In case of loading problems.
     */
    public List<Score> loadInfiniteHighScores() throws IOException {
        List<Score> highScores = new ArrayList<Score>();
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        try {
            String jarPath = FileManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            jarPath = URLDecoder.decode(jarPath, "UTF-8");

            String scoresPath = new File(jarPath).getParent();
            scoresPath += File.separator;
            scoresPath += "infinite_scores"; // 파일명을 다르게 설정

            File scoresFile = new File(scoresPath);
            inputStream = new FileInputStream(scoresFile);
            bufferedReader = new BufferedReader(new InputStreamReader(
                    inputStream, Charset.forName("UTF-8")));

            logger.info("Loading infinite mode high scores.");

            Score highScore = null;
            String name = bufferedReader.readLine();
            String score = bufferedReader.readLine();

            while ((name != null) && (score != null)) {
                highScore = new Score(name, Integer.parseInt(score));
                highScores.add(highScore);
                name = bufferedReader.readLine();
                score = bufferedReader.readLine();
            }

        } catch (FileNotFoundException e) {
            // create new file if it doesn't exist
            logger.info("Loading default infinite high scores (empty).");
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
        }

        Collections.sort(highScores);
        return highScores;
    }

    /**
     * Saves infinite mode high scores to disk.
     *
     * @param highScores High scores to save.
     * @throws IOException In case of loading problems.
     */
    public void saveInfiniteHighScores(final List<Score> highScores) throws IOException {
        OutputStream outputStream = null;
        BufferedWriter bufferedWriter = null;

        try {
            String jarPath = FileManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            jarPath = URLDecoder.decode(jarPath, "UTF-8");

            String scoresPath = new File(jarPath).getParent();
            scoresPath += File.separator;
            scoresPath += "infinite_scores"; // 파일명을 다르게 설정

            File scoresFile = new File(scoresPath);

            if (!scoresFile.exists())
                scoresFile.createNewFile();

            outputStream = new FileOutputStream(scoresFile);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                    outputStream, Charset.forName("UTF-8")));

            logger.info("Saving infinite mode high scores.");

            // Saves 7 or less scores.
            int savedCount = 0;
            for (Score score : highScores) {
                if (savedCount >= 7) // MAX_SCORES 상수 대신 숫자 사용 또는 상수 접근 필요
                    break;
                bufferedWriter.write(score.getName());
                bufferedWriter.newLine();
                bufferedWriter.write(Integer.toString(score.getScore()));
                bufferedWriter.newLine();
                savedCount++;
            }

        } finally {
            if (bufferedWriter != null)
                bufferedWriter.close();
        }
    }

	@SuppressWarnings("unchecked")
	private void loadHighScores() {
		highScores = new ArrayList<>();

		try (InputStream inputStream = new FileInputStream(HIGHSCORES_FILE_PATH);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {

			StringBuilder jsonContent = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				jsonContent.append(line);
			}

			Map<String, Object> root = JsonLoader.parseGeneric(jsonContent.toString());
			List<Map<String, Object>> scoresList = (List<Map<String, Object>>) root.get("highScores");

			for (Map<String, Object> scoreMap : scoresList) {
				String name = (String) scoreMap.get("name");
				int scoreValue = ((Number) scoreMap.get("score")).intValue();
				int stage = scoreMap.containsKey("stage") ? ((Number) scoreMap.get("stage")).intValue() : 0;
				int killed = scoreMap.containsKey("killed") ? ((Number) scoreMap.get("killed")).intValue() : 0;
				int bullets = scoreMap.containsKey("bullets") ? ((Number) scoreMap.get("bullets")).intValue() : 0;
				float accuracy = scoreMap.containsKey("accuracy") ? ((Number) scoreMap.get("accuracy")).floatValue() : 0;
				highScores.add(new Score(name, scoreValue, stage, killed, bullets, accuracy));
			}
			Collections.sort(highScores);
			logger.info("High scores loaded from JSON.");

		} catch (FileNotFoundException e) {
			logger.info("highscores.json not found, a new one will be created.");
		} catch (Exception e) {
			logger.severe("Failed to parse highscores.json: " + e.getMessage());
		}
	}

	public void saveHighScores() throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(HIGHSCORES_FILE_PATH, false), "UTF-8"))) {
			logger.info("Saving high scores to JSON.");
			StringBuilder jsonBuilder = new StringBuilder();
			jsonBuilder.append("{\n  \"highScores\": [\n");

			boolean firstScore = true;
			for (Score score : highScores) {
				if (!firstScore) {
					jsonBuilder.append(",\n");
				}
				jsonBuilder.append("    {\n");
				jsonBuilder.append("      \"name\": \"").append(escapeJson(score.getName())).append("\",\n");
				jsonBuilder.append("      \"score\": ").append(score.getScore()).append(",\n");
				jsonBuilder.append("      \"stage\": ").append(score.getStage()).append(",\n");
				jsonBuilder.append("      \"killed\": ").append(score.getKilled()).append(",\n");
				jsonBuilder.append("      \"bullets\": ").append(score.getBullets()).append(",\n");
				jsonBuilder.append("      \"accuracy\": ").append(score.getAccuracy()).append("\n");
				jsonBuilder.append("    }");
				firstScore = false;
			}
			jsonBuilder.append("\n  ]\n}");
			writer.write(jsonBuilder.toString());
		}
	}


	private String escapeJson(String str) {
		if (str == null) return "";
		return str.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
