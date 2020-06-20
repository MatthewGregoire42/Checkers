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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import static gui.Main.X_DIM;
import static gui.Main.Y_DIM;
import checkers.Board;
import checkers.Board.*;
import ai.AlphaBetaAI.StaticEval;

import java.util.ArrayList;
import java.util.List;

public class PlayController {

    @FXML private Canvas canvas;
    private GraphicsContext gc;
    private Board gameboard;
    private int clickCount;
    private List<Move> currentLegals;
    private BotMoveService redMoveHandler, whiteMoveHandler;

    private AgentType[] players;
    private Agent[] bots;
    private String[] botTypes;
    private boolean[] timed;
    private int[] options; // depthOne, timeLimitOne, depthTwo, timeLimitTwo;
    private StaticEval[] evals;

    // Sets who is playing: HvH, HvB, or BvB, and the human's player.
    // Then starts the game.
    // Called by the StartController to pass in information.
    // The initializer for this class is basically useless, because
    // we need so much information from the Start screen before the
    // Play screen can do anything.
    public void setOptions(AgentType[] players, String[] botTypes, int size,
                           int[] options, boolean[] timed, StaticEval[] evals) {

        canvas.requestFocus();

        this.players = players;
        this.botTypes = botTypes;
        this.options = options;
        this.timed = timed;
        this.evals = evals;

        gameboard = new Board(size);
        gameboard.setPlayer(Player.RED, players[0]);
        gameboard.setPlayer(Player.WHITE, players[1]);

        // This is where we'll draw the game as it progresses
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(5);
        drawBoard();
        clickCount = 0;

        bots = new Agent[2];
        if (botTypes[0].equals("Random AI")) {
            bots[0] = new RandomAI();
        } else if (botTypes[0].equals("Alpha-Beta AI")) {
            bots[0] = new AlphaBetaAI(options[0], options[0], timed[0], evals[0]);
        }
        if (botTypes[1].equals("Random AI")) {
            bots[1] = new RandomAI();
        } else if (botTypes[1].equals("Alpha-Beta AI")) {
            bots[1] = new AlphaBetaAI(options[1], options[1], timed[1], evals[1]);
        }

        // How to handle bot moves.
        redMoveHandler = new BotMoveService(gameboard, bots[0]);
        botSetup(redMoveHandler);
        whiteMoveHandler = new BotMoveService(gameboard, bots[1]);
        botSetup(whiteMoveHandler);

        // How to handle human moves.
        canvas.setOnMouseClicked(e -> {
            if (gameboard.whoHasTheTurn().equals(AgentType.HUMAN)) {
                int x = (int) (e.getX() / (X_DIM/gameboard.getSize()));
                int y = (int) (e.getY() / (Y_DIM/gameboard.getSize()));
                Square square = gameboard.getSquare(x, y);
                // If it's the first click of a move, show the available options.
                if (clickCount % 2 == 0 && square.getContents() != null &&
                square.getContents().getPlayer() == gameboard.getTurn()) {
                    clickCount++;
                    currentLegals = gameboard.getLegalMovesFor(square);
                    for (Move m : currentLegals) {
                        Square dest = m.getDestination();
                        drawPotential(dest);
                    }
                    if (currentLegals.size() == 0) {
                        clickCount++;
                    }
                // Otherwise, apply the selected move.
                } else if (clickCount % 2 == 1) {
                    clickCount++;
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
            }
        });

        // Start the game off if a bot is red.
        if (players[0] == AgentType.BOT) {
            botMove();
        }
    }

    private void botSetup(BotMoveService service) {
        service.setOnSucceeded(e -> {
            Move move = service.getValue();
            gameboard.applyMove(move);
            drawMove(move, gameboard.getTurn());

            if (gameboard.isOver()) {
                transitionToFinish();
            } else {
                botMove();
            }
        });
        service.setOnFailed(e -> {
            e.getSource().getException().printStackTrace();
        });
    }

    // Applies a bot move, if necessary.
    private void botMove() {
        // If the bot needs to make a move, then let it.
        if (gameboard.whoHasTheTurn().equals(AgentType.BOT)) {
            if (gameboard.getTurn() == Player.RED) {
                redMoveHandler.restart();
            } else {
                whiteMoveHandler.restart();
            }
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
                boolean humanWon = (winner == Player.RED && players[0] == AgentType.HUMAN) ||
                        (winner == Player.WHITE && players[1] == AgentType.HUMAN);

                // 3. Let the finish controller know who won the game and what to use as a background.
                Parent finishParent;
                try {
                    finishParent = loader.load();
                } catch (Exception exception) {
                    finishParent = null;
                }
                FinishController finishController = loader.getController();
                finishController.setOptions(winner, finalState, humanWon, gameboard.getSize(),
                        players, botTypes, options, timed, evals);

                // 4. Display the finish scene in the window.
                Scene finishScene = new Scene(finishParent);
                Stage window = (Stage) canvas.getScene().getWindow();
                window.setScene(finishScene);
            }
        });
        pause.restart();
    }

    @FXML private void handleKeyPressed(KeyEvent e) throws Exception {
        if (e.getCode() == KeyCode.ESCAPE) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Start.fxml"));
            Parent startParent = loader.load();

            Scene startScene = new Scene(startParent);
            Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
            window.setScene(startScene);
        }
    }

}
