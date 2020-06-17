package checkers;

public interface Piece {

    Move[] getLegalMoves();
    int[] getLocation();

}
