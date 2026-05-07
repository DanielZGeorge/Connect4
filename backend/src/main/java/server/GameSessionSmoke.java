package server;

public class GameSessionSmoke {
    public static void main(String[] args) {
        GameSession game = new GameSession("alice", "bob", true);
        game.resetTurnDeadline(30_000L);

        System.out.println("Initial state: " + game.buildSnapshotJson());
        game.applyMove("alice", 3);
        System.out.println("After alice move: " + game.buildSnapshotJson());
        game.applyMove("bob", 3);
        System.out.println("After bob move: " + game.buildSnapshotJson());
    }
}

