package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static final int DEFAULT_WIDTH = 900;
    private static final int DEFAULT_HEIGHT = 700;

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {//init UI and run player
        Parent root = FXMLLoader.load(getClass().getResource("/playerWindow.fxml"));
        primaryStage.setTitle("GoGame");
        primaryStage.setScene(new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT));
        primaryStage.setMinWidth(DEFAULT_WIDTH);
        primaryStage.setMinHeight(DEFAULT_HEIGHT);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(we -> {
            System.exit(0);
        });
    }
}
