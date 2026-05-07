package server;

import java.util.List;

public class hardBot extends botPlayer {
    private static final int COLS = 7;
    private static final int ROWS = 6;
    private static final int WIN_SCORE = 1_000_000;
    private static final int MAX_DEPTH = 6;
    private static final int[] MOVE_ORDER = {3, 2, 4, 1, 5, 0, 6};

    public hardBot(int id, String username, int elo) {
        super(id, username, elo);
    }

    @Override
    public int chooseMove(GameSession session) {
        List<Integer> validMoves = session.getValidMoves();
        if (validMoves.isEmpty()) return -1;

        int botPlayerNum = session.getCurrentUser();
        int[][] board = buildBoard(session);
        int bestMove = validMoves.get(0);
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE + 1;
        int beta = Integer.MAX_VALUE - 1;

        for (int move : MOVE_ORDER) {
            if (!validMoves.contains(move)) continue;
            int row = getDropRow(board, move);
            if (row == -1) continue;

            board[move][row] = botPlayerNum;
            int score = -negamax(board, MAX_DEPTH - 1, -beta, -alpha, opposite(botPlayerNum));
            board[move][row] = 0;

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, score);
        }

        return bestMove;
    }

    private int negamax(int[][] board, int depth, int alpha, int beta, int player) {
        int opponent = opposite(player);

        if (checkWin(board, opponent)) return -WIN_SCORE - depth;
        if (checkWin(board, player)) return WIN_SCORE + depth;
        if (depth == 0 || isBoardFull(board)) return evaluateBoard(board, player);

        int best = Integer.MIN_VALUE + 1;
        for (int move : MOVE_ORDER) {
            int row = getDropRow(board, move);
            if (row == -1) continue;

            board[move][row] = player;
            int score = -negamax(board, depth - 1, -beta, -alpha, opponent);
            board[move][row] = 0;

            if (score > best) {
                best = score;
            }
            if (score > alpha) {
                alpha = score;
            }
            if (alpha >= beta) {
                break;
            }
        }
        return best;
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

    private boolean isBoardFull(int[][] board) {
        for (int c = 0; c < COLS; c++) {
            if (board[c][ROWS - 1] == 0) return false;
        }
        return true;
    }

    private int evaluateBoard(int[][] board, int player) {
        int score = 0;
        score += centerScore(board, player);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS - 3; c++) {
                score += scoreWindow(board, player, c, r, 1, 0);
            }
        }
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS - 3; r++) {
                score += scoreWindow(board, player, c, r, 0, 1);
            }
        }
        for (int c = 0; c < COLS - 3; c++) {
            for (int r = 0; r < ROWS - 3; r++) {
                score += scoreWindow(board, player, c, r, 1, 1);
            }
        }
        for (int c = 0; c < COLS - 3; c++) {
            for (int r = 3; r < ROWS; r++) {
                score += scoreWindow(board, player, c, r, 1, -1);
            }
        }

        return score;
    }

    private int centerScore(int[][] board, int player) {
        int score = 0;
        int centerCol = COLS / 2;
        for (int r = 0; r < ROWS; r++) {
            if (board[centerCol][r] == player) score += 3;
        }
        return score;
    }

    private int scoreWindow(int[][] board, int player, int col, int row, int dCol, int dRow) {
        int opponent = opposite(player);
        int playerCount = 0;
        int opponentCount = 0;
        int emptyCount = 0;

        for (int i = 0; i < 4; i++) {
            int value = board[col + i * dCol][row + i * dRow];
            if (value == player) playerCount++;
            else if (value == opponent) opponentCount++;
            else emptyCount++;
        }

        if (playerCount == 4) return WIN_SCORE;
        if (opponentCount == 4) return -WIN_SCORE;
        if (playerCount == 3 && emptyCount == 1) return 120;
        if (playerCount == 2 && emptyCount == 2) return 15;
        if (opponentCount == 3 && emptyCount == 1) return -140;

        return 0;
    }

    private boolean checkWin(int[][] gameBoard, int currentUser) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS - 3; col++) {
                if (gameBoard[col][row] == currentUser && gameBoard[col + 1][row] == currentUser && gameBoard[col + 2][row] == currentUser && gameBoard[col + 3][row] == currentUser) {
                    return true;
                }
            }
        }
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS - 3; row++) {
                if (gameBoard[col][row] == currentUser && gameBoard[col][row + 1] == currentUser && gameBoard[col][row + 2] == currentUser && gameBoard[col][row + 3] == currentUser) {
                    return true;
                }
            }
        }
        for (int col = 0; col < COLS - 3; col++) {
            for (int row = 0; row < ROWS - 3; row++) {
                if (gameBoard[col][row] == currentUser && gameBoard[col + 1][row + 1] == currentUser && gameBoard[col + 2][row + 2] == currentUser && gameBoard[col + 3][row + 3] == currentUser) {
                    return true;
                }
            }
        }
        for (int col = 0; col < COLS - 3; col++) {
            for (int row = 3; row < ROWS; row++) {
                if (gameBoard[col][row] == currentUser && gameBoard[col + 1][row - 1] == currentUser && gameBoard[col + 2][row - 2] == currentUser && gameBoard[col + 3][row - 3] == currentUser) {
                    return true;
                }
            }
        }
        return false;
    }

    private int opposite(int player) {
        return player == 1 ? 2 : 1;
    }
}
