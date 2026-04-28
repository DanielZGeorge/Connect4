package server;

public class easyBot extends botPlayer {
    public easyBot(int id, String username, int elo) {
        super(id, username, elo);
    }

    @Override
    public int chooseMove(GameSession session) {
        return -1;
    }
}
