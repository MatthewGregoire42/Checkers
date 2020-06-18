package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import checkers.Board;
import checkers.Move;
import checkers.Square;

public class RandomAI implements Agent {

    private Random random;

    public RandomAI() {
        random = new Random();
    }

    @Override
    public Move chooseMove(Board board) {
        List<Square> squares = board.getPieces(board.getTurn());
        List<Move> legalMoves = new ArrayList<>();
        for (Square s : squares) {
            legalMoves.addAll(board.getLegalMovesFor(s, board.getTurn(), s.getContents().getType(), null));
        }
        return legalMoves.get(random.nextInt(legalMoves.size()-1));
    }

}
