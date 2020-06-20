package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import checkers.Board;
import checkers.Board.*;
import checkers.Move;
import checkers.Piece;
import checkers.Square;

public class AlphaBetaAI implements Agent {

    public enum StaticEval {
        BASIC_PIECEVALUE, POSITIONAL_PIECEVALUE;
    }

    private int depth;
    private long timeLimitNano;
    private boolean timed;
    private StaticEval evalType;
    private Random random;

    public AlphaBetaAI(int strength, boolean timed, StaticEval evalType) {
        this.depth = strength;
        if (timed) {
            this.timeLimitNano = (long) strength*1000000000;
        } else {
            this.timeLimitNano = Long.MAX_VALUE;
        }
        this.timed = timed;
        this.evalType = evalType;
        this.random = new Random();
    }

    @Override
    public Move chooseMove(Board board) {
        long start = System.nanoTime();
        Player who = board.getTurn();
        List<Move> legalMoves = board.getAllLegalMoves(board.getTurn());

        long timePerMove = timeLimitNano / ((long) legalMoves.size());

        Move bestMove = null;
        if (who == Player.RED) {
            int maxEval = Integer.MIN_VALUE;
            for (Move m : legalMoves) {
                Board future = board.copy();
                future.applyMove(future.transferMove(m));
                int eval;
                if (timed) {
                    // System.out.println("Total time " + timeLimitNano + " Time per move: " + timePerMove);
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
    // TODO: Add more static evaluation options.
    private int basicPieceValueStaticEval(Board board) {
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
                eval += 5;
            } else {
                eval += 10;
            }
        }
        for (Square s : board.getPieces(Player.WHITE)) {
            if (s.getContents().getType() == Piece.Type.MAN) {
                eval -= 5;
            } else {
                eval -= 10;
            }
        }
        return eval + random.nextInt(10) - 5;
    }

    private int positionalPieceValueStaticEval(Board board) {
        int redWeight, whiteWeight;
        if (board.getTurn() == Player.RED) {
            redWeight = 0;
            whiteWeight = 10;
        } else {
            redWeight = 10;
            whiteWeight = -10;
        }
        int eval = 0;
        if (board.isOver()) {
            Player winner = board.findWinner();
            if (winner == Player.RED) {
                return 10000;
            }  else if (winner == Player.WHITE) {
                return -10000;
            } else {
                return 0;
            }
        }
        for (Square s : board.getPieces(Player.RED)) {
            if (s.getContents().getType() == Piece.Type.MAN) {
                if (s.getY() < board.getSize() / 2) {
                    eval += 80 + redWeight;
                } else if (s.getY() == board.getSize()-1) {
                    eval += 70 + redWeight;
                } else {
                    eval += 50 + redWeight;
                }
            } else {
                eval += 120 + redWeight;
            }
            eval -= (Math.abs(s.getX() - board.getSize()/2)*3);
        }
        for (Square s : board.getPieces(Player.WHITE)) {
            if (s.getContents().getType() == Piece.Type.MAN) {
                if (s.getY() > board.getSize() / 2) {
                    eval -= 80 - whiteWeight;
                } else if (s.getY() == 0) {
                    eval -= 70 - whiteWeight;
                } else {
                    eval -= 50 - whiteWeight;
                }
            } else {
                eval -= 120 - whiteWeight;
            }
            eval -= (Math.abs(s.getX() - board.getSize()/2));
        }
        return eval + random.nextInt(10) - 5;
    }


    private int alphaBeta(Board board, int depth, int alpha, int beta, Player player) {
        if (depth == 0 || board.isOver()) {
            if (evalType == StaticEval.BASIC_PIECEVALUE) {
                return basicPieceValueStaticEval(board);
            } else {
                return positionalPieceValueStaticEval(board);
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
    // TODO: Make the actual time spent thinking closer to the given time limit.
    private int timedAlphaBeta(Board board, Player player, long timeLimitNano) {
        long start = System.nanoTime();
        long end = start + timeLimitNano;
        int depth = 0;
        int eval = 0;
        while(System.nanoTime() < end) {
            eval = alphaBeta(board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, player);
            depth++;
        }
        // Return the second-to-last result found, because the last computation
        // will have been cut short.
        return eval;
    }

    public int getDepth() {
        return depth;
    }
    public StaticEval getEvalType() {
        return evalType;
    }
}
