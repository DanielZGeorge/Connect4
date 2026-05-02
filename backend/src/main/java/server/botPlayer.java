package server;

public abstract class botPlayer extends player {
    protected botPlayer(int id, String username, int elo) {
        super(id, username, elo);
    }

    public abstract int chooseMove(GameSession session);
}
