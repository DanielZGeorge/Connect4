package bot; 
import game.GameSession;
import java.util.List;


public class MedBot extends BotPlayer{
    public MediumBot(int playerNumber) {
        super("Medium Bot", playerNumber);
    }

    
     // Evaluates every valid move and returns the column with the best score.
    @Override
    public int chooseMove(GameSession game) {
        List<Integer> validMoves = game.getValidMoves();
        int[][] board = game.getRawBoard();

        int bestScore = Integer.MIN_VALUE;
        int bestCol = validMoves.get(0); // fallback

        for (int col : validMoves) {
            int score = evaluateMove(board, col);
            System.out.println("MediumBot: col " + col + " scored " + score);

            if (score > bestScore) {
                bestScore = score;
                bestCol = col;
            }
        }

        System.out.println("MediumBot chose column: " + bestCol);
        return bestCol;
    }


//Determines priority by checking if the game wins (1000 points), if the move blocks (900), or the length of the chain post move
//Uses a potential move 
    private int evaluateMove(int[][] board, int col) {
        int opponent = getOpponentNumber();

       //Can bot win? If so, take it
        int[][] botBoard = simulateMove(board, col, playerNumber);
        if (botBoard != null && hasWinner(botBoard)) {
            return 1000; 
        }

        //Does the other player win if this spot isn’t taken? If yes, block
        int[][] opponentBoard = simulateMove(board, col, opponent);
        if (opponentBoard != null && hasWinner(opponentBoard)) {
            return 900; 
        }

        // Score by the longest chain this move creates ---
        if (botBoard != null) {
            return getLongestChain(botBoard, col, playerNumber);
        }

        return 0;
    }

    
}
