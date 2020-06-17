package checkers;

import java.util.HashMap;

public class Board {

    public enum Player {
        RED, WHITE;
    }

    public enum AgentType {
        HUMAN, BOT;
    }

    private int size;
    private Piece[][] board;
    private Player turn; // The player who needs to go next.
    private HashMap<Player, AgentType> identities;

    public Board(int size) {
        this.size = size;

        // Populate the empty board. In general, we'll use the normal
        // pattern and leave the middle two rows empty, which agrees
        // with the standard 8x8 and 10x10 versions.
        this.board = new Piece[size][size];


        this.turn = Player.RED;
        this.identities = new HashMap<Player, AgentType>();
    }

    private Board(int size, Piece[][] board, Player turn) {
        this.size = size;
        this.board = board;
        this.turn = turn;
    }

    public boolean isOnGrid(int x, int y) {
        return (0 <= x && x < size) && (0 <= y && y < size);
    }

    // TODO; implement
    private boolean isLegalOneStepMove(Piece piece, int x, int y) {
        return false;
    }

    // TODO: implement
    public boolean isLegalMove(Piece piece, int x, int y) {
        return false;
    }

    // TODO: implement
    public void applyMove(int x, int y) {

    }

    // TODO: implement
    public Player findWinner() {
        return null;
    }

    // TODO: implement
    public boolean isOver() {
        return false;
    }

    public int getSize() {
        return size;
    }

    public Player getTurn() {
        return turn;
    }

    public void setPlayer(Player key, AgentType value) {
        identities.put(key, value);
    }

    public AgentType whoHasTheTurn() {
        return identities.get(turn);
    }

    public Board copy() {
        Piece[][] copiedBoard = new Piece[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                copiedBoard[i][j] = board[i][j];
            }
        }
        return new Board(size, copiedBoard, turn);
    }

}
