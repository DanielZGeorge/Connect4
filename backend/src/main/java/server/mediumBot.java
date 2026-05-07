package server;

import java.util.List;
import java.util.Random;

public class mediumBot extends botPlayer {
    private static final int COLS = 7;
    private static final int ROWS = 6;
    private final Random random = new Random();

    public mediumBot(int id, String username, int elo) {
        super(id, username, elo);
    }

    @Override
    public int chooseMove(GameSession session) {
        List<Integer> validMoves = session.getValidMoves();
        if (validMoves.isEmpty()) return -1;

        int bestMove = -1;
        int bestScore = Integer.MIN_VALUE;
        int botPlayerNum = session.getCurrentUser();
        int[][] board = buildBoard(session);

        for (int move : validMoves) {
            int row = getDropRow(board, move);
            if (row == -1) {
                continue;
            }
            board[move][row] = botPlayerNum;
            int score = maxConnected(board, botPlayerNum);
            board[move][row] = 0;

            if (score > bestScore || (score == bestScore && random.nextBoolean())) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove == -1 ? validMoves.get(0) : bestMove;
    }

    private int[][] buildBoard(GameSession session) {
        List<List<Integer>> boardList = session.getBoard();
        int[][] board = new int[COLS][ROWS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[c][ROWS - 1 - r] = boardList.get(r).get(c);
            }
        }
        return board;
    }

    private int getDropRow(int[][] board, int col) {
        for (int r = 0; r < ROWS; r++) {
            if (board[col][r] == 0) return r;
        }
        return -1;
    }

    private int maxConnected(int[][] board, int player) {
        int max = 0;
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                if (board[c][r] != player) continue;
                max = Math.max(max, countDirection(board, c, r, 1, 0, player));
                max = Math.max(max, countDirection(board, c, r, 0, 1, player));
                max = Math.max(max, countDirection(board, c, r, 1, 1, player));
                max = Math.max(max, countDirection(board, c, r, 1, -1, player));
            }
        }
        return max;
    }

    private int countDirection(int[][] board, int col, int row, int dCol, int dRow, int player) {
        int count = 0;
        int c = col;
        int r = row;
        while (c >= 0 && c < COLS && r >= 0 && r < ROWS && board[c][r] == player) {
            count++;
            c += dCol;
            r += dRow;
        }
        return count;
    }
}
