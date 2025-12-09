package engine.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Game state DTO to be transferred from Java → Python
 * example: { "state": { ... } }
 * In the first version, it simply consists of key-value form.
 * Detailed properties can be expanded for DQN learning in the future.
 */
public class StatePacket {
    public int frame;
    public int playerX;
    public int playerY;
    public int playerHp;
    public List<List<Integer>> bullets;
    public List<List<Integer>> enemies;
    public List<List<String>> items;
    public int score;
    public List<List<Integer>> enemyDamageEvents;
}