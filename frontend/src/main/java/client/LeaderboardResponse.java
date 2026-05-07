package client;

import java.util.List;

public class LeaderboardResponse {
    private final List<LeaderboardEntry> entries;
    private final int total;

    public LeaderboardResponse(List<LeaderboardEntry> entries, int total) {
        this.entries = entries;
        this.total = total;
    }

    public List<LeaderboardEntry> getEntries() {
        return entries;
    }

    public int getTotal() {
        return total;
    }
}

