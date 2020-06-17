package checkers;

import java.util.ArrayList;
import java.util.List;

public class Man extends Piece {

    public Man(Board board, Square startingSquare, Board.Player player) {
        super(board, startingSquare, player);
    }

    @Override
    public Piece copy() {
        return null;
    }

}
