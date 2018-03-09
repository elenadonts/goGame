package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class that start when user run app
 *
 * @author Eugene Lobin
 * @version 1.0 09 Mar 2018
 */
public class Main extends Application {
    public static Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Create new application main stage
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/playerWindow.fxml"));
        primaryStage.setTitle("GoGame");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(we -> System.exit(0));
        mainStage = primaryStage;
    }
}
