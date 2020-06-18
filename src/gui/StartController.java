package gui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import checkers.Board.*;

public class StartController {

    @FXML private ToggleGroup boardSize = new ToggleGroup();
    @FXML private RadioButton six = new RadioButton();
    @FXML private RadioButton eight = new RadioButton();
    @FXML private RadioButton ten = new RadioButton();

    @FXML private ToggleGroup playerOne = new ToggleGroup();
    @FXML private RadioButton humanOne = new RadioButton();
    @FXML private RadioButton randomOne = new RadioButton();

    @FXML private ToggleGroup playerTwo = new ToggleGroup();
    @FXML private RadioButton humanTwo = new RadioButton();
    @FXML private RadioButton randomTwo = new RadioButton();

    private AgentType playerRed;
    private AgentType playerWhite;

    @FXML private void initialize() {

        RadioButton[] buttons = {humanOne, randomOne, humanTwo, randomTwo,
        six, eight, ten};

        for (RadioButton button : buttons) {
            button.getStyleClass().remove("radio-button");
            button.getStyleClass().add("toggle-button");
        }

        humanOne.setToggleGroup(playerOne);
        randomOne.setToggleGroup(playerOne);
        playerOne.selectToggle(humanOne);

        humanTwo.setToggleGroup(playerTwo);
        randomTwo.setToggleGroup(playerTwo);
        playerTwo.selectToggle(randomTwo);

        six.setToggleGroup(boardSize);
        eight.setToggleGroup(boardSize);
        ten.setToggleGroup(boardSize);
        boardSize.selectToggle(eight);
    }

    // What to do when the user presses the "play" button.
    @FXML private void pressPlay(ActionEvent e) throws Exception {
        RadioButton redButton = (RadioButton) playerOne.getSelectedToggle();
        RadioButton whiteButton = (RadioButton) playerTwo.getSelectedToggle();
        RadioButton boardSizeButton = (RadioButton) boardSize.getSelectedToggle();

        if (redButton.equals(humanOne)) {
            playerRed = AgentType.HUMAN;
        } else {
            playerRed = AgentType.BOT;
        }
        if (whiteButton.equals(humanTwo)) {
            playerWhite = AgentType.HUMAN;
        } else {
            playerWhite = AgentType.BOT;
        }

        int s;
        if (boardSizeButton.equals(six)) {
            s = 6;
        } else if (boardSizeButton.equals(eight)) {
            s = 8;
        } else {
            s = 10;
        }

        String bot = "random";

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("Play.fxml"));
        Parent playParent = loader.load();

        PlayController playController = loader.getController();

        Scene playScene = new Scene(playParent);
        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setScene(playScene);

        playController.setOptions(playerRed, playerWhite, s, bot);
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
