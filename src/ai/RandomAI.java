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
        List<Move> legalMoves = board.getAllLegalMoves(board.getTurn());
        if (legalMoves.size() > 1) {
            return legalMoves.get(random.nextInt(legalMoves.size()-1));
        } else {
            return legalMoves.get(0);
        }
    }

}
