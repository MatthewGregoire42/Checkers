package checkers;

/**
 * The Square class handles all positional data for each square
 * of the board. Each square either has one piece or null as its
 * contents. If in the Board class, a square is null, it's not
 * on the board.
 */

public class Square {

    private Piece contents;
    private int x;
    private int y;

    public Square(Piece contents, int x, int y) {
        this.contents = contents;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    public Piece getContents() {
        return contents;
    }

    public void setContents(Piece contents) {
        this.contents = contents;
    }

    public Square copy() {
        return new Square(contents.copy(), x, y);
    }
}
