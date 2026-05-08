
package model;

import Game_Engine.GameSession;
import java.util.List;

public abstract class BotPlayer {

    protected String username;  //Difficulty Level
    protected int playerNumber; // Turn

    public BotPlayer(String username, int playerNumber) {
        this.username = username;
        this.playerNumber = playerNumber;
    }

//To be overwritten based on difficulty 
    public abstract int chooseMove(GameSession game);


//Gets the player number of the player 
    protected int getOpponentNumber() {
        return (playerNumber == 1) ? 2 : 1;
    }

//Returns what the board would look like after a potential move
    protected int[][] simulateMove(int[][] board, int col, int player) {
        int rows = board[0].length;
        int cols = board.length;

        // Deep copy the board 
        int[][] newBoard = new int[cols][rows];
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                newBoard[c][r] = board[c][r];
            }
        }

        // Find the lowest empty row in this column 
        for (int row = 0; row < rows; row++) {
            if (newBoard[col][row] == 0) {
                newBoard[col][row] = player;
                return newBoard; // return the updated copy
            }
        }

        return null; // column was full 
    }

 
//Determines whether the game board has a winner, used to evaluate potential moves.
    protected boolean hasWinner(int[][] board) {
        int cols = board.length;
        int rows = board[0].length;
        int[][] directions = {{1,0},{0,1},{1,1},{1,-1}};

        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                int player = board[col][row];
                if (player == 0) continue;

                for (int[] dir : directions) {
                    int count = 0;
                    for (int i = 0; i < 4; i++) {
                        int c = col + i * dir[0];
                        int r = row + i * dir[1];
                        if (c < 0 || c >= cols || r < 0 || r >= rows) break;
                        if (board[c][r] != player) break;
                        count++;
                    }
                    if (count >= 4) return true;
                }
            }
        }
        return false;
    }

    public String getUsername() { return username; }
    public int getPlayerNumber() { return playerNumber; }
}