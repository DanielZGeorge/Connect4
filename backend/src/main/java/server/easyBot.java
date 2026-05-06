package server;

import java.util.List;
import java.util.Random;

public class easyBot extends botPlayer {
    private Random random = new Random();

    public easyBot(int id, String username, int elo) {
        super(id, username, elo);
    }

    @Override
    public int chooseMove(GameSession session) {
        List<Integer> validMoves = session.getValidMoves();
        if (validMoves.isEmpty()) return -1;
        return validMoves.get(random.nextInt(validMoves.size()));
    }
}
