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

    @FXML private ToggleGroup who = new ToggleGroup();
    @FXML private RadioButton hvh_button = new RadioButton();
    @FXML private RadioButton hvb_button = new RadioButton();
    @FXML private RadioButton bvb_button = new RadioButton();

    @FXML private ToggleGroup player = new ToggleGroup();
    @FXML private RadioButton red_button = new RadioButton();
    @FXML private RadioButton white_button = new RadioButton();

    @FXML private ToggleGroup board_size = new ToggleGroup();
    @FXML private RadioButton three = new RadioButton();
    @FXML private RadioButton four = new RadioButton();
    @FXML private RadioButton five = new RadioButton();

    @FXML private ToggleGroup whichBot = new ToggleGroup();
    @FXML private RadioButton random = new RadioButton();
    @FXML private RadioButton minimax = new RadioButton();

    private AgentType player_red;
    private AgentType player_white;

    @FXML private void initialize() {

        RadioButton[] buttons = {hvh_button, hvb_button, bvb_button, red_button, white_button,
        three, four, five, random, minimax};

        for (RadioButton button : buttons) {
            button.getStyleClass().remove("radio-button");
            button.getStyleClass().add("toggle-button");
        }

        hvh_button.setToggleGroup(who);
        hvb_button.setToggleGroup(who);
        bvb_button.setToggleGroup(who);

        red_button.setToggleGroup(player);
        white_button.setToggleGroup(player);

        three.setToggleGroup(board_size);
        four.setToggleGroup(board_size);
        five.setToggleGroup(board_size);

        random.setToggleGroup(whichBot);
        minimax.setToggleGroup(whichBot);

        who.selectToggle(hvb_button);
        player.selectToggle(red_button);
        board_size.selectToggle(three);
        whichBot.selectToggle(minimax);

        BooleanProperty disableLarge = new SimpleBooleanProperty(false);
        disableLarge.bind(minimax.selectedProperty().and(
                hvb_button.selectedProperty().or(bvb_button.selectedProperty())));

        // Only allow 3x3 for minimax. But we also want to allow 3x3 when minimax
        // is not selected, so we add a listener instead of using a direct binding.
        four.setDisable(true);
        five.setDisable(true);
        minimax.selectedProperty().addListener((observable, oldValue, isSelected) -> {
            if (isSelected) {
                four.setDisable(true);
                five.setDisable(true);
                three.setSelected(true);
            } else {
                four.setDisable(false);
                five.setDisable(false);
            }
        });

        BooleanProperty disablePlayer = new SimpleBooleanProperty(false);
        disablePlayer.bind(hvh_button.selectedProperty().or(bvb_button.selectedProperty()));

        // The Player button isn't relevant when the user isn't doing human vs. bot.
        red_button.disableProperty().bind(disablePlayer);
        white_button.disableProperty().bind(disablePlayer);

        // The Bot button isn't relevant when the user selects human vs. human.
        random.disableProperty().bind(hvh_button.selectedProperty());
        minimax.disableProperty().bind(hvh_button.selectedProperty());
    }

    // What to do when the user presses the "play" button.
    @FXML private void pressPlay(ActionEvent e) throws Exception {
        RadioButton who_is_button = (RadioButton) who.getSelectedToggle();
        RadioButton player_is_button = (RadioButton) player.getSelectedToggle();
        RadioButton board_size_button = (RadioButton) board_size.getSelectedToggle();
        RadioButton bot_button = (RadioButton) whichBot.getSelectedToggle();

        if (who_is_button.equals(hvh_button)) {
            player_red = AgentType.HUMAN;
            player_white = AgentType.HUMAN;
        } else if (who_is_button.equals(hvb_button)) {
            if (player_is_button.equals(red_button)) {
                player_red = AgentType.HUMAN;
                player_white = AgentType.BOT;
            } else {
                player_red = AgentType.BOT;
                player_white = AgentType.HUMAN;
            }
        } else {
            player_red = AgentType.BOT;
            player_white = AgentType.BOT;
        }

        int s;
        if (board_size_button.equals(three)) {
            s = 3;
        } else if (board_size_button.equals(four)) {
            s = 4;
        } else {
            s = 5;
        }

        String bot;
        if (bot_button.equals(random)) {
            bot = "random";
        } else {
            bot = "minimax";
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("Play.fxml"));
        Parent playParent = loader.load();

        PlayController playController = loader.getController();

        Scene playScene = new Scene(playParent);
        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setScene(playScene);

        playController.setOptions(player_red, player_white, s, bot);
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
