package screen;

import engine.*;

import java.awt.event.KeyEvent;

public class LoginScreen extends Screen {

    /** Cooldown time for navigating between fields. */
    private static final int NAVIGATION_COOLDOWN = 200;
    /** Cooldown time for typing characters and backspace. */
    private static final int TYPING_COOLDOWN = 125;
    private static final int ERROR_DISPLAY_COOLDOWN = 2000; // 2 seconds
    private static final int MIN_INPUT_LENGTH = 1;
    private static final int MAX_INPUT_LENGTH = 12;

    private String username = "";
    private String password = "";
    private int selectedField = 0; // 0: username, 1: password, 2: login button
    private String errorMessage = null;

    private Cooldown navigationCooldown;
    private Cooldown typingCooldown;
    private Cooldown errorCooldown;

    public LoginScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        this.navigationCooldown = Core.getCooldown(NAVIGATION_COOLDOWN);
        this.typingCooldown = Core.getCooldown(TYPING_COOLDOWN);
        this.errorCooldown = Core.getCooldown(ERROR_DISPLAY_COOLDOWN);
        this.navigationCooldown.reset();
        this.typingCooldown.reset();
        this.errorCooldown.reset();
    }

    @Override
    public void initialize() {
        super.initialize();
        this.inputDelay.reset();
    }

    public final int run() {
        super.run();

        return this.returnCode;
    }

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
            if (username.length() < MIN_INPUT_LENGTH) {
                this.errorMessage = "Username must be at least " + MIN_INPUT_LENGTH + " characters.";
                this.errorCooldown.reset();
                return;
            }
            if (password.length() < MIN_INPUT_LENGTH) {
                this.errorMessage = "Password must be at least " + MIN_INPUT_LENGTH + " characters.";
                this.errorCooldown.reset();
                return;
            }

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

    private void appendCharacter(char c) {
        String currentText = (selectedField == 0) ? username : password;
        if (currentText.length() >= MAX_INPUT_LENGTH) {
            return; // Do not append if max length is reached
        }

        if (!inputManager.isKeyDown(KeyEvent.VK_SHIFT)) {
            c = Character.toLowerCase(c);
        }
        if (selectedField == 0) {
            username += c;
        } else {
            password += c;
        }
    }

    private void draw() {
        drawManager.initDrawing(this);
        drawManager.drawLoginScreen(this, username, password, selectedField, errorMessage);
        drawManager.completeDrawing(this);
    }

    // --- Methods for Testability ---

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public int getSelectedField() {
        return this.selectedField;
    }

    public int getReturnCode() {
        return this.returnCode;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setInputManager(engine.InputManager inputManager) {
        this.inputManager = inputManager;
    }

    public void setDrawManager(engine.DrawManager drawManager) {
        this.drawManager = drawManager;
    }

    public void setSelectedField(int selectedField) {
        this.selectedField = selectedField;
    }

    // --- Package-private setters for testing dependency injection ---

    void setNavigationCooldown(Cooldown cooldown) {
        this.navigationCooldown = cooldown;
    }

    void setTypingCooldown(Cooldown cooldown) {
        this.typingCooldown = cooldown;
    }

    void setErrorCooldown(Cooldown cooldown) {
        this.errorCooldown = cooldown;
    }

    void setInputDelay(Cooldown cooldown) {
        this.inputDelay = cooldown;
    }
}