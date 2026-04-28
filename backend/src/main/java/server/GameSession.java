package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameSession implements Runnable {
    private int currentUser;
    private final String user1;
    private final String user2;
    private final int[][] gameBoard;

    public GameSession(String user1, String user2) {
        this.currentUser = 1;
        this.user1 = user1;
        this.user2 = user2;
        this.gameBoard = new int[7][6];
    }

    @Override
    public void run() {
        // Placeholder for session loop.
    }

    public boolean takeTurn(int col) {
        if (col < 0 || col >= gameBoard.length) {
            return false;
        }
        for (int row = 0; row < gameBoard[col].length; row++) {
            if (gameBoard[col][row] == 0) {
                gameBoard[col][row] = currentUser;
                return true;
            }
        }
        return false;
    }

    public List<Integer> getValidMoves() {
        List<Integer> moves = new ArrayList<>();
        for (int col = 0; col < gameBoard.length; col++) {
            if (gameBoard[col][gameBoard[col].length - 1] == 0) {
                moves.add(col);
            }
        }
        return moves;
    }

    public void switchPlayer() {
        currentUser = (currentUser == 1) ? 2 : 1;
    }

    public boolean checkWinner() {
        return false;
    }

    public boolean checkFull() {
        for (int col = 0; col < gameBoard.length; col++) {
            if (gameBoard[col][gameBoard[col].length - 1] == 0) {
                return false;
            }
        }
        return true;
    }

    public void endGame() {
        // Placeholder: update player stats via DAO.
    }

    public boolean gameOver() {
        return checkWinner() || checkFull();
    }

    public List<List<Integer>> getBoard() {
        List<List<Integer>> board = new ArrayList<>();
        for (int row = gameBoard[0].length - 1; row >= 0; row--) {
            List<Integer> rowValues = new ArrayList<>();
            for (int col = 0; col < gameBoard.length; col++) {
                rowValues.add(gameBoard[col][row]);
            }
            board.add(Collections.unmodifiableList(rowValues));
        }
        return Collections.unmodifiableList(board);
    }

    public int getCurrentUser() {
        return currentUser;
    }

    public String getUser1() {
        return user1;
    }

    public String getUser2() {
        return user2;
    }
}
