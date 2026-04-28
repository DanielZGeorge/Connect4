package server;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Matchmaker implements Runnable {
    // Initialize the static final queue inline
    private static final Queue<humanPlayer> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final int initialEloRange;
    private final int maxWaitTime;

    public Matchmaker(int initialEloRange, int maxWaitTime) {
        this.initialEloRange = initialEloRange;
        this.maxWaitTime = maxWaitTime;
    }

    @Override
    public void run() {
        // Placeholder: matchmaking loop.
    }

    public static void addPlayer(humanPlayer player) {
        waitingPlayers.add(player);
    }

    public void removePlayer(humanPlayer player) {
        waitingPlayers.remove(player);
    }

    public humanPlayer findMatch(humanPlayer player) {
        return null;
    }

    public void expandRange(humanPlayer player) {
        // Placeholder.
    }

    public GameSession createMatch(player p1, player p2) {
        return new GameSession(p1.getUsername(), p2.getUsername());
    }
}
