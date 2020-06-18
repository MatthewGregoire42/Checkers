package ai;

import java.util.ArrayList;
import java.util.List;

import checkers.Board;
import checkers.Board.*;
import checkers.Move;
import checkers.Piece;
import checkers.Square;

public class AlphaBetaAI implements Agent {

    int depth;

    public AlphaBetaAI(int depth) {
        this.depth = depth;
    }

    @Override
    public Move chooseMove(Board board) {
        Player who = board.getTurn();
        List<Square> squares = board.getPieces(who);
        List<Move> legalMoves = new ArrayList<>();
        for (Square s : squares) {
            legalMoves.addAll(board.getLegalMovesFor(s, who, s.getContents().getType(), null));
        }

        Move bestMove = null;
        if (who == Player.RED) {
            int maxEval = Integer.MIN_VALUE;
            for (Move m : legalMoves) {
                Board future = board.copy();
                future.applyMove(future.transferMove(m));
                int eval = alphaBeta(future, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, Player.WHITE);
                if (eval > maxEval) {
                    maxEval = eval;
                    bestMove = m;
                }
            }
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move m : legalMoves) {
                Board future = board.copy();
                future.applyMove(future.transferMove(m));
                int eval = alphaBeta(future, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, Player.RED);
                if (eval < minEval) {
                    minEval = eval;
                    bestMove = m;
                }
            }
        }
        return bestMove;
    }

    // Red is the maximizing player, and white
    // is the minimizing player.
    private int staticEval(Board board) {
        int eval = 0;
        for (Square s : board.getPieces(Player.RED)) {
            if (s.getContents().getType() == Piece.Type.MAN) {
                eval += 1;
            } else {
                eval -= 2;
            }
        }
        for (Square s : board.getPieces(Player.WHITE)) {
            if (s.getContents().getType() == Piece.Type.MAN) {
                eval -= 1;
            } else {
                eval -= 2;
            }
        }
        return eval;
    }

    private int alphaBeta(Board board, int depth, int alpha, int beta, Player player) {
        if (depth == 0 || board.isOver()) {
            return staticEval(board);
        }

        List<Move> legalMoves = new ArrayList<>();
        for (Square s : board.getPieces(player)) {
            legalMoves.addAll(board.getLegalMovesFor(s, board.getTurn(), s.getContents().getType(), null));
        }
        if (player == Player.RED) {
            int maxEval = Integer.MIN_VALUE;
            for (Move m : legalMoves) {
                Board future = board.copy();
                future.applyMove(future.transferMove(m));
                int eval = alphaBeta(future, depth-1, alpha, beta, Player.WHITE);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move m : legalMoves) {
                Board future = board.copy();
                future.applyMove(future.transferMove(m));
                int eval = alphaBeta(future, depth-1, alpha, beta, Player.RED);
                minEval = Math.min(minEval, eval);
                beta = Math.min(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }
}
