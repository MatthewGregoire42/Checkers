package checkers;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece {

    protected Board board;
    protected Board.Player color;
    protected Square location;
    protected Square[] neighbors;

    public Piece(Board board, Square startingLocation, Board.Player color) {
        this.board = board;
        this.location = startingLocation;
        this.color = color;
        this.neighbors = new Square[4];
        updateNeighbors();
    }

    public List<Square> getOpenNeighbors() {
        List<Square> out = new ArrayList<>();
        for (Square n : neighbors) {
            if (n != null) {
                if (n.getContents() == null) {
                    out.add(n);
                }
            }
        }
        return out;
    }

    private void updateNeighbors() {
        int x = location.getX();
        int y = location.getY();
        int[] xCoords = {x-1, x+1, x-1, x+1};
        int[] yCoords = {y-1, y-1, y+1, y+1};
        for (int i = 0; i < 4; i++) {
            neighbors[i] = board.getSquare(xCoords[i], yCoords[i]);
        }
    }

    public Square[] getNeighbors() {
        return neighbors;
    }

    public Square getLocation() {
        return location;
    }

    public abstract Piece copy();

}
