package gui;

import checkers.Board;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

public class Main extends Application {

    public static final int X_DIM = 600;
    public static final int Y_DIM = 600;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader startLoader = new FXMLLoader(getClass().getResource("Start.fxml"));
        VBox start = startLoader.<VBox>load();
        Scene scene = new Scene(start, X_DIM, Y_DIM);

        primaryStage.getIcons().add(new Image("resources/icon.png"));

        primaryStage.setScene(scene);
        primaryStage.setTitle("Checkers");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}