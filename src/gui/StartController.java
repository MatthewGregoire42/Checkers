package gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import checkers.Board.*;
import ai.AlphaBetaAI.StaticEval;

public class StartController {

    @FXML private ToggleGroup boardSize = new ToggleGroup();
    @FXML private RadioButton six;
    @FXML private RadioButton eight;
    @FXML private RadioButton ten;

    @FXML private ChoiceBox<String> playerOne;
    @FXML private ChoiceBox<String> playerTwo;

    @FXML private Slider strengthSliderOne;
    @FXML private Slider strengthSliderTwo;

    @FXML private ChoiceBox<String> alphaBetaEvalOne;
    @FXML private ChoiceBox<String> alphaBetaEvalTwo;

    @FXML private ToggleGroup depthOrStrengthOne = new ToggleGroup();
    @FXML private RadioButton depthOne;
    @FXML private RadioButton timeOne;
    @FXML private ToggleGroup depthOrStrengthTwo = new ToggleGroup();
    @FXML private RadioButton depthTwo;
    @FXML private RadioButton timeTwo;

    @FXML private void initialize() {
        six.setToggleGroup(boardSize);
        eight.setToggleGroup(boardSize);
        ten.setToggleGroup(boardSize);
        boardSize.selectToggle(eight);

        depthOne.setToggleGroup(depthOrStrengthOne);
        timeOne.setToggleGroup(depthOrStrengthOne);
        depthOrStrengthOne.selectToggle(depthOne);

        depthTwo.setToggleGroup(depthOrStrengthTwo);
        timeTwo.setToggleGroup(depthOrStrengthTwo);
        depthOrStrengthTwo.selectToggle(depthTwo);

        playerOne.getItems().addAll("Human", "Random AI", "Alpha-Beta AI");
        playerOne.setValue("Human");
        playerTwo.getItems().addAll("Human", "Random AI", "Alpha-Beta AI");
        playerTwo.setValue("Alpha-Beta AI");

        alphaBetaEvalOne.getItems().addAll("Basic Piece Value", "Positional Piece Value");
        alphaBetaEvalOne.setValue("Positional Piece Value");
        alphaBetaEvalTwo.getItems().addAll("Basic Piece Value", "Positional Piece Value");
        alphaBetaEvalTwo.setValue("Basic Piece Value");

        StringProperty alphaBetaSelected = new SimpleStringProperty("Alpha-Beta AI");

        Control[] controlsOne = {depthOne, timeOne, strengthSliderOne, alphaBetaEvalOne};
        Control[] controlsTwo = {depthTwo, timeTwo, strengthSliderTwo, alphaBetaEvalTwo};

        for (Control c : controlsOne) {
            c.disableProperty().bind(playerOne.valueProperty().isNotEqualTo(alphaBetaSelected));
        }
        for (Control c : controlsTwo) {
            c.disableProperty().bind(playerTwo.valueProperty().isNotEqualTo(alphaBetaSelected));
        }
    }

    // What to do when the user presses the "play" button.
    // This is almost all preprocessing the inputs from the
    // start menu, to feed into the PlayController.
    @FXML private void pressPlay(ActionEvent e) throws Exception {

        AgentType[] players = new AgentType[2];
        String[] botTypes = {playerOne.getValue(), playerTwo.getValue()};
        if (botTypes[0].equals("Human")) {
            players[0] = AgentType.HUMAN;
        } else {
            players[0] = AgentType.BOT;
        }
        if (botTypes[1].equals("Human")) {
            players[1] = AgentType.HUMAN;
        } else {
            players[1] = AgentType.BOT;
        }

        RadioButton boardSizeButton = (RadioButton) boardSize.getSelectedToggle();
        int size;
        if (boardSizeButton.equals(six)) {
            size = 6;
        } else if (boardSizeButton.equals(eight)) {
            size = 8;
        } else {
            size = 10;
        }

        int[] options = {(int) strengthSliderOne.getValue(), (int) strengthSliderTwo.getValue()};

        boolean[] timed = {timeOne.isSelected(), timeTwo.isSelected()};

        StaticEval[] evals = new StaticEval[2];

        if (alphaBetaEvalOne.getValue().equals("Basic Piece Value")) {
            evals[0] = StaticEval.BASIC_PIECEVALUE;
        } else {
            evals[0] = StaticEval.POSITIONAL_PIECEVALUE;
        }
        if (alphaBetaEvalTwo.getValue().equals("Basic Piece Value")) {
            evals[1] = StaticEval.BASIC_PIECEVALUE;
        } else {
            evals[1] = StaticEval.POSITIONAL_PIECEVALUE;
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("Play.fxml"));
        Parent playParent = loader.load();

        PlayController playController = loader.getController();

        Scene playScene = new Scene(playParent);
        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setScene(playScene);

        playController.setOptions(players, botTypes, size, options, timed, evals);
    }

    // Switch to "About" scene when the user presses the "About" button.
    @FXML private void pressAbout(ActionEvent e) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("About.fxml"));
        Parent aboutParent = loader.load();

        AboutController aboutController = loader.getController();

        Scene aboutScene = new Scene(aboutParent);
        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setScene(aboutScene);
    }

}
