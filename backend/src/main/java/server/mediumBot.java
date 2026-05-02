package server;

public class mediumBot extends botPlayer {
    public mediumBot(int id, String username, int elo) {
        super(id, username, elo);
    }

    @Override
    public int chooseMove(GameSession session) {
        return -1;
    }
}
