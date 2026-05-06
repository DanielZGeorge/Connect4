package server;
import java.util.List;
public class hardBot extends botPlayer {
    public hardBot(int id, String username, int elo) {
        super(id, username, elo);
    }
    public int chooseMove(GameSession session) {
        List<Integer> validMoves = session.getValidMoves();
        List<Integer> validMoves = session.getValidMoves();
        if (validMoves.isEmpty()) return -1;
        int bestMove = -1;
        int maxScore = Integer.MIN_VALUE;
        int botPlayerNum = session.getCurrentUser();
        List<List<Integer>> boardList = session.getBoard();
        int[][] board = new int[7][6];
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                board[c][5 - r] = boardList.get(r).get(c);
            }
        }
        for (int move : validMoves) {
            int row = getDropRow(board, move);
            board[move][row] = botPlayerNum;
            int score = minimax(board, 5, Integer.MIN_VALUE, Integer.MAX_VALUE, false, botPlayerNum);
            board[move][row] = 0;
            if (score > maxScore) {
                maxScore = score;
                bestMove = move;
            }
        }
        return bestMove == -1 ? validMoves.get(0) : bestMove;
    }
    private int minimax(int[][] board, int depth, int alpha, int beta, boolean isMaximizing, int botPlayerNum) {
        int opponentNum = (botPlayerNum == 1) ? 2 : 1;
        if (checkWin(board, botPlayerNum)) return 1000000 + depth;
        if (checkWin(board, opponentNum)) return -1000000 - depth;
        if (isBoardFull(board) || depth == 0) return evaluateBoard(board, botPlayerNum);
        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int c = 0; c < 7; c++) {
                int r = getDropRow(board, c);
                if (r != -1) {
                    board[c][r] = botPlayerNum;
                    int eval = minimax(board, depth - 1, alpha, beta, false, botPlayerNum);
                    board[c][r] = 0;
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int c = 0; c < 7; c++) {
                int r = getDropRow(board, c);
                if (r != -1) {
                    board[c][r] = opponentNum;
                    int eval = minimax(board, depth - 1, alpha, beta, true, botPlayerNum);
                    board[c][r] = 0;
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) break;
                }
            }
            return minEval;
        }
    }
    private int getDropRow(int[][] board, int col) {
        for (int r = 0; r < 6; r++) {
            if (board[col][r] == 0) return r;
        }
        if (validMoves.isEmpty()) return -1;
        int bestMove = -1;
    private boolean isBoardFull(int[][] board) {
        for (int c = 0; c < 7; c++) {
            if (board[c][5] == 0) return false;
        }
        return true;
    }
    private int evaluateBoard(int[][] board, int player) {
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
        int maxScore = Integer.MIN_VALUE;
        int botPlayerNum = session.getCurrentUser();
        List<List<Integer>> boardList = session.getBoard();
        int[][] board = new int[7][6];
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                board[c][5 - r] = boardList.get(r).get(c);
            }
        }
        for (int move : validMoves) {
            int row = getDropRow(board, move);
            board[move][row] = botPlayerNum;
            int score = minimax(board, 5, Integer.MIN_VALUE, Integer.MAX_VALUE, false, botPlayerNum);
            board[move][row] = 0;
            if (score > maxScore) {
                maxScore = score;
                bestMove = move;
            }
        }
        return bestMove == -1 ? validMoves.get(0) : bestMove;
    }
    private int minimax(int[][] board, int depth, int alpha, int beta, boolean isMaximizing, int botPlayerNum) {
        int opponentNum = (botPlayerNum == 1) ? 2 : 1;
        if (checkWin(board, botPlayerNum)) return 1000000 + depth;
        if (checkWin(board, opponentNum)) return -1000000 - depth;
        if (isBoardFull(board) || depth == 0) return evaluateBoard(board, botPlayerNum);
        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int c = 0; c < 7; c++) {
                int r = getDropRow(board, c);
                if (r != -1) {
                    board[c][r] = botPlayerNum;
                    int eval = minimax(board, depth - 1, alpha, beta, false, botPlayerNum);
                    board[c][r] = 0;
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int c = 0; c < 7; c++) {
                int r = getDropRow(board, c);
                if (r != -1) {
                    board[c][r] = opponentNum;
                    int eval = minimax(board, depth - 1, alpha, beta, true, botPlayerNum);
                    board[c][r] = 0;
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) break;
                }
            }
            return minEval;
        }
    }
    private int getDropRow(int[][] board, int col) {
        for (int r = 0; r < 6; r++) {
            if (board[col][r] == 0) return r;
        }
        return -1;
    }
    private boolean isBoardFull(int[][] board) {
        for (int c = 0; c < 7; c++) {
            if (board[c][5] == 0) return false;
        }
        return true;
    }
    private int evaluateBoard(int[][] board, int player) {
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
