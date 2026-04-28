package server;

public abstract class player {
    private final int id;
    private final String username;
    private int elo;

    protected player(int id, String username, int elo) {
        this.id = id;
        this.username = username;
        this.elo = elo;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }
}
