package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Launcher extends Application {

    public void start(Stage stage) throws IOException {
        //Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/UI/UI.fxml"));
        Scene scene = new Scene(root);

//        Image icon = new Image("icon.jpg");
//        stage.getIcons().add(icon);

        stage.setScene(scene);
        stage.setFullScreen(false);
        stage.setTitle("UETMoney");
        stage.show();
    }
}

