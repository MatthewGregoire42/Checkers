package checkers;

import java.util.ArrayList;
import java.util.List;

public class Move {

    private Piece piece;
    private List<Square> destinations;

    public Move(Piece piece, List<Square> destinations) {
        this.piece = piece;
        this.destinations = destinations;
    }

    public Move(Piece piece, Square destination) {
        this.piece = piece;
        ArrayList<Square> dest = new ArrayList<>();
        dest.add(destination);
        this.destinations = dest;
    }

    public boolean isCapture() {
        return (destinations.size() > 1 ||
                Math.abs(piece.location.getY() - destinations.get(0).getY()) > 1);
    }

    public Piece getPiece() {
        return piece;
    }

    public List<Square> getDestinations() {
        return destinations;
    }
}
