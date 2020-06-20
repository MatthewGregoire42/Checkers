package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import checkers.Board.*;
import ai.AlphaBetaAI.StaticEval;

public class FinishController {

    @FXML private Label label;
    @FXML private ImageView imgView;
    @FXML private VBox vbox;

    private int size;

    private AgentType[] players;
    private String[] botTypes;
    private int[] options;
    private boolean[] timed;
    private StaticEval[] evals;

    // The Finish screen needs to know who won and the final board state to set as
    // the background. It also needs to know who played as X and O and what size the
    // board was, in case the user wants to play again with the same settings.
    public void setOptions(Player won, Image image, boolean humanWon, int size,
                           AgentType[] players, String[] botTypes, int[] options,
                           boolean[] timed, StaticEval[] evals) {

        this.players = players;
        this.size = size;
        this.botTypes = botTypes;
        this.options = options;
        this.timed = timed;
        this.evals = evals;

        if (humanWon) {
            vbox.setStyle("-fx-background-color: rgba(200, 255, 200, 0.8); -fx-font: 24 system;");
        } else {
            vbox.setStyle("-fx-background-color: rgba(255, 175, 175, 0.8); -fx-font: 24 system;");
        }

        if (won == null) {
            label.setText("It's a tie!");
        } else {
            label.setText("Player " + won.toString() + " wins!");
        }
        imgView.setImage(image);
    }

    @FXML private void backToPlay(ActionEvent e) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("Play.fxml"));
        Parent playParent = loader.load();

        PlayController playController = loader.getController();

        Scene playScene = new Scene(playParent);
        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setScene(playScene);

        playController.setOptions(players, botTypes, size,
                options, timed, evals);
    }

    @FXML private void backToStart(ActionEvent e) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("Start.fxml"));
        Parent startParent = loader.load();

        StartController startController = loader.getController();

        Scene startScene = new Scene(startParent);
        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setScene(startScene);
    }
}
