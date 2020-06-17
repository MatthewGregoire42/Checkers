package checkers;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    public King(Board board, Square startingLocation, Board.Player player) {
        super(board, startingLocation, player);
    }

    // TODO: implement
    @Override
    public List<Move> getLegalMoves() {
        return new ArrayList<>();
    }

    @Override
    public Piece copy() {
        return null;
    }

}