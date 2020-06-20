package ai;

import java.util.List;
import java.util.Random;

import checkers.Board;
import checkers.Board.*;
import checkers.Move;
import checkers.Piece;
import checkers.Square;

public class AlphaBetaAI implements Agent {

    public enum StaticEval {
        PIECEVALUE, PIECEVALUE_AND_ENDING;
    }

    private int depth;
    private int timeLimitNano;
    private boolean timed;
    private StaticEval evalType;
    private Random random;

    public AlphaBetaAI(int depth, int timeLimitSeconds, boolean timed, StaticEval evalType) {
        this.depth = depth;
        this.timeLimitNano = timeLimitSeconds*1000000000;
        this.timed = timed;
        this.evalType = evalType;
        this.random = new Random();
    }

    @Override
    public Move chooseMove(Board board) {
        Player who = board.getTurn();
        List<Move> legalMoves = board.getAllLegalMoves(board.getTurn());

        long timePerMove = timeLimitNano / legalMoves.size();

        Move bestMove = null;
        if (who == Player.RED) {
            int maxEval = Integer.MIN_VALUE;
            for (Move m : legalMoves) {
                Board future = board.copy();
                future.applyMove(future.transferMove(m));
                int eval;
                if (timed) {
                    eval = timedAlphaBeta(future, Player.WHITE, timePerMove);
                } else {
                    eval = alphaBeta(future, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, Player.WHITE);
                }
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
                int eval;
                if (timed) {
                    eval = timedAlphaBeta(future, Player.RED, timePerMove);
                } else {
                    eval = alphaBeta(future, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, Player.RED);
                }
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
    private int pieceValueStaticEval(Board board) {
        int eval = 0;
        if (board.isOver()) {
            Player winner = board.findWinner();
            if (winner == Player.RED) {
                return 1000;
            }  else if (winner == Player.WHITE) {
                return -1000;
            } else {
                return 0;
            }
        }
        for (Square s : board.getPieces(Player.RED)) {
            if (s.getContents().getType() == Piece.Type.MAN) {
                if (s.getY() < board.getSize() / 2) {
                    eval += 7;
                } else {
                    eval += 5;
                }
            } else {
                eval += 10;
            }
        }
        for (Square s : board.getPieces(Player.WHITE)) {
            if (s.getContents().getType() == Piece.Type.MAN) {
                if (s.getY() > board.getSize() / 2) {
                    eval -= 7;
                } else {
                    eval -= 5;
                }
            } else {
                eval -= 10;
            }
        }
        return eval + random.nextInt(10) - 5;
    }

    private int pieceEvalWithEndgame(Board board) {
        int eval = 0;
        if (board.isOver()) {
            Player winner = board.findWinner();
            if (winner == Player.RED) {
                return 1000;
            } else if (winner == Player.WHITE) {
                return -1000;
            } else {
                return 0;
            }
        } else if (board.inEnding()) {
            List<Square> reds = board.getPieces(Player.RED);
            List<Square> whites = board.getPieces(Player.WHITE);
            int pieceDifference = reds.size() - whites.size();
            // Red has more pieces and needs to get closer to white.
            if (pieceDifference > 0) {
                for (Square s : reds) {
                    for (Square t : whites) {
                        eval -= s.distanceTo(t);
                    }
                }
            // White has more pieces and distance to red is penalized.
            } else {
                for (Square s : reds) {
                    for (Square t : whites) {
                        eval += s.distanceTo(t);
                    }
                }
            }
        }
        return pieceValueStaticEval(board);
    }

    private int alphaBeta(Board board, int depth, int alpha, int beta, Player player) {
        if (depth == 0 || board.isOver()) {
            if (evalType == StaticEval.PIECEVALUE) {
                return pieceValueStaticEval(board);
            } else {
                return pieceEvalWithEndgame(board);
            }
        }

        List<Move> legalMoves = board.getAllLegalMoves(player);
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
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    // TODO: Implement Zobrist hashing to avoid re-computing positions.
    private int timedAlphaBeta(Board board, Player player, long timeLimitNano) {
        long start = System.nanoTime();
        long end = start + timeLimitNano;
        int result = 0;
        int depth = 0;
        while(System.nanoTime() < end) {
            result = alphaBeta(board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, player);
            depth++;
        }
        return result;
    }

    public int getDepth() {
        return depth;
    }
    public StaticEval getEvalType() {
        return evalType;
    }
}
