package model;

/**
 * Abstract base for all players (human and bot).
 * BotPlayer should also extend this class.
 */
public abstract class Player {
    protected int id;
    protected String username;
    protected int elo;

    public Player(String username, int elo) {
        this.username = username;
        this.elo = elo;
    }

    public abstract String getName();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public int getElo() { return elo; }
    public void setElo(int elo) { this.elo = elo; }
}