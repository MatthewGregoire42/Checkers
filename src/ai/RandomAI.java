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
        return new int[0];
    }

}
