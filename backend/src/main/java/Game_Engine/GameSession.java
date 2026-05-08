package Game_Engine;

import java.util.ArrayList;
import java.util.List;

// Manages a single Connect 4 Match 
// Connect 4 Board includes a 7 column (index 0-6) by 6 row (index 0-5) grid
// Row 0 starts at the bottom 
// For each box: 0 = empty, 1= player 1’s piece, and 2= player 2’s piece 

public class GameSession implements Runnable {

    // tells us which player’s turn it is
    private int currentUser;  

    // Usernames of each player 
    private String user1;        
    private String user2;    

    //[col][row] : 7x6 grid
    private int[][] gameBoard;       

    private static final int COLS = 7;
    private static final int ROWS = 6;
    private static final int WIN_LENGTH = 4;

    // Constructor

    public GameSession(String user1, String user2, int startingPlayer) {
        this.user1 = user1;
        this.user2 = user2;
        this.currentUser = startingPlayer; // 1 or 2, decided by weighted random
        this.gameBoard = new int[COLS][ROWS]; // Java auto-fills with 0s (empty)
    }

    // Runnable

    @Override
    public void run() {
        // TODO
        System.out.println("Game started: " + user1 + " vs " + user2);
    }

    // Core Game Methods

    // A player will drop a piece into the given column they chose. 
    // The piece will be placed to the lowest empty row in that column
    // Returns true if the move was valid; false if the column if full or invalid

    public boolean takeTurn(int col) {
        // Check if the column is within bounds
        if (col < 0 || col >= COLS) {
            System.out.println("Invalid column: " + col);
            return false;
        }

        // Scan from the bottom row upward to find the lowest empty slot
        for (int row = 0; row < ROWS; row++) {
            if (gameBoard[col][row] == 0) {      // 0 means empty
                gameBoard[col][row] = currentUser; // place the piece
                return true;
            }
        }

        // If we get here, the column is completely full
        System.out.println("Column " + col + " is full!");
        return false;
    }

// This returns a list of columns that still have >= 1 empty slot
// The AI bots will use this to know which slots are available 
 
    public List<Integer> getValidMoves() {
        List<Integer> validCols = new ArrayList<>();

        for (int col = 0; col < COLS; col++) {
            // The top row (ROWS - 1) being empty means the column has space
            if (gameBoard[col][ROWS - 1] == 0) {
                validCols.add(col);
            }
        }

        return validCols;
    }

    public void switchPlayer() {
        currentUser = (currentUser == 1) ? 2 : 1;
    }

     // Checks whether the current board has a 4-in-a-row win (Horizontal, Vertical, Diagonal)
 
    public boolean checkWinner() {
            int[][] directions = {
            {1, 0},   // horizontal 
            {0, 1},   // vertical 
            {1, 1},   // diagonal 
            {1, -1}   // diagonal 
        };

        // Check every cell on the board as a potential starting point
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {

                int player = gameBoard[col][row];
                if (player == 0) continue; // skip empty cells

                // Try extending 4 in a row in each direction
                for (int[] dir : directions) {
                    if (countInDirection(col, row, dir[0], dir[1], player) >= WIN_LENGTH) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


     // Helper function for checkWinner
     // This starts at (col, row), counts how many consecutive pieces belong to `player` going in the direction (colStep, rowStep).

    private int countInDirection(int col, int row, int colStep, int rowStep, int player) {
        int count = 0;

        for (int i = 0; i < WIN_LENGTH; i++) {
            int c = col + i * colStep;
            int r = row + i * rowStep;

            // Make sure we haven't stepped off the board/out-of-bounds
            if (c < 0 || c >= COLS || r < 0 || r >= ROWS) break;

            // If this piece doesn't belong to the player, stop counting
            if (gameBoard[c][r] != player) break;

            count++;
        }

        return count;
    }

       public boolean checkFull() {
        for (int col = 0; col < COLS; col++) {
            if (gameBoard[col][ROWS - 1] == 0) {
                return false; // found an empty top cell — not full yet
            }
        }
        return true;
    }

       public boolean gameOver() {
        return checkWinner() || checkFull();
    }

   // This is called when the game ends. It determines the outcome and triggers Elo updates + database save 
   
    public void endGame() {
        if (checkWinner()) {
            String winner = (currentUser == 1) ? user1 : user2;
            String loser  = (currentUser == 1) ? user2 : user1;
            System.out.println("Game over! Winner: " + winner + ", Loser: " + loser);

            // TODO: call DAO to save match result and update Elo ratings
        } else {
            System.out.println("Game over! It's a draw.");

            // TODO: call DAO to save draw result
        }
    }

   // Returns a copy of the board which will be used by AI bots to simulate moves
   
    public List<List<Integer>> getBoard() {
        List<List<Integer>> boardCopy = new ArrayList<>();

        for (int col = 0; col < COLS; col++) {
            List<Integer> column = new ArrayList<>();
            for (int row = 0; row < ROWS; row++) {
                column.add(gameBoard[col][row]);
            }
            boardCopy.add(column);
        }

        return boardCopy;
    }

    // Getters

    public int getCurrentUser()  { return currentUser; }
    public String getUser1()     { return user1; }
    public String getUser2()     { return user2; }

     // for bots to read directly
    public int[][] getRawBoard() { return gameBoard; }
}