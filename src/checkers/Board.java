package checkers;

import java.util.ArrayList;
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
    private List<Piece> redPieces;
    private List<Piece> whitePieces;

    public Board(int size) {
        if ((size % 2) != 0) {
            throw new IllegalArgumentException("Board must have even size.");
        }
        this.size = size;

        // Populate the board with an array of squares.
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
                    Man man = new Man(this, coord, Player.WHITE);
                    board[i][j].setContents(man);
                    whitePieces.add(man);
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
                    Man man = new Man(this, coord, Player.RED);
                    board[i][j].setContents(man);
                    redPieces.add(man);
                }
                tracker++;
            }
        }
    }

    // Internal constructor used only for copying the board.
    private Board(Square[][] board, Player turn, HashMap<Player, AgentType> identities,
                  List<Piece> redPieces, List<Piece> whitePieces) {
        this.size = board.length;
        this.board = board;
        this.turn = turn;
        this.identities = identities;
        this.redPieces = redPieces;
        this.whitePieces = whitePieces;
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

    public List<Move> getLegalMovesFor(Piece piece) {

        List<Move> legalMoves = new ArrayList<Move>();

        Square[] neighbors = piece.getNeighbors();

        for (Square n : neighbors) {
            if (n != null) {
                if (n.getContents() == null) {
                    if (piece instanceof Man) {
                        Square s = piece.getLocation();
                        if (piece.color == Player.RED && n.getY() - s.getY() > 0) {
                            legalMoves.add(new Move(piece, n));
                        } else if (piece.color == Player.WHITE && n.getY() - s.getY() < 0) {
                            legalMoves.add(new Move(piece, n));
                        }
                    } else {
                        legalMoves.add(new Move(piece, n));
                    }
                } else {

                }
            }
        }

        return legalMoves;
    }

    public Board copy() {
        Square[][] copiedBoard = new Square[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Square square = board[i][j];
                Piece piece = square.getContents();
                Piece copiedPiece;
                if (piece != null) {
                }
            }
        }

        return null;
    }

}
