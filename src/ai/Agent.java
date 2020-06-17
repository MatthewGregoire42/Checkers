package ai;

import checkers.Board;

public interface Agent {
    int[] chooseMove(Board board);
}
