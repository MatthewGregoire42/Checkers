package checkers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Board {

    public enum Player {
        RED, WHITE;
    }

    public enum AgentType {
        HUMAN, BOT;
    }

    private int size;
    private Square[][] board;
    private Player turn; // The player who needs to go next.
    private HashMap<Player, AgentType> identities;
    private List<Square> redPieceSquares;
    private List<Square> whitePieceSquares;

    public Board(int size) {
        if ((size % 2) != 0) {
            throw new IllegalArgumentException("Board must have even size.");
        }
        this.size = size;

        // Populate the board with an array of empty squares.
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int[] coord = {j, i};
                board[i][j] = new Square(null, j, i);
            }
        }

        // Put white pieces into the top half of the board.
        int tracker = 0;
        for (int i = 0; i < size/2 - 1; i++) {
            for (int j = 0; j < size; j++) {
                if (tracker % 2 == 1) {
                    int[] coord = {j, i};
                    Piece man = new Piece(Piece.Type.MAN, Player.WHITE);
                    board[i][j].setContents(man);
                    whitePieceSquares.add(board[i][j]);
                }
                tracker++;
            }
        }

        // Put red pieces into the lower half of the board.
        if ((size/2) % 2 == 0) {
            tracker = 0;
        } else {
            tracker = 1;
        }
        for (int i = size/2 + 1; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (tracker % 2 == 1) {
                    int[] coord = {j, i};
                    Piece man = new Piece(Piece.Type.MAN, Player.RED);
                    board[i][j].setContents(man);
                    redPieceSquares.add(board[i][j]);
                }
                tracker++;
            }
        }
    }

    // Internal constructor used only for copying the board.
    private Board(Square[][] board, Player turn, HashMap<Player, AgentType> identities,
                  List<Square> redPieceSquares, List<Square> whitePieceSquares) {
        this.size = board.length;
        this.board = board;
        this.turn = turn;
        this.identities = identities;
        this.redPieceSquares = redPieceSquares;
        this.whitePieceSquares = whitePieceSquares;
    }

    public boolean isOnGrid(int x, int y) {
        return (0 <= x && x < size) && (0 <= y && y < size);
    }

    public Square getSquare(int x, int y) {
        if (isOnGrid(x, y)) {
            return board[y][x];
        } else {
            return null;
        }
    }

    public List<Square> getNeighbors(Square square) {
        List<Square> out = new ArrayList<>();

        int x = square.getX();
        int y = square.getY();
        int[] xCoords = {x-1, x+1, x-1, x+1};
        int[] yCoords = {y-1, y-1, y+1, y+1};
        for (int i = 0; i < 4; i++) {
            Square neighbor = getSquare(xCoords[i], yCoords[i]);
            if (neighbor != null) {
                out.add(neighbor);
            }
        }
        return out;
    }

    // TODO: implement
    public List<Move> getLegalMovesFor(Square square) {
        if (square.getContents() == null) {
            return null;
        }

        Piece piece = square.getContents();
        List<Move> legalMoves = new ArrayList<Move>();

        // Find all legal, non-capture moves
        List<Square> neighbors = getNeighbors(square);
        for (Square n: neighbors) {
            // TODO: update to add movement restrictions
            if (n.getContents() == null) {
                legalMoves.add(new Move(square, n));
            }
        }
        legalMoves.addAll(getLegalCaptureMovesFor(square));

        return legalMoves;
    }

    private List<Move> getLegalCaptureMovesFor(Square square) {
        if (square.getContents() == null) {
            return null;
        }

        Piece piece = square.getContents();
        List<Move> legalCaptureMoves = new ArrayList<>();

        List<Square> neighbors = getNeighbors(square);
        for (Square n: neighbors) {
            Piece nPiece = n.getContents();
            int dx = n.getX() - square.getX();
            int dy = n.getY() - square.getY();
            Square destination = getSquare(square.getX() + 2 * dx, square.getY() + 2 * dy);
            if (destination != null) {
                boolean isMan = piece.getType() == Piece.Type.MAN;
                boolean correctDirectionRed = piece.getPlayer() == Player.RED &&
                        square.getY() - n.getY() < 0;
                boolean correctDirectionWhite = piece.getPlayer() == Player.WHITE &&
                        square.getY() - n.getY() > 0;
                boolean isKing = piece.getType() == Piece.Type.KING;
                boolean correctColor =
                        (piece.getPlayer() == Player.RED && nPiece.getPlayer() == Player.WHITE) ||
                                (piece.getPlayer() == Player.WHITE && nPiece.getPlayer() == Player.RED);
                if (((isMan && (correctDirectionRed || correctDirectionWhite)) || isKing) && correctColor) {
                    // Add the capture to legal moves.
                    List<Square> captures = new ArrayList<>();
                    captures.add(n);
                    Move m = new Move(square, destination, captures);
                    legalCaptureMoves.add(m);

                    // Look for chains of captures.
                    Board possible = this.copy();
                    possible.applyMove(m);

                    List<Move> futures = possible.getLegalCaptureMovesFor(destination);
                    legalCaptureMoves.addAll(m.followedByAll(futures));

                }
            }
        }
        return legalCaptureMoves;
    }

    public boolean isLegalMove(Move m) {
        return getLegalMovesFor(m.getOrigin()).contains(m);
    }

    public void applyMove(Move m) {
        Square source = m.getOrigin();
        Square dest = m.getDestination();
        List<Square> captures = m.getCaptures();

        dest.setContents(source.getContents());
        source.setContents(null);
        for (Square c : captures) {
            if (c.getContents().getPlayer() == Player.WHITE) {
                whitePieceSquares.remove(c);
            } else {
                redPieceSquares.remove(c);
            }
        }

        if (turn == Player.RED) {
            turn = Player.WHITE;
        } else {
            turn = Player.RED;
        }
    }

    public Board copy() {
        Square[][] copiedBoard = new Square[size][size];
        List<Square> copiedRedPieceSquares = new ArrayList<>();
        List<Square> copiedWhitePieceSquares = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Square copiedSquare = board[i][j].copy();
                copiedBoard[i][j] = copiedSquare;

                if (copiedSquare.getContents() != null) {
                    if (copiedSquare.getContents().getPlayer() == Player.RED) {
                        copiedRedPieceSquares.add(copiedSquare);
                    } else {
                        copiedWhitePieceSquares.add(copiedSquare);
                    }
                }
            }
        }

        HashMap<Player, AgentType> copiedIdentities = new HashMap<>();
        copiedIdentities.put(Player.RED, identities.get(Player.RED));
        copiedIdentities.put(Player.WHITE, identities.get(Player.WHITE));

        return new Board(copiedBoard, turn, copiedIdentities, copiedRedPieceSquares, copiedWhitePieceSquares);
    }

}
