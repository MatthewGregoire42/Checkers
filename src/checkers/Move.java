package checkers;

import java.util.ArrayList;
import java.util.List;

/**
 * The Move class represents a (not yet executed) move on
 * the board. All of the relevant data is positional, so
 * Moves operate on Squares instead of Pieces.
 */

public class Move {

    private Square origin;
    private Square destination;
    private List<Square> captures;

    public Move(Square origin, Square destination) {
        this.origin = origin;
        this.destination = destination;
        this.captures = new ArrayList<>();
    }

    public Move(Square origin, Square destination, List<Square> captures) {
        this.origin = origin;
        this.destination = destination;
        this.captures = captures;
    }

    public Move(Square origin, Square destination, Square capture) {
        this.origin = origin;
        this.destination = destination;
        this.captures = new ArrayList<>();
        captures.add(capture);
    }

    public Move followedBy(Move next) {
        if (!this.destination.equals(next.origin)) {
            throw new IllegalArgumentException("Unallowed sequence of moves");
        }
        if (next == null) {
            return this;
        }
        List<Square> allCaptures = new ArrayList<>();
        allCaptures.addAll(this.getCaptures());
        allCaptures.addAll(next.getCaptures());

        return new Move(this.origin, next.destination, allCaptures);
    }

    public List<Move> followedByAll(List<Move> futures) {
        List<Move> moves = new ArrayList<>();
        for (Move m : futures) {
            moves.add(this.followedBy(m));
        }
        return moves;
    }

    public boolean isCapture() {
        return captures.size() > 0;
    }

    public Piece getPiece() {
        return origin.getContents();
    }

    public Square getOrigin() {
        return origin;
    }

    public Square getDestination() {
        return destination;
    }

    public List<Square> getCaptures() {
        return captures;
    }
}
