package client;

public class LeaderboardEntry {
    private final String username;
    private final int elo;
    private final boolean inMatch;

    public LeaderboardEntry(String username, int elo, boolean inMatch) {
        this.username = username;
        this.elo = elo;
        this.inMatch = inMatch;
    }

    public String getUsername() {
        return username;
    }

    public int getElo() {
        return elo;
    }

    public boolean isInMatch() {
        return inMatch;
    }
}

