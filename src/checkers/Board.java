package checkers;

import java.util.*;

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
    private Map<Player, AgentType> identities;
    private List<Square> redPieceSquares;
    private List<Square> whitePieceSquares;

    public Board(int size) {
        if ((size % 2) != 0) {
            throw new IllegalArgumentException("Board must have even size.");
        }
        this.size = size;
        this.board = new Square[size][size];
        this.whitePieceSquares = new ArrayList<>();
        this.redPieceSquares = new ArrayList<>();
        this.identities = new HashMap<>();
        this.turn = Player.RED;

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
            tracker++;
        }

        // Put red pieces into the lower half of the board.
        if ((size/2) % 2 == 0) {
            tracker = 1;
        } else {
            tracker = 0;
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
            tracker++;
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
        List<Move> legalMoves = new ArrayList<Move>();

        // Find all legal, non-capture moves
        List<Square> neighbors = getNeighbors(square);
        for (Square n: neighbors) {
            Move m = new Move(square, n);
            if (isLegalMove(m)) {
                legalMoves.add(m);
            }
        }
        legalMoves.addAll(getLegalCaptureMovesFor(square));

        return legalMoves;
    }

    private List<Move> getLegalCaptureMovesFor(Square square) {
        if (square.getContents() == null) { return null; }

        List<Move> legalCaptureMoves = new ArrayList<>();
        List<Square> neighbors = getNeighbors(square);

        for (Square n: neighbors) {
            if (n.getContents() != null && n.getContents().getPlayer() != square.getContents().getPlayer()) {
                int dx = n.getX() - square.getX();
                int dy = n.getY() - square.getY();
                Square destination = getSquare(square.getX() + 2 * dx, square.getY() + 2 * dy);

                List<Square> captures = new ArrayList<>();
                captures.add(n);
                Move m = new Move(square, destination, captures);
                if (isLegalMove(m)) {

                    // Add the capture to legal moves.
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
        Square source = m.getOrigin();
        if (source == null) {
            return false;
        }

        Square dest = m.getDestination();
        if (dest == null || !isOnGrid(dest.getX(), dest.getY())) {
            return false;
        }

        Piece p = source.getContents();
        if ( p == null) {
            return false;
        }

        boolean correctDirectionRed = p.getPlayer() == Player.RED &&
                source.getY() - dest.getY() > 0;
        boolean correctDirectionWhite = p.getPlayer() == Player.WHITE &&
                source.getY() - dest.getY() < 0;

        // Legal simple move?
        if (!m.isCapture()) {
            if (p.getType() == Piece.Type.KING) {
                return Math.abs(source.getX() - dest.getX()) == 1 &&
                        Math.abs(source.getY() - dest.getY()) == 1;
            } else {
                return correctDirectionRed || correctDirectionWhite;
            }
        }

        // Legal capture move?

        boolean isMan = p.getType() == Piece.Type.MAN;
        boolean isKing = p.getType() == Piece.Type.KING;
        for (Square hopped : m.getCaptures()) {
            Piece capture = hopped.getContents();
            boolean correctColor =
                    (p.getPlayer() == Player.RED && capture.getPlayer() == Player.WHITE) ||
                            (p.getPlayer() == Player.WHITE && capture.getPlayer() == Player.RED);
            if (!(((isMan && (correctDirectionRed || correctDirectionWhite)) || isKing) && correctColor)) {
                return false;
            }
        }
        return true;
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

    public void setPlayer(Player player, AgentType agentType) {
        identities.put(player, agentType);
    }

    public AgentType whoHasTheTurn() {
        return identities.get(turn);
    }

    public int getSize() {
        return size;
    }

    public Player getTurn() {
        return turn;
    }

    public List<Square> getPieces(Player player) {
        if (player == Player.RED) {
            return redPieceSquares;
        } else {
            return whitePieceSquares;
        }
    }

    public Player findWinner() {
        if (redPieceSquares.size() == 0) {
            return Player.WHITE;
        } else if (whitePieceSquares.size() == 0) {
            return Player.RED;
        }
        return null;
    }

    public boolean isOver() {
        if (findWinner() != null) {
            return true;
        }
        if (turn == Player.RED) {
            for (Square s : redPieceSquares) {
                if (getLegalMovesFor(s).size() != 0) {
                    return false;
                }
            }
            return true;
        } else {
            for (Square s : whitePieceSquares) {
                if (getLegalMovesFor(s).size() != 0) {
                    return false;
                }
            }
            return true;
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
