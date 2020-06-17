package checkers;

// Represents moving the given piece to the given coordinate.
// The class is immutable.
public class Move {

    private Piece piece;
    private int[] location;

    public Move(Piece piece, int[] location) {
        this.piece = piece;
        this.location = location;
    }

    public Piece getPiece() {
        return piece;
    }

    public int[] getLocation() {
        return location;
    }
}
