package bot;
import game.GameSession;
import java.util.List;
import java.util.Random;

public class EasyBot extends BotPlayer {

    private Random random;

    public EasyBot(int playerNumber) {
        super("Easy Bot", playerNumber);
        this.random = new Random();
    }

    @Override
    public int chooseMove(GameSession game) {
        List<Integer> validMoves = game.getValidMoves();
       // Pick a random index from the list of valid columns
        int randomIndex = random.nextInt(validMoves.size());
        int chosenCol = validMoves.get(randomIndex);

      
        System.out.println("EasyBot chose column: " + chosenCol);
        return chosenCol;
    }
}
