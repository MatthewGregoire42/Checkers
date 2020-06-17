package ai;

import java.util.ArrayList;
import java.util.Random;

import checkers.Board;

public class RandomAI implements Agent {

    private Random random;

    public RandomAI() {
        random = new Random();
    }

    @Override
    public int[] chooseMove(Board board) {
        int size = board.getSize();
        ArrayList<int[]> legal_moves = new ArrayList<int[]>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board.isLegalMove(i, j)) {
                    int[] move = {i, j};
                    legal_moves.add(move);
                }
            }
        }

        int[] move = legal_moves.get(random.nextInt(legal_moves.size()));
        return move;
    }

}
