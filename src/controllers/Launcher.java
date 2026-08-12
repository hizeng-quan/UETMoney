package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;

public class Launcher extends Application {

    public void start(Stage stage) throws IOException {
        //Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/UI/UI.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/UI/style.css").toExternalForm());

        Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
        stage.getIcons().add(icon);

        stage.setScene(scene);
        stage.setFullScreen(false);
        stage.setTitle("UETMoney");

        stage.setAlwaysOnTop(true);
        stage.show();
        stage.toFront();
        stage.requestFocus();
        stage.setAlwaysOnTop(false);
    }
}

