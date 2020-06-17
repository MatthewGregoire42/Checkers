package checkers;

/**
 * The only information a Piece object contains is
 * 1. which type of piece it is, and
 * 2. which player it belongs to.
 * All logic that explicitly operates on pieces
 * is handled within other classes.
 */

public class Piece {

    public enum Type {
        MAN, KING;
    }

    private Type type;
    private Board.Player player;

    public Piece(Type type, Board.Player player) {
        this.type = type;
        this.player = player;
    }

    public Type getType() {
        return type;
    }

    public Board.Player getPlayer() {
        return player;
    }

    public Piece copy() {
        return new Piece(type, player);
    }

}
