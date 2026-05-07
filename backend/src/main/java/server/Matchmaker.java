package server;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Matchmaker implements Runnable {
    private static final int INITIAL_RANGE = 150;
    private static final int RANGE_INCREMENT = 100;
    private static final long RANGE_STEP_MILLIS = 10_000L;
    private static final long POLL_INTERVAL_MILLIS = 200L;

    private final Queue<WaitingPlayer> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final GameHub hub;
    private volatile boolean running = true;

    public Matchmaker(GameHub hub) {
        this.hub = hub;
    }

    @Override
    public void run() {
        while (running) {
            try {
                matchPlayers();
                Thread.sleep(POLL_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void shutdown() {
        running = false;
    }

    public void addPlayer(WaitingPlayer player) {
        waitingPlayers.add(player);
    }

    public void removePlayer(String username) {
        waitingPlayers.removeIf(p -> p.username().equals(username));
    }

    private void matchPlayers() {
        List<WaitingPlayer> snapshot = new ArrayList<>(waitingPlayers);
        for (int i = 0; i < snapshot.size(); i++) {
            WaitingPlayer p1 = snapshot.get(i);
            if (!waitingPlayers.contains(p1)) continue;
            for (int j = i + 1; j < snapshot.size(); j++) {
                WaitingPlayer p2 = snapshot.get(j);
                if (!waitingPlayers.contains(p2)) continue;
                if (isCompatible(p1, p2)) {
                    waitingPlayers.remove(p1);
                    waitingPlayers.remove(p2);
                    hub.createMatch(p1.username(), p2.username());
                    return;
                }
            }
        }
    }

    private boolean isCompatible(WaitingPlayer p1, WaitingPlayer p2) {
        int range1 = computeRange(p1.joinedAtMillis());
        int range2 = computeRange(p2.joinedAtMillis());
        int diff = Math.abs(p1.elo() - p2.elo());
        return diff <= range1 && diff <= range2;
    }

    private int computeRange(long joinedAtMillis) {
        long elapsed = Math.max(0L, System.currentTimeMillis() - joinedAtMillis);
        long steps = elapsed / RANGE_STEP_MILLIS;
        return INITIAL_RANGE + (int) steps * RANGE_INCREMENT;
    }

    public record WaitingPlayer(String username, int elo, long joinedAtMillis) {
    }
}
