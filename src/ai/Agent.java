package ai;

import checkers.Board;
import checkers.Move;

public interface Agent {
    Move chooseMove(Board board);
}
