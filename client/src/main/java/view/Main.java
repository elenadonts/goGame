package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sun.dc.pr.PRError;

import java.lang.ref.PhantomReference;

public class Main extends Application {
//    private static final int DEFAULT_WIDTH = 400;
//    private static final int DEFAULT_HEIGHT = 300;
    public static Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {//init UI and run player
        Parent root = FXMLLoader.load(getClass().getResource("/playerWindow.fxml"));

        primaryStage.setTitle("GoGame");
        primaryStage.setScene(new Scene(root));
//        primaryStage.setMinWidth(DEFAULT_WIDTH);
//        primaryStage.setMinHeight(DEFAULT_HEIGHT);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(we -> {
            System.exit(0);
        });
        mainStage = primaryStage;
    }
}
