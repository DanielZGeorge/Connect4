package model;

import Game_Engine.GameSession;
import java.util.List;

/**
 * HardBot — uses the Negamax algorithm with Alpha-Beta Pruning.
 *
 * -------------------------------------------------------------------------
 * CONCEPT 
 * -------------------------------------------------------------------------
 *
 * Negamax is a smarter way to think about Minimax. Instead of switching
 * between "maximize for bot" and "minimize for opponent", EVERY player
 * tries to maximize THEIR OWN score — and the opponent's score is just
 * the negative of yours.
 *
 * Alpha-Beta Pruning speeds this up. As the algorithm explores the tree:
 *   - alpha = the best score the current player is GUARANTEED so far
 *   - beta  = the best score the OPPONENT is guaranteed so far
 *
 * If we find a branch where beta <= alpha, we STOP searching it ("prune").
 * Why? Because the opponent would never let us reach a state that's
 * better for us than what they can already guarantee for themselves.
 *
 * Search depth (MAX_DEPTH): how many moves ahead the bot looks.
 *   Depth 7 is very strong. Depth 4–5 is fast and still tough to beat.
 *
 * Algorithm credit: Pascal Pons (https://blog.gamesolver.org)
 * -------------------------------------------------------------------------
 */


public class HardBot extends BotPlayer {

    private static final int MAX_DEPTH = 7;   // how many moves ahead to look
    private static final int WIN_SCORE = 1000; // score for a winning position
    private static final int COLS = 7;
    private static final int ROWS = 6;

    // Column order to try — center columns first (usually stronger moves)
    private static final int[] COLUMN_ORDER = {3, 2, 4, 1, 5, 0, 6};

    public HardBot(int playerNumber) {
        super("Hard Bot", playerNumber);
    }

//For each valid col, run negamax, return highest score
    @Override
    public int chooseMove(GameSession game) {
        int[][] board = game.getRawBoard();
        List<Integer> validMoves = game.getValidMoves();

        int bestScore = Integer.MIN_VALUE;
        int bestCol = validMoves.get(0);

        // Alpha-beta window: start fully open
        int alpha = Integer.MIN_VALUE;
        int beta  = Integer.MAX_VALUE;

        // Try columns in the preferred order
        for (int col : COLUMN_ORDER) {
            if (!validMoves.contains(col)) continue; // skip invalid columns

            // Simulate this move
            int[][] newBoard = simulateMove(board, col, playerNumber);
            if (newBoard == null) continue;

            // Score this move using negamax (opponent's turn next, so negate)
            int score = -negamax(newBoard, getOpponentNumber(), MAX_DEPTH - 1, -beta, -alpha);

            System.out.println("HardBot: col " + col + " scored " + score);

            if (score > bestScore) {
                bestScore = score;
                bestCol = col;
            }

            alpha = Math.max(alpha, score);
        }

        System.out.println("HardBot chose column: " + bestCol);
        return bestCol;
    }

//Negamax with Alpha-Beta Pruning.
    private int negamax(int[][] board, int player, int depth, int alpha, int beta) {

        //  Did the PREVIOUS move win the game? 
        // We check for a winner BEFORE switching players
        if (hasWinner(board)) {
            // The opponent won — this is bad for `player`, so return a negative score.
            // We add depth so that FASTER wins are scored higher than slower ones.
            return -(WIN_SCORE + depth);
        }

        // Board is full (draw)
        if (isFull(board)) {
            return 0;
        }

        // Reached the search depth limit 
        if (depth == 0) {
            return evaluate(board, player); // use heuristic to estimate the position
        }

        int bestScore = Integer.MIN_VALUE;
        int opponent = (player == 1) ? 2 : 1;

        // Try each column in the preferred order
        for (int col : COLUMN_ORDER) {
            // Skip full columns
            if (board[col][ROWS - 1] != 0) continue;

            // Simulate placing `player`'s piece in this column
            int[][] newBoard = simulateMove(board, col, player);
            if (newBoard == null) continue;

            // Recurse: now it's the opponent's turn (negate because negamax)
            int score = -negamax(newBoard, opponent, depth - 1, -beta, -alpha);

            bestScore = Math.max(bestScore, score);
            alpha = Math.max(alpha, score);

            // Alpha-Beta pruning: stop searching this branch
            if (alpha >= beta) {
                break; // "prune" — opponent wouldn't allow this anyway
            }
        }

        return bestScore;
    }

    /**
     * Heuristic evaluation when we hit the depth limit.
     * Scores the board based on how favorable it looks for `player`.
     *
     * The current implementation counts how many 3-in-a-rows and 2-in-a-rows
     * exist for each side. You can improve this over time.
     *
     * Positive score = good for `player`, negative = bad for `player`.
     */
    private int evaluate(int[][] board, int player) {
        int opponent = (player == 1) ? 2 : 1;
        return scoreForPlayer(board, player) - scoreForPlayer(board, opponent);
    }

    /**
     * Counts "potential" chains for a player (2-in-a-rows and 3-in-a-rows).
     * A chain of 3 is worth more than a chain of 2.
     */
    private int scoreForPlayer(int[][] board, int player) {
        int[][] directions = {{1,0},{0,1},{1,1},{1,-1}};
        int score = 0;

        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {
                if (board[col][row] != player) continue;

                for (int[] dir : directions) {
                    int count = 0;
                    boolean blocked = false;

                    for (int i = 0; i < 4; i++) {
                        int c = col + i * dir[0];
                        int r = row + i * dir[1];

                        if (c < 0 || c >= COLS || r < 0 || r >= ROWS) { blocked = true; break; }

                        int cell = board[c][r];
                        if (cell == player)   count++;
                        else if (cell != 0)   { blocked = true; break; } // opponent piece blocks
                    }

                    if (!blocked) {
                        // Weight longer chains exponentially more
                        if (count == 3) score += 10;
                        else if (count == 2) score += 3;
                        else if (count == 1) score += 1;
                    }
                }
            }
        }

        return score;
    }

    /**
     * Checks if the board is completely full.
     */
    private boolean isFull(int[][] board) {
        for (int col = 0; col < COLS; col++) {
            if (board[col][ROWS - 1] == 0) return false;
        }
        return true;
    }
}