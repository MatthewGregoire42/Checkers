package checkers;

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
        Square copied = new Square(contents.copy(), x, y);
        if (copied.contents != null) {
            copied.contents.location = copied;
        }
        return copied;
    }
}
