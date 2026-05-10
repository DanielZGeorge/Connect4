package client;

public class ActiveMatch {
    private final String gameId;
    private final String player1;
    private final String player2;

    public ActiveMatch(String gameId, String player1, String player2) {
        this.gameId = gameId;
        this.player1 = player1;
        this.player2 = player2;
    }

    public String getGameId() {
        return gameId;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }
}
