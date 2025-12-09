package screen;

import java.awt.event.KeyEvent;

import engine.AnimatedBackground;
import engine.Cooldown;
import engine.Core;
import engine.DrawManager;
import audio.SoundManager;

public class ModeSelectScreen extends Screen {

    /** Milliseconds between changes in user selection. */
    private static final int SELECTION_TIME = 200;

    /** Time between changes in user selection. */
    private Cooldown selectionCooldown;

    /** Animated background. */
    private AnimatedBackground animatedBackground;

    private Cooldown feedbackCooldown;

    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param width
     *            Screen width.
     * @param height
     *            Screen height.
     * @param fps
     *            Frames per second, frame rate at which the game is run.
     */
    public ModeSelectScreen(final int width, final int height, final int fps) {
        super(width, height, fps);

        // Defaults to 1P mode.
        // returnCode
        // 1: Back
        // 2: 1P Mode
        // 7: 2P Mode
        // 5: AI Mode
        this.returnCode = 2;
        this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();

        this.feedbackCooldown = Core.getCooldown(2000);
        this.animatedBackground = new AnimatedBackground(width, height);
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
     * Updates visual state, renders the screen, and handles menu navigation input.
     *
     * Updates the animated background and redraws the screen, then—if the selection
     * cooldown and input delay have finished—processes keyboard input:
     * UP or W selects the previous menu item, DOWN or S selects the next menu item,
     * and SPACE stops the screen run loop.
     */
    protected final void update() {
        super.update();
        this.animatedBackground.update();

        draw();
        if (this.selectionCooldown.checkFinished()
                && this.inputDelay.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_UP)
                    || inputManager.isKeyDown(KeyEvent.VK_W)) {
                previousMenuItem();
                this.selectionCooldown.reset();
            }
            if (inputManager.isKeyDown(KeyEvent.VK_DOWN)
                    || inputManager.isKeyDown(KeyEvent.VK_S)) {
                nextMenuItem();
                this.selectionCooldown.reset();
            }
            if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
                    this.isRunning = false;
            }
        }
    }

    /**
     * Shifts the focus to the next menu item.
     */
    private void nextMenuItem() {
        SoundManager.play("sfx/menu_select.wav");
        if (this.returnCode == 2)
            this.returnCode = 7;
        else if (this.returnCode == 7)
            this.returnCode = 9;
        else if (this.returnCode == 9)
            this.returnCode = 5;
        else if (this.returnCode == 5)
            this.returnCode = 1; // Return to title screen
        else if (this.returnCode == 1)
            this.returnCode = 2;
        this.animatedBackground.rotateRight();
    }

    /**
     * Shifts the focus to the previous menu item.
     */
    private void previousMenuItem() {
        SoundManager.play("sfx/menu_select.wav");
        if (this.returnCode == 2)
            this.returnCode = 1; // Return to title screen
        else if (this.returnCode == 1)
            this.returnCode = 5;
        else if (this.returnCode == 5)
            this.returnCode = 9;
        else if (this.returnCode == 9)
            this.returnCode = 7;
        else if (this.returnCode == 7)
            this.returnCode = 2;
        this.animatedBackground.rotateLeft();
    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);

        this.animatedBackground.draw(drawManager, this);
        drawManager.drawModeSelect(this, this.returnCode);

        if (!feedbackCooldown.checkFinished()) {
            drawManager.drawShopFeedback(this, "AI Mode Coming Soon");
        }

        drawManager.completeDrawing(this);
    }
}