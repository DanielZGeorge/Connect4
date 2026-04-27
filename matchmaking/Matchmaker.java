package matchmaking;

import model.HumanPlayer;
import model.Player;
import Game_Engine.GameSession;

import java.util.*;
import java.util.concurrent.*;

public class Matchmaker implements Runnable {

    private static final int    INITIAL_ELO_RANGE         = 100;
    private static final int    RANGE_EXPANSION_AMOUNT     = 50;
    private static final long   RANGE_EXPANSION_INTERVAL  = 30_000L;  // 30 seconds
    private static final long   MAX_WAIT_TIME             = 120_000L; // 2 minutes
    private static final long   POLL_INTERVAL_MS          = 1_000L;

    private final Queue<HumanPlayer> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final ExecutorService gameThreadPool;
    private volatile boolean running = true;

    public Matchmaker(ExecutorService gameThreadPool) {
        this.gameThreadPool = gameThreadPool;
    }

    // -------------------------------------------------------------------------
    // Public queue management
    // -------------------------------------------------------------------------

    public void addPlayer(HumanPlayer player) {
        player.setCurrentEloRange(INITIAL_ELO_RANGE);
        waitingPlayers.add(player);
    }

    public void removePlayer(HumanPlayer player) {
        waitingPlayers.remove(player);
    }

    public int getQueueSize() {
        return waitingPlayers.size();
    }

    public List<HumanPlayer> getWaitingPlayers() {
        return new ArrayList<>(waitingPlayers);
    }

    // -------------------------------------------------------------------------
    // Matchmaking logic
    // -------------------------------------------------------------------------

    /**
     * Returns the best available opponent for {@code player} within their
     * current Elo range, or null if none exists yet.
     */
    public HumanPlayer findMatch(HumanPlayer player) {
        HumanPlayer bestMatch = null;
        int smallestDiff = Integer.MAX_VALUE;

        for (HumanPlayer candidate : waitingPlayers) {
            if (candidate == player) continue;
            int diff = Math.abs(candidate.getElo() - player.getElo());
            int allowedRange = Math.max(player.getCurrentEloRange(),
                                        candidate.getCurrentEloRange());
            if (diff <= allowedRange && diff < smallestDiff) {
                smallestDiff = diff;
                bestMatch = candidate;
            }
        }
        return bestMatch;
    }

    /** Widens a player's acceptable Elo range by RANGE_EXPANSION_AMOUNT. */
    public void expandRange(HumanPlayer player) {
        player.setCurrentEloRange(player.getCurrentEloRange() + RANGE_EXPANSION_AMOUNT);
    }

    /**
     * Pairs two players into a GameSession and submits it to the thread pool.
     * The lower-Elo player has a weighted probability of going first.
     */
    public GameSession createMatch(Player p1, Player p2) {
        int firstPlayer = assignFirstTurn(p1, p2);
        String user1 = (firstPlayer == 1) ? p1.getUsername() : p2.getUsername();
        String user2 = (firstPlayer == 1) ? p2.getUsername() : p1.getUsername();
        GameSession session = new GameSession(user1, user2, firstPlayer);
        gameThreadPool.submit(session);
        return session;
    }

    /**
     * Weighted coin flip: lower-Elo player gets up to 80% chance to go first.
     * At equal Elo the split is 50/50; the advantage grows with the rating gap,
     * capped at 30 extra percentage points (80/20 split).
     */
    private int assignFirstTurn(Player p1, Player p2) {
        int eloDiff = Math.abs(p1.getElo() - p2.getElo());
        double lowerEloProb = 0.5 + Math.min(0.3, eloDiff / 2000.0);
        boolean p1IsLower = p1.getElo() <= p2.getElo();
        double rand = Math.random();
        if (p1IsLower) {
            return (rand < lowerEloProb) ? 1 : 2;
        } else {
            return (rand < lowerEloProb) ? 2 : 1;
        }
    }

    // -------------------------------------------------------------------------
    // Main loop
    // -------------------------------------------------------------------------

    public void stop() { running = false; }

    @Override
    public void run() {
        while (running) {
            try {
                processQueue();
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processQueue() {
        Set<HumanPlayer> matched = new HashSet<>();
        List<HumanPlayer> snapshot = new ArrayList<>(waitingPlayers);

        for (HumanPlayer player : snapshot) {
            if (matched.contains(player)) continue;

            long waitMs = System.currentTimeMillis() - player.getQueueJoinTime();

            // Expand the search range once per 30-second interval
            int expectedExpansions = (int) (waitMs / RANGE_EXPANSION_INTERVAL);
            int expectedRange = INITIAL_ELO_RANGE + expectedExpansions * RANGE_EXPANSION_AMOUNT;
            if (player.getCurrentEloRange() < expectedRange) {
                player.setCurrentEloRange(expectedRange);
            }

            // Player has waited too long — pull them out and offer a bot
            if (waitMs >= MAX_WAIT_TIME) {
                waitingPlayers.remove(player);
                notifyBotOffer(player);
                continue;
            }

            HumanPlayer opponent = findMatch(player);
            if (opponent != null && !matched.contains(opponent)) {
                matched.add(player);
                matched.add(opponent);
                waitingPlayers.remove(player);
                waitingPlayers.remove(opponent);
                createMatch(player, opponent);
            }
        }
    }

    /**
     * Signals to the player that no human match was found and a bot game is
     * available. NioServer (George) should send the actual message over the socket.
     */
    private void notifyBotOffer(HumanPlayer player) {
        // TODO: route through NioServer to send "BOT_OFFER" message to player's socket
        System.out.println("[Matchmaker] " + player.getUsername()
                + " waited " + MAX_WAIT_TIME / 1000 + "s — offering bot match.");
    }
}
