package server;
import java.util.List;
import java.util.Random;
public class mediumBot extends botPlayer {
    private Random random = new Random();
    public mediumBot(int id, String username, int elo) {
        super(id, username, elo);
    }
    @Override
    public int chooseMove(GameSession session) {
        List<Integer> validMoves = session.getValidMoves();
        if (validMoves.isEmpty()) return -1;
        int bestMove = -1;
        int maxScore = -1000000;
        int botPlayerNum = session.getCurrentUser();
        for (int move : validMoves) {
            int score = evaluateMove(session, move, botPlayerNum);
            if (score > maxScore) {
                maxScore = score;
                bestMove = move;
            } else if (score == maxScore) {
                if (random.nextBoolean()) {
                    bestMove = move;
                }
            }
        }
        return bestMove == -1 ? validMoves.get(0) : bestMove;
    }
    private int evaluateMove(GameSession session, int col, int playerNum) {
        List<List<Integer>> boardList = session.getBoard();
        int[][] board = new int[7][6];
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                board[c][5 - r] = boardList.get(r).get(c);
            }
        }
        int dropRow = -1;
        for (int r = 0; r < 6; r++) {
            if (board[col][r] == 0) {
                dropRow = r;
                break;
            }
        }
        if (dropRow == -1) return -1000000;
        board[col][dropRow] = playerNum;
        int score = getScore(board, playerNum);
        board[col][dropRow] = 0;
        board[col][dropRow] = (playerNum == 1) ? 2 : 1;
        if (checkWin(board, (playerNum == 1) ? 2 : 1)) {
            score += 10000; // block opponent win
        }
        board[col][dropRow] = 0;
        board[col][dropRow] = playerNum;
        if (checkWin(board, playerNum)) {
            score += 100000; // winning is best
        }
        board[col][dropRow] = 0;
        return score;
    }
    private int getScore(int[][] board, int player) {
        int score = 0;
        for (int r = 0; r < 6; r++) {
            if (board[3][r] == player) score += 3;
            if (board[2][r] == player || board[4][r] == player) score += 2;
        }
        return score;
    }
    private boolean checkWin(int[][] gameBoard, int currentUser) {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 4; col++) {
                if (gameBoard[col][row] == currentUser && gameBoard[col + 1][row] == currentUser && gameBoard[col + 2][row] == currentUser && gameBoard[col + 3][row] == currentUser) {
                    return true;
                }
            }
        }
        for (int col = 0; col < 7; col++) {
            for (int row = 0; row < 3; row++) {
                if (gameBoard[col][row] == currentUser && gameBoard[col][row + 1] == currentUser && gameBoard[col][row + 2] == currentUser && gameBoard[col][row + 3] == currentUser) {
                    return true;
                }
            }
        }
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 3; row++) {
                if (gameBoard[col][row] == currentUser && gameBoard[col + 1][row + 1] == currentUser && gameBoard[col + 2][row + 2] == currentUser && gameBoard[col + 3][row + 3] == currentUser) {
                    return true;
                }
            }
        }
        for (int col = 0; col < 4; col++) {
            for (int row = 3; row < 6; row++) {
                if (gameBoard[col][row] == currentUser && gameBoard[col + 1][row - 1] == currentUser && gameBoard[col + 2][row - 2] == currentUser && gameBoard[col + 3][row - 3] == currentUser) {
                    return true;
                }
            }
        }
        return false;
    }
}
