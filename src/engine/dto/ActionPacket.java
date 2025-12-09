package engine.dto;

/**
 *
 * A class that contains action information sent from the outside (Python)
 * /action Map the request body (JSON) of the endpoint
 */
public class ActionPacket {
    public int moveX; // -1, 0, +1
    public int moveY; // -1, 0, +1
    public boolean shoot;
}