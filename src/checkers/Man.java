package checkers;

public class Man implements Piece {

    private Board board;
    private int[] location;
    private Board.Player color;

    public Man(Board board, int[] startingLocation, Board.Player color) {
        this.board = board;
        this.location = startingLocation;
        this.color = color;
    }

    // TODO: implement
    @Override
    public Move[] getLegalMoves() {
        return new Move[0];
    }

    @Override
    public int[] getLocation() {
        return location;
    }
}
