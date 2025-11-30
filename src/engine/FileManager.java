package engine;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import engine.DrawManager.SpriteType;
import engine.level.JsonLoader;

public final class FileManager {

    private static FileManager instance;
    private static Logger logger;
    private static final int MAX_SCORES = 7;
    private static final String USERS_DIR = "res";
    private static final String USERS_FILE_PATH = USERS_DIR + java.io.File.separator + "users.json";
    private static final String HIGHSCORES_FILE_PATH = USERS_DIR + java.io.File.separator + "highscores.json";
    private static List<Score> highScores;

    /**
     * Initializes the FileManager singleton, prepares the user-data directory, and loads persisted high scores.
     *
     * Initializes the logger, ensures the USERS_DIR directory exists (creating it if necessary and logging success or failure),
     * and populates the in-memory high scores by calling loadHighScores().
     */
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

    /**
     * Get the singleton FileManager instance, creating it if necessary.
     *
     * @return the singleton FileManager instance
     */
    protected static FileManager getInstance() {
        if (instance == null)
            instance = new FileManager();
        return instance;
    }
	
    /**
	 * Retrieve the in-memory list of high scores maintained by the FileManager.
	 *
	 * @return the current list of high scores; the returned list is the internal cache and modifications to it will affect FileManager's state
	 */
	public List<Score> getHighScores() {
		return highScores;
	}

    /**
	 * Populate each boolean[][] in spriteMap with pixel values read from the embedded "graphics" resource.
	 *
	 * Each character '1' in the resource sets the corresponding array cell to `true`; each '0' sets it to `false`.
	 *
	 * @param spriteMap map from SpriteType to a preallocated 2D boolean array that will be filled with sprite pixels
	 * @throws IOException if reading the "graphics" resource fails
	 */
	public void loadSprite(final Map<SpriteType, boolean[][]> spriteMap)
			throws IOException {
		InputStream inputStream = null;

		try {
			inputStream = DrawManager.class.getClassLoader()
                    .getResourceAsStream("graphics");
            char c;

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
	 * Loads the bundled TrueType font and returns it at the specified size.
	 *
	 * @param size the font size in points
	 * @return the loaded Font derived to the requested size
	 * @throws FontFormatException if the font resource is not a valid font
	 * @throws IOException if the font resource cannot be read or the stream fails to close
	 */
	public Font loadFont(final float size) throws IOException,
			FontFormatException {
		InputStream inputStream = null;
		Font font;

		try {
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

	/**
	 * Load user accounts from the users.json file and return them keyed by username.
	 *
	 * The method parses JSON stored at USERS_FILE_PATH, constructs User objects with
	 * their stored passwords and achievement mappings, and returns a map from
	 * username to User. If the file is missing or parsing fails, an empty map is
	 * returned (a new file will be created when users are next saved).
	 *
	 * @return a map of usernames to corresponding User objects; empty if no data was loaded
	 * @throws IOException if an I/O error occurs while opening or reading the users file
	 */
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
					user.getAchievements().put(entry.getKey(), entry.getValue());
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

	/**
	 * Serialize the provided users map and write it to the users.json file in the user data directory.
	 *
	 * The method overwrites the existing file and stores each user's username, password, and achievements
	 * in a JSON structure under the top-level `users` array.
	 *
	 * @param users a map of usernames to User objects to persist
	 * @throws IOException if an I/O error occurs while writing the users file
	 */
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
	 * Initializes and populates the in-memory highScores list from the highscores JSON file.
	 *
	 * Reads HIGHSCORES_FILE_PATH, parses the "highScores" array, constructs Score objects,
	 * sorts the resulting list, and stores it in the class-level `highScores` field.
	 * If the file is missing the list remains empty and an informational message is logged.
	 * If parsing fails an error is logged.
	 */
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

	/**
	 * Writes the in-memory high score list to the highscores.json file in the user data directory.
	 *
	 * The output is a JSON object with a top-level "highScores" array; each entry contains
	 * the fields "name", "score", "stage", "killed", "bullets", and "accuracy".
	 *
	 * @throws IOException if writing to the highscores.json file fails
	 */
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

	/**
	 * Escape backslashes and double quotes for safe inclusion in JSON.
	 *
	 * <p>Null inputs are converted to an empty string.
	 *
	 * @param str the input string to escape
	 * @return the input with backslashes (`\`) and double quotes (`"`) escaped, or an empty string if {@code str} is null
	 */
	private String escapeJson(String str) {
		if (str == null) return "";
		return str.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}