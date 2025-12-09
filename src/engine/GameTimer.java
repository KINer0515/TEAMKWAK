package engine;

/**
 * Implements a simple game timer to measure elapsed time.
 * @author Amartsogt / CHO
 */
public class GameTimer {

    private long startTime;
    private long stopTime;
    private boolean running;
    
    public GameTimer() {
        this.startTime = 0L;
        this.stopTime = 0L;
        this.running = false;
    }

    /**
     * Starts the timer.
     */
    public void start() {
        this.startTime = System.nanoTime();
        this.running = true;
        this.stopTime = 0L;
    }

    /**
     * Stops the timer.
     */
    public void stop() {
        if (this.running) {
            this.stopTime = System.nanoTime();
            this.running = false;
        }
    }

    public void resume() {
        if (!this.running && this.stopTime > 0) {
            long pausedDuration = this.stopTime - this.startTime;
            this.startTime = System.nanoTime() - pausedDuration;
            this.running = true;
            this.stopTime = 0L;
        }
    }

    /**
     * @return Elapsed time in milliseconds.
     */
    public long getElapsedTime() {
        final long endTime = this.running ? System.nanoTime() : this.stopTime;
        return (endTime - this.startTime) / 1000000;
    }

    /**
     * Checks if the timer is currently running.
     * @return True if the timer is running.
     */
    public boolean isRunning() {
        return this.running;
    }
}