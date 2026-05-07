package server;

import java.util.UUID;

public class GameSession {
    private static final int COLS = 7;
    private static final int ROWS = 6;

    private final String gameId;
    private final String user1;
    private final String user2;
    private final int[][] gameBoard;
    private final boolean ranked;

    private int currentUser;
    private int winner;
    private boolean gameOver;
    private long turnEndsAt;
    private long turnDurationMillis;

    public GameSession(String user1, String user2, boolean ranked) {
        this.gameId = UUID.randomUUID().toString();
        this.user1 = user1;
        this.user2 = user2;
        this.ranked = ranked;
        this.currentUser = 1;
        this.winner = 0;
        this.gameOver = false;
        this.gameBoard = new int[COLS][ROWS];
    }

    public synchronized MoveResult applyMove(String username, int col) {
        if (gameOver) return new MoveResult(false, "GAME_OVER");

        int playerNum = getPlayerNumber(username);
        if (playerNum == 0) return new MoveResult(false, "NOT_IN_MATCH");
        if (playerNum != currentUser) return new MoveResult(false, "NOT_YOUR_TURN");
        if (col < 0 || col >= COLS) return new MoveResult(false, "INVALID_COLUMN");

        int row = getDropRow(col);
        if (row == -1) return new MoveResult(false, "COLUMN_FULL");

        gameBoard[col][row] = playerNum;

        if (checkWin(playerNum)) {
            gameOver = true;
            winner = playerNum;
        } else if (checkFull()) {
            gameOver = true;
            winner = 0;
        } else {
            currentUser = (currentUser == 1) ? 2 : 1;
            turnEndsAt = System.currentTimeMillis() + turnDurationMillis;
        }

        return new MoveResult(true, "OK");
    }

    public synchronized void forfeit(String username) {
        if (gameOver) return;
        int playerNum = getPlayerNumber(username);
        if (playerNum == 0) return;
        winner = (playerNum == 1) ? 2 : 1;
        gameOver = true;
    }

    public synchronized boolean handleTimeout(long now) {
        if (gameOver) return false;
        if (now < turnEndsAt) return false;
        currentUser = (currentUser == 1) ? 2 : 1;
        turnEndsAt = now + turnDurationMillis;
        return true;
    }

    public synchronized void resetTurnDeadline(long turnMillis) {
        turnDurationMillis = turnMillis;
        turnEndsAt = System.currentTimeMillis() + turnMillis;
    }

    public synchronized String buildSnapshotJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"gameId\":\"").append(escape(gameId)).append("\",");
        sb.append("\"players\":{");
        sb.append("\"p1\":\"").append(escape(user1)).append("\",");
        sb.append("\"p2\":\"").append(escape(user2)).append("\"},");
        sb.append("\"currentPlayer\":").append(currentUser).append(",");
        sb.append("\"turnEndsAt\":").append(turnEndsAt).append(",");
        sb.append("\"winner\":").append(winner).append(",");
        sb.append("\"gameOver\":").append(gameOver).append(",");
        sb.append("\"board\":[");
        for (int r = ROWS - 1; r >= 0; r--) {
            sb.append("[");
            for (int c = 0; c < COLS; c++) {
                sb.append(gameBoard[c][r]);
                if (c < COLS - 1) sb.append(",");
            }
            sb.append("]");
            if (r > 0) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    public void endGame(int winner, boolean rankedMatch) {
        if (!rankedMatch) return;
        try {
            dao.UserDAO userDAO = new dao.UserDAO();
            dao.MatchDAO matchDAO = new dao.MatchDAO();
            model.User u1 = userDAO.getUserByUsername(user1);
            model.User u2 = userDAO.getUserByUsername(user2);
            if (u1 != null && u2 != null) {
                double actual1 = (winner == 1) ? 1.0 : (winner == 2) ? 0.0 : 0.5;
                double actual2 = (winner == 2) ? 1.0 : (winner == 1) ? 0.0 : 0.5;
                double expected1 = 1.0 / (1.0 + Math.pow(10.0, (u2.getEloRating() - u1.getEloRating()) / 400.0));
                double expected2 = 1.0 / (1.0 + Math.pow(10.0, (u1.getEloRating() - u2.getEloRating()) / 400.0));
                int kFactor = 32;
                int newElo1 = (int) Math.round(u1.getEloRating() + kFactor * (actual1 - expected1));
                int newElo2 = (int) Math.round(u2.getEloRating() + kFactor * (actual2 - expected2));
                userDAO.updateElo(u1.getUserId(), newElo1);
                userDAO.updateElo(u2.getUserId(), newElo2);
                String outcome = (winner == 1) ? "P1_WIN" : (winner == 2) ? "P2_WIN" : "DRAW";
                matchDAO.saveMatchResult(u1.getUserId(), u2.getUserId(), outcome);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getGameId() {
        return gameId;
    }

    public int getCurrentUser() {
        return currentUser;
    }

    public long getTurnEndsAt() {
        return turnEndsAt;
    }

    public String getUser1() {
        return user1;
    }

    public String getUser2() {
        return user2;
    }

    public synchronized java.util.List<java.util.List<Integer>> getBoard() {
        java.util.List<java.util.List<Integer>> board = new java.util.ArrayList<>();
        for (int row = ROWS - 1; row >= 0; row--) {
            java.util.List<Integer> rowValues = new java.util.ArrayList<>();
            for (int col = 0; col < COLS; col++) {
                rowValues.add(gameBoard[col][row]);
            }
            board.add(java.util.Collections.unmodifiableList(rowValues));
        }
        return java.util.Collections.unmodifiableList(board);
    }

    public synchronized java.util.List<Integer> getValidMoves() {
        java.util.List<Integer> moves = new java.util.ArrayList<>();
        for (int col = 0; col < COLS; col++) {
            if (gameBoard[col][ROWS - 1] == 0) {
                moves.add(col);
            }
        }
        return moves;
    }

    public boolean isRanked() {
        return ranked;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getWinner() {
        return winner;
    }

    public String getOpponent(String username) {
        if (user1.equals(username)) return user2;
        if (user2.equals(username)) return user1;
        return null;
    }

    public int getPlayerNumber(String username) {
        if (user1.equals(username)) return 1;
        if (user2.equals(username)) return 2;
        return 0;
    }

    private int getDropRow(int col) {
        for (int row = 0; row < ROWS; row++) {
            if (gameBoard[col][row] == 0) return row;
        }
        return -1;
    }

    private boolean checkFull() {
        for (int col = 0; col < COLS; col++) {
            if (gameBoard[col][ROWS - 1] == 0) return false;
        }
        return true;
    }

    private boolean checkWin(int player) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS - 3; col++) {
                if (gameBoard[col][row] == player && gameBoard[col + 1][row] == player && gameBoard[col + 2][row] == player && gameBoard[col + 3][row] == player) {
                    return true;
                }
            }
        }
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS - 3; row++) {
                if (gameBoard[col][row] == player && gameBoard[col][row + 1] == player && gameBoard[col][row + 2] == player && gameBoard[col][row + 3] == player) {
                    return true;
                }
            }
        }
        for (int col = 0; col < COLS - 3; col++) {
            for (int row = 0; row < ROWS - 3; row++) {
                if (gameBoard[col][row] == player && gameBoard[col + 1][row + 1] == player && gameBoard[col + 2][row + 2] == player && gameBoard[col + 3][row + 3] == player) {
                    return true;
                }
            }
        }
        for (int col = 0; col < COLS - 3; col++) {
            for (int row = 3; row < ROWS; row++) {
                if (gameBoard[col][row] == player && gameBoard[col + 1][row - 1] == player && gameBoard[col + 2][row - 2] == player && gameBoard[col + 3][row - 3] == player) {
                    return true;
                }
            }
        }
        return false;
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record MoveResult(boolean accepted, String reason) {
    }
}

