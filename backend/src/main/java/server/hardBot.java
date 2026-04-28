package server;

public class hardBot extends botPlayer {
    public hardBot(int id, String username, int elo) {
        super(id, username, elo);
    }

    @Override
    public int chooseMove(GameSession session) {
        return -1;
    }
}
