package gui;

import ai.Agent;
import ai.AlphaBetaAI;
import ai.RandomAI;
import checkers.Move;
import checkers.Piece;
import checkers.Square;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import static gui.Main.X_DIM;
import static gui.Main.Y_DIM;
import checkers.Board;
import checkers.Board.*;

import java.util.List;

public class PlayController {

    @FXML private Canvas canvas;
    private GraphicsContext gc;
    private String botType;
    private AgentType player_red;
    private AgentType player_white;
    private Board gameboard;
    private BotMoveService moveHandler;
    private int clickCount;
    private List<Move> currentLegals;

    // Sets who is playing: HvH, HvB, or BvB, and the human's player.
    // Then starts the game.
    // Called by the StartController to pass in information.
    // The initializer for this class is basically useless, because
    // we need so much information from the Start screen before the
    // Play screen can do anything.
    public void setOptions(AgentType red, AgentType white, int s, String botType) {
        player_red = red;
        player_white = white;
        this.botType = botType;
        clickCount = 0;

        gameboard = new Board(s);
        gameboard.setPlayer(Player.RED, player_red);
        gameboard.setPlayer(Player.WHITE, player_white);

        // This is where we'll draw the game as it progresses
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(5);
        drawBoard();

        Agent bot = new AlphaBetaAI(9);

        // How to handle bot moves.
        moveHandler = new BotMoveService(gameboard, bot);
        moveHandler.setOnSucceeded(e -> {
            Move move = moveHandler.getValue();
            gameboard.applyMove(move);
            drawMove(move, gameboard.getTurn());

            if (gameboard.isOver()) {
                transitionToFinish();
            } else {
                botMove();
            }
        });
        moveHandler.setOnFailed(e -> {
            e.getSource().getException().printStackTrace();
        });

        // How to handle human moves.
        canvas.setOnMouseClicked(e -> {
            if (gameboard.whoHasTheTurn().equals(AgentType.HUMAN)) {
                int x = (int) (e.getX() / (X_DIM/gameboard.getSize()));
                int y = (int) (e.getY() / (Y_DIM/gameboard.getSize()));
                Square square = gameboard.getSquare(x, y);
                // If it's the first click of a move, show the available options.
                if (clickCount % 2 == 0 && square.getContents() != null) {
                    currentLegals = gameboard.getLegalMovesFor(
                            square, gameboard.getTurn(), square.getContents().getType(), null);
                    for (Move m : currentLegals) {
                        Square dest = m.getDestination();
                        drawPotential(dest);
                    }
                    if (currentLegals.size() == 0) {
                        clickCount++;
                    }
                // Otherwise, apply the selected move.
                } else {
                    for (Move m : currentLegals) {
                        Square dest = m.getDestination();
                        drawSquare(dest.getX(), dest.getY());
                        if (dest.getX() == x && dest.getY() == y) {
                            gameboard.applyMove(m);
                            drawMove(m, gameboard.getTurn());
                            // If a bot move comes next and the game isn't over, handle it.
                            if (gameboard.isOver()) {
                                transitionToFinish();
                            } else {
                                botMove();
                            }
                        }
                    }
                }
                clickCount++;
            }
        });

        // Start the game off if a bot is X.
        if (player_red.equals(AgentType.BOT)) {
            botMove();
        }
    }

    // Applies a bot move, if necessary.
    private void botMove() {
        // If the bot needs to make a move, then let it.
        if (gameboard.whoHasTheTurn().equals(AgentType.BOT)) {
            moveHandler.restart();
        }
    }

    private void drawSquare(int x, int y) {
        if ((x + y) % 2 == 0) {
            gc.setFill(Color.web("#CAB7A8"));
        } else {
            gc.setFill(Color.web("#6A4E4B"));
        }
        int squareLength = X_DIM/gameboard.getSize();
        gc.fillRect(x*squareLength, y*squareLength, squareLength, squareLength);
    }

    // TODO: implement
    private void drawBoard() {
        int s = gameboard.getSize();
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                drawSquare(j, i);
                Piece p = gameboard.getSquare(j, i).getContents();
                if (p != null) {
                    drawPiece(gameboard.getSquare(j,i), p.getPlayer(), p.getType());
                }
            }
        }
    }

    // The move must be drawn *after* the move is carried out.
    private void drawMove(Move move, Player player) {
        // erase the piece from the origin square, all captures, and the shown conditional moves.
        drawSquare(move.getOrigin().getX(), move.getOrigin().getY());
        for (Square c : move.getCaptures()) {
            drawSquare(c.getX(), c.getY());
        }
        Piece p = move.getDestination().getContents();
        drawPiece(move.getDestination(), p.getPlayer(), p.getType());
    }

    private void drawPiece(Square square, Player player, Piece.Type type) {
        if (player == Player.RED) {
            gc.setFill(Color.web("#cc0000"));
        } else {
            gc.setFill(Color.web("#f2f2f2"));
        }
        int x = square.getX();
        int y = square.getY();
        int s = gameboard.getSize();
        int squareLength = X_DIM/s;
        gc.fillOval(x*squareLength + (squareLength/9), y*squareLength + (squareLength/9),
                7*squareLength/9, 7*squareLength/9);
        if (type == Piece.Type.KING) {
            gc.drawImage(new Image("resources/crown.png"),
                    x*squareLength + (squareLength/5), y*squareLength + (squareLength/5),
                    3*squareLength/5, 3*squareLength/5);
        }
    }

    private void drawPotential(Square square) {
        int x = square.getX();
        int y = square.getY();
        int squareLength = X_DIM/gameboard.getSize();
        gc.setFill(Color.web("#33cc33"));
        gc.fillOval(x*squareLength + (squareLength/3), y*squareLength + (squareLength/3),
                squareLength/3, squareLength/3);
    }

    // Execute this code when the game ends. We need to do the song and dance
    // with a new Service in order to add a one second pause.
    private void transitionToFinish() {
        Service<Void> pause = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Thread.sleep(1000);
                        return null;
                    }
                };
            }
        };
        pause.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                // 1. Take a picture of the final board state,
                // to use as a background to the finish scene.
                Image finalState = canvas.snapshot(new SnapshotParameters(), null);

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("Finish.fxml"));

                // 2. If a human won the game, make the finish background green.
                Player winner = gameboard.findWinner();
                boolean humanWon = (winner == Player.RED && player_red == AgentType.HUMAN) ||
                        (winner == Player.WHITE && player_white == AgentType.HUMAN);

                // 3. Let the finish controller know who won the game and what to use as a background.
                Parent finishParent;
                try {
                    finishParent = loader.load();
                } catch (Exception exception) {
                    finishParent = null;
                }
                FinishController finishController = loader.getController();
                finishController.setOptions(winner, finalState,
                        player_red, player_white, gameboard.getSize(), botType, humanWon);

                // 4. Display the finish scene in the window.
                Scene finishScene = new Scene(finishParent);
                Stage window = (Stage) canvas.getScene().getWindow();
                window.setScene(finishScene);
            }
        });
        pause.restart();
    }

}
