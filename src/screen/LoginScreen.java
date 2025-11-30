package screen;

import java.awt.Color;
import java.awt.event.KeyEvent;
import engine.Cooldown;
import engine.Core;
import engine.User;
import engine.UserManager;
import engine.AchievementManager;

public class LoginScreen extends Screen {

    /** Cooldown time for navigating between fields. */
    private static final int NAVIGATION_COOLDOWN = 200;
    /** Cooldown time for typing characters and backspace. */
    private static final int TYPING_COOLDOWN = 125;
    private static final int ERROR_DISPLAY_COOLDOWN = 2000; // 2 seconds

    private String username = "";
    private String password = "";
    private int selectedField = 0; // 0: username, 1: password, 2: login button
    private String errorMessage = null;

    private Cooldown navigationCooldown;
    private Cooldown typingCooldown;
    private Cooldown errorCooldown;

    /**
     * Create a LoginScreen and initialize its input and error cooldowns.
     *
     * Initializes the screen with the given width, height, and frame rate, and creates
     * and resets the navigation, typing, and error-display cooldowns used by the
     * login UI.
     *
     * @param width  the screen width in pixels
     * @param height the screen height in pixels
     * @param fps    the target frames per second for the screen
     */
    public LoginScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        this.navigationCooldown = Core.getCooldown(NAVIGATION_COOLDOWN);
        this.typingCooldown = Core.getCooldown(TYPING_COOLDOWN);
        this.errorCooldown = Core.getCooldown(ERROR_DISPLAY_COOLDOWN);
        this.navigationCooldown.reset();
        this.typingCooldown.reset();
        this.errorCooldown.reset();
    }

    /**
     * Prepare the screen for display and reset the input delay timer.
     *
     * Calls superclass initialization and clears any pending input delay so input
     * handling starts from a fresh state.
     */
    @Override
    public void initialize() {
        super.initialize();
        this.inputDelay.reset();
    }

    /**
     * Execute the screen's run logic and return its result code.
     *
     * <p>Delegates execution to the superclass run implementation and returns the
     * integer code representing the screen outcome.</p>
     *
     * @return the screen's return code (e.g., 1 for a successful login; other values indicate different outcomes)
     */
    public final int run() {
        super.run();

        return this.returnCode;
    }

    /**
     * Advance the screen's state for the current frame: process input if ready, clear expired error messages, and render.
     *
     * Calls the superclass update behavior, invokes input handling when the input delay has finished, clears the
     * displayed error message when its cooldown completes, and triggers drawing of the screen.
     */
    @Override
    protected void update() {
        super.update();

        if (this.inputDelay.checkFinished()) {
            handleInput();
        }

        // Check if error message cooldown has finished and clear the message
        if (this.errorCooldown.checkFinished() && this.errorMessage != null) {
            this.errorMessage = null;
        }

        draw();
    }

    /**
     * Process keyboard input to navigate between fields, edit credential text, and initiate login.
     *
     * <p>Updates the focused field, modifies the username or password when editing, and attempts
     * authentication when the login action is triggered. Successful authentication sets the current
     * user, synchronizes achievements, sets the screen return code to 1, and stops the screen;
     * failed authentication sets an error message and resets the error display cooldown.</p>
     *
     * <p>Input handling respects the screen's navigation, typing, and error-display cooldowns.</p>
     */
    private void handleInput() {
        // Navigation Logic
        if (this.navigationCooldown.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_UP)) {
                selectedField = Math.max(0, selectedField - 1);
                this.navigationCooldown.reset();
            } else if (inputManager.isKeyDown(KeyEvent.VK_DOWN)) {
                selectedField = Math.min(2, selectedField + 1);
                this.navigationCooldown.reset();
            }
        }

        // Text Input and Backspace Logic
        if (selectedField == 0 || selectedField == 1) { // Username or Password field
            if (this.typingCooldown.checkFinished()) {
                if (inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE)) {
                    String target = (selectedField == 0) ? username : password;
                    if (!target.isEmpty()) {
                        target = target.substring(0, target.length() - 1);
                        if (selectedField == 0) {
                            username = target;
                        } else {
                            password = target;
                        }
                        this.typingCooldown.reset();
                    }
                } else {
                    for (int i = KeyEvent.VK_A; i <= KeyEvent.VK_Z; i++) {
                        if (inputManager.isKeyDown(i)) {
                            appendCharacter((char) i);
                            this.typingCooldown.reset();
                            return; // Process one key at a time
                        }
                    }
                    for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; i++) {
                        if (inputManager.isKeyDown(i)) {
                            appendCharacter((char) i);
                            this.typingCooldown.reset();
                            return; // Process one key at a time
                        }
                    }
                }
            }
        }

        // Login Button Action
        if (selectedField == 2 && (inputManager.isKeyDown(KeyEvent.VK_ENTER) || inputManager.isKeyDown(KeyEvent.VK_SPACE))) {
            logger.info("Login button pressed!");
            User loggedInUser = UserManager.getInstance().login(username, password);

            if (loggedInUser != null) {
                logger.info("Login successful for user: " + username);
                Core.setCurrentUser(loggedInUser); // Set the current user in Core
                AchievementManager.getInstance().syncAchievementsWithUser(loggedInUser); // Sync achievements
                this.returnCode = 1; // Success code for login
                this.isRunning = false; // Exit login screen
            } else {
                logger.warning("Login failed for user: " + username);
                this.errorMessage = "Invalid password.";
                this.errorCooldown.reset();
            }
        }
    }

    /**
     * Appends a character to the currently selected input field (username or password).
     *
     * If the SHIFT key is not held, the character is converted to lowercase before appending.
     *
     * @param c the character to append to the active field
     */
    private void appendCharacter(char c) {
        if (!inputManager.isKeyDown(KeyEvent.VK_SHIFT)) {
            c = Character.toLowerCase(c);
        }
        if (selectedField == 0) {
            username += c;
        } else {
            password += c;
        }
    }

    /**
     * Render the login screen using the current username, password, selected field, and error message.
     *
     * Performs the draw lifecycle by beginning drawing, instructing the draw manager to render the login UI with
     * the screen's current state, and finalizing the drawing.
     */
    private void draw() {
        drawManager.initDrawing(this);
        drawManager.drawLoginScreen(this, username, password, selectedField, errorMessage);
        drawManager.completeDrawing(this);
    }

    /**
     * Gets the current username input.
     *
     * @return the current username input, possibly empty
     */

    public String getUsername() {
        return this.username;
    }

    /**
     * Retrieves the current password input.
     *
     * @return the current password string
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Gets the currently selected input field.
     *
     * @return 0 if the username field is selected, 1 if the password field is selected, 2 if the login button is selected.
     */
    public int getSelectedField() {
        return this.selectedField;
    }

    /**
     * Retrieve the screen's return code indicating the outcome of its execution.
     *
     * @return the integer return code that represents the screen's result
     */
    public int getReturnCode() {
        return this.returnCode;
    }

    /**
     * Indicates whether the screen's main loop is currently active.
     *
     * @return `true` if the screen is running, `false` otherwise.
     */
    public boolean isRunning() {
        return this.isRunning;
    }

    /**
     * Set the InputManager instance used by this screen to receive input events.
     *
     * @param inputManager the InputManager to use for processing user input
     */
    public void setInputManager(engine.InputManager inputManager) {
        this.inputManager = inputManager;
    }

    /**
     * Sets the DrawManager used to render the login screen; primarily intended for injection in tests.
     *
     * @param drawManager the draw manager to use for rendering
     */
    public void setDrawManager(engine.DrawManager drawManager) {
        this.drawManager = drawManager;
    }

    /**
     * Set which input element is currently focused in the login screen.
     *
     * @param selectedField the focus index: 0 = username field, 1 = password field, 2 = login button
     */
    public void setSelectedField(int selectedField) {
        this.selectedField = selectedField;
    }
}
