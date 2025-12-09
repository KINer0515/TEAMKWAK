package engine;

import com.google.gson.Gson;
import engine.dto.ActionPacket;
import engine.dto.StatePacket;
import io.javalin.Javalin;
import io.javalin.http.Context;
import screen.GameScreen;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;


/**
 * Lightweight HTTP API server that exposes the current game state
 * and receives player actions from external sources (e.g., Python RL agent).
 */
public class ApiServer {

    private final Javalin app;
    private final Gson gson = new Gson();
    private final LinkedBlockingQueue<ActionPacket> actionQueue = new LinkedBlockingQueue<>(32);
    private final Supplier<StatePacket> snapshotSupplier;

    /**
     * @param port              Port number for the HTTP server
     * @param snapshotSupplier  A callback that provides the latest StatePacket snapshot
     */
    public ApiServer(int port, Supplier<StatePacket> snapshotSupplier) {
        this.snapshotSupplier = snapshotSupplier;

        app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
        });

        // Basic endpoints
        app.get("/health", ctx -> ctx.json(Map.of("ok", true)));
        app.get("/state", this::handleGetState);
        app.post("/action", this::handlePostAction);

        app.start(port);
        System.out.println("[ApiServer] Running on http://localhost:" + port);
    }

    /**
     * Returns the latest state snapshot in JSON.
     */
    private void handleGetState(Context ctx) {
        StatePacket s = snapshotSupplier.get();
        if (s == null) {
            ctx.status(503).result("{\"error\":\"no_state\"}");
            return;
        }
        ctx.result(gson.toJson(s));
    }

    /**
     * Receives an action packet from an external client (Python, etc.)
     * and adds it to the internal action queue.
     */
    private void handlePostAction(Context ctx) {
        try {
            ActionPacket a = gson.fromJson(ctx.body(), ActionPacket.class);
            if (a == null) {
                ctx.status(400).result("{\"error\":\"bad_json\"}");
                return;
            }
            actionQueue.offer(a);
            ctx.json(Map.of("ok", true));
        } catch (Exception e) {
            ctx.status(400).result("{\"error\":\"bad_request\"}");
        }
    }

    /**
     * Retrieves and removes the next action in the queue.
     */
    public ActionPacket pollAction() {
        return actionQueue.poll();
    }

    /**
     * Gracefully stops the server.
     */
    public void stop() {
        app.stop();
    }

    public static void start(int port) {
        Gson gson = new Gson();

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new GsonJsonMapper(gson));
        });

        app.start(8000);

        // Existing action endpoint
        app.post("/action", ctx -> {
            try {
                ActionPacket packet = gson.fromJson(ctx.body(), ActionPacket.class);

                // Basic validation
                if (packet == null) {
                    ctx.status(400).result("{\"error\":\"bad_json\"}");
                    return;
                }

                // Forward the action to the game core
                Core.handleExternalAction(packet);

                ctx.json(Map.of("ok", true));
            } catch (Exception e) {
                ctx.status(400).result("{\"error\":\"bad_request\"}");
            }
        });

        // === New endpoint: get current game state ===
        app.get("/state", ctx -> {
            GameScreen gameScreen = Core.getCurrentGameScreen();
            if (gameScreen == null) {
                ctx.status(503).result("Game screen is not active.");
                return;
            }

            StatePacket state = gameScreen.buildStatePacket();
            ctx.json(state);
        });
    }
}