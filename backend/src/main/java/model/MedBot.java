package model; 
import Game_Engine.GameSession;
import java.util.List;


public class MedBot extends BotPlayer{
    public MedBot(int playerNumber) {
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

    // Finds longest chain after placing a piece 
    private int getLongestChain(int[][] board, int col, int player) {
        int rows = board[0].length;

        // Find which row the piece landed in 
        int landedRow = -1;
        for (int row = 0; row < rows; row++) {
            if (board[col][row] == player) {
                landedRow = row; 
            }
        }

        if (landedRow == -1) return 0; 

        // Check all 4 directions and find the longest chain through (col, landedRow)
        int[][] directions = {{1,0},{0,1},{1,1},{1,-1}};
        int longest = 0;

        for (int[] dir : directions) {
            // Count in the positive direction + negative direction, minus 1 for center
            int chain = countConsecutive(board, col, landedRow,  dir[0],  dir[1], player)
                      + countConsecutive(board, col, landedRow, -dir[0], -dir[1], player)
                      - 1; // subtract 1 because the center piece is counted twice
            longest = Math.max(longest, chain);
        }

        return longest;
    }
//Count consecutive from (col, row)
    private int countConsecutive(int[][] board, int col, int row,
                                  int colStep, int rowStep, int player) {
        int cols = board.length;
        int rows = board[0].length;
        int count = 0;

        int c = col;
        int r = row;

        while (c >= 0 && c < cols && r >= 0 && r < rows && board[c][r] == player) {
            count++;
            c += colStep;
            r += rowStep;
        }

        return count;
    }
}