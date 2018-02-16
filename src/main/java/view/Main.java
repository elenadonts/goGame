package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static final int DEFAULT_WIDTH = 200;
    private static final int DEFAULT_HEIGHT = 180;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {//init UI and run player
        Parent login = FXMLLoader.load(getClass().getResource("/login.fxml"));
        primaryStage.setTitle("GoGame login");
        primaryStage.setScene(new Scene(login));
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(we -> {
            System.exit(0);
        });
    }
}
