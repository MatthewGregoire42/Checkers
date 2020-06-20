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

    // Assuming that the given player is on the given square, return all
    // legal moves for that particular piece.

    public List<Move> getLegalMovesFor(Square square) {
        return getLegalMovesFor(square, turn, square.getContents().getType(), null);
    }

    private List<Move> getLegalMovesFor(Square square, Player player, Piece.Type type, Move chained) {
        if (square == null || player == null) {
            throw new IllegalArgumentException("Invalid arguments.");
        }
        List<Move> legalMoves = new ArrayList<>();
        // Make sure that this piece is allowed to move.
        if (chained == null && square.getContents().getPlayer() != turn) {
            return legalMoves;
        }

        List<Square> neighbors = getNeighbors(square);
        for (Square n : neighbors) {
            boolean correctDirection = type == Piece.Type.KING || (type == Piece.Type.MAN &&
                    ((player == Player.RED && (n.getY() - square.getY() < 0)) ||
                    player == Player.WHITE && (n.getY() - square.getY() > 0)));
            Piece other = n.getContents();
            if (correctDirection) {
                // Legal simple move?
                if (other == null && chained == null) {
                    legalMoves.add(new Move(square, n));
                // Legal capture move?
                } else if (other != null && other.getPlayer() != player) {
                    int dx = n.getX() - square.getX();
                    int dy = n.getY() - square.getY();
                    Square dest = getSquare(square.getX() + 2*dx, square.getY() + 2*dy);
                    if (dest != null && dest.getContents() == null) {
                        // If this is the first jump in a chain, it's just a normal capture.
                        // Also make sure you're not capturing the same piece twice.
                        if (chained == null || !chained.getCaptures().contains(n)) {
                            Move capture = new Move(square, dest, n);
                            if (chained != null) {
                                capture = chained.followedBy(capture);
                            }
                            legalMoves.add(capture);
                            // Search for chained captures.
                            legalMoves.addAll(getLegalMovesFor(dest, player, type, capture));
                        }
                    }
                }
            }
        }
        return legalMoves;
    }

    public List<Move> getAllLegalMoves(Player player) {
        List<Move> moves = new ArrayList<>();
        for (Square s : getPieces(player)) {
            moves.addAll(getLegalMovesFor(s));
        }
        return moves;
    }

    public void applyMove(Move m) {
        Square source = m.getOrigin();
        Square dest = m.getDestination();
        Piece piece = source.getContents();
        List<Square> captures = m.getCaptures();

        // Move the piece.
        dest.setContents(piece);
        source.setContents(null);
        // Update the piece square lists.
        if (piece.getPlayer() == Player.RED) {
            redPieceSquares.remove(source);
            redPieceSquares.add(dest);
        } else {
            whitePieceSquares.remove(source);
            whitePieceSquares.add(dest);
        }

        // Remove all captures.
        for (Square c : captures) {
            if (c.getContents().getPlayer() == Player.WHITE) {
                whitePieceSquares.remove(c);
            } else {
                redPieceSquares.remove(c);
            }
            c.setContents(null);
        }
        // King pieces if necessary
        boolean kingMe = piece.getType() == Piece.Type.MAN &&
                ((piece.getPlayer() == Player.RED && dest.getY() == 0) ||
                (piece.getPlayer() == Player.WHITE && dest.getY() == size - 1));
        if (kingMe) {
            piece.kingMe();
        }

        // Progress to the next turn.
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
        if (turn == Player.RED) {
            for (Square s : redPieceSquares) {
                if (getLegalMovesFor(s, Player.RED, s.getContents().getType(), null).size() != 0) {
                    return null;
                }
            }
            return Player.WHITE;
        } else {
            for (Square s : whitePieceSquares) {
                if (getLegalMovesFor(s, Player.WHITE, s.getContents().getType(), null).size() != 0) {
                    return null;
                }
            }
            return Player.RED;
        }
    }

    public boolean inEnding() {
        int kingCount = 0;
        for (Square s : redPieceSquares) {
            if (s.getContents().getType() == Piece.Type.KING) {
                kingCount++;
            }
        }
        for (Square s : whitePieceSquares) {
            if (s.getContents().getType() == Piece.Type.KING) {
                kingCount++;
            }
        }
        return kingCount > 6;
    }

    public boolean isOver() {
        return findWinner() != null;
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

    public Move transferMove(Move m) {
        int origin_x = m.getOrigin().getX();
        int origin_y = m.getOrigin().getY();
        Square origin = getSquare(origin_x, origin_y);
        int dest_x = m.getDestination().getX();
        int dest_y = m.getDestination().getY();
        Square destination = getSquare(dest_x, dest_y);
        List<Square> captures = new ArrayList<>();
        for (Square c : m.getCaptures()) {
            int capture_x = c.getX();
            int capture_y = c.getY();
            captures.add(getSquare(capture_x, capture_y));
        }
        return new Move(origin, destination, captures);
    }

    public void printBoard() {
        System.out.println("--------------------");
        for (int i = 0; i < size; i++) {
            String line = "";
            for (int j = 0; j < size; j++) {
                Square s = getSquare(j, i);
                if (s.getContents() == null) {
                    line += "  ";
                } else if (s.getContents().getPlayer() == Player.RED) {
                    if (s.getContents().getType() == Piece.Type.MAN) {
                        line += "XX";
                    } else {
                        line += "XK";
                    }
                } else {
                    if (s.getContents().getType() == Piece.Type.MAN) {
                        line += "OO";
                    } else {
                        line += "OK";
                    }
                }
            }
            System.out.println(line);
        }
        System.out.println("--------------------");
    }

}
