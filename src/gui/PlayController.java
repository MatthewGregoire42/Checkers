package gui;

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
import ai.*;

public class PlayController {

    @FXML private Canvas canvas;
    private GraphicsContext gc;
    private String botType;
    private AgentType player_X;
    private AgentType player_O;
    private Board gameboard;
    private BotMoveService moveHandler;

    // Sets who is playing: HvH, HvB, or BvB, and the human's player.
    // Then starts the game.
    // Called by the StartController to pass in information.
    // The initializer for this class is basically useless, because
    // we need so much information from the Start screen before the
    // Play screen can do anything.
    public void setOptions(AgentType X, AgentType O, int s, String botType) {
        player_X = X;
        player_O = O;
        this.botType = botType;

        gameboard = new Board(s);
        gameboard.setPlayer(Player.RED, player_X);
        gameboard.setPlayer(Player.WHITE, player_O);

        // This is where we'll draw the game as it progresses
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(5);
        drawBoard();

        Agent bot;
        if (botType.equals("random")) {
            bot = new RandomAI();
        } else {
            bot = new MinimaxAI();
        }

        // How to handle bot moves.
        moveHandler = new BotMoveService(gameboard, bot);
        moveHandler.setOnSucceeded( e -> {
            int[] move = moveHandler.getValue();
            drawMarker(move[0], move[1], gameboard.getTurn());
            gameboard.applyMove(move[0], move[1]);

            if (gameboard.isOver()) {
                transitionToFinish();
            } else {
                botMove();
            }
        });

        // How to handle human moves.
        canvas.setOnMouseClicked(e -> {
            if (gameboard.whoHasTheTurn().equals(AgentType.HUMAN)) {
                int x = (int) (e.getX() / (X_DIM/gameboard.getSize()));
                int y = (int) (e.getY() / (Y_DIM/gameboard.getSize()));
                humanMove(x, y);
            }
        });

        // Start the game off if a bot is X.
        if (player_X.equals(AgentType.BOT)) {
            botMove();
        }
    }

    // Takes a human move, draws it on the screen,
    // and applies it to the board. Then check if we
    // need to handle a bot move or the end of the game.
    // Called when the screen is clicked.
    private void humanMove(int x, int y) {
        if (gameboard.isLegalMove(x,y)) {
            drawMarker(x, y, gameboard.getTurn());
            gameboard.applyMove(x,y);
            // Handle the next move, if it's a bot move.
            if (gameboard.isOver()) {
                transitionToFinish();
            } else {
                botMove();
            }
        }
    }

    // Applies a bot move, if necessary.
    private void botMove() {
        // If the bot needs to make a move, then let it.
        if (gameboard.whoHasTheTurn().equals(AgentType.BOT)) {
            moveHandler.restart();
        }
    }

    private void drawBoard() {
        int s = gameboard.getSize();

        for (int i = 1; i < s; i++) {
            gc.strokeLine(i*X_DIM/s, 0, i*X_DIM/s, Y_DIM);
            gc.strokeLine(0, i*Y_DIM/s, X_DIM, i*Y_DIM/s);
        }
    }

    private void drawMarker(int x, int y, Player player) {
        int s = gameboard.getSize();
        if (player.equals(Player.RED)) {
            int x_start1 = X_DIM / (s*4) + (x * X_DIM) / s;
            int y_start1 = Y_DIM / (s*4) + (y * Y_DIM) / s;
            int x_end1 = 3 * X_DIM / (s*4) + (x * X_DIM) / s;
            int y_end1 = 3 * Y_DIM / (s*4) + (y * Y_DIM) / s;
            int x_start2 = 3 * X_DIM / (s*4) + (x * X_DIM) / s;
            int y_start2 = Y_DIM / (s*4) + (y * Y_DIM) / s;
            int x_end2 = X_DIM / (s*4) + (x * X_DIM) / s;
            int y_end2 = 3 * Y_DIM / (s*4) + (y * Y_DIM) / s;
            gc.strokeLine(x_start1, y_start1, x_end1, y_end1);
            gc.strokeLine(x_start2, y_start2, x_end2, y_end2);
        } else {
            gc.strokeOval((x*X_DIM)/s + X_DIM/(s*5), (y*Y_DIM)/s + Y_DIM/(s*5),
                    3*X_DIM/(s*5), 3*Y_DIM/(s*5));
        }
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
                // 1. Take a picture of the final board state, to use as a background to the finish scene.
                Image finalState = canvas.snapshot(new SnapshotParameters(), null);

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("Finish.fxml"));

                // 2. Let the finish controller know who won the game and what to use as a background.
                Parent finishParent;
                try {
                    finishParent = loader.load();
                } catch (Exception exception) {
                    finishParent = null;
                }
                FinishController finishController = loader.getController();
                finishController.setOptions(gameboard.findWinner(), finalState,
                        player_X, player_O, gameboard.getSize(), botType);

                // 3. Display the finish scene in the window.
                Scene finishScene = new Scene(finishParent);
                Stage window = (Stage) canvas.getScene().getWindow();
                window.setScene(finishScene);
            }
        });
        pause.restart();
    }

}
