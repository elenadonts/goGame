package view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GoGame extends Application {
    public static final int TILE_SIZE = 100;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;

    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();

    public Group getTileGroup() {
        return tileGroup;
    }

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE + 100, HEIGHT * TILE_SIZE + 100);
        root.getChildren().addAll(tileGroup, pieceGroup);
        root.setBackground(new Background(new BackgroundFill(Color.valueOf("#db9900"), CornerRadii.EMPTY, Insets.EMPTY)));

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile(x, y, this);
                /* add to hashMap<Point, PointState> */
                tileGroup.getChildren().add(tile);
                tileGroup.setLayoutX(50);
                tileGroup.setLayoutY(50);
            }
        }
        return root;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(createContent());
        primaryStage.setTitle("GoGame");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void drawStone(Stone stone) {
        pieceGroup.getChildren().add(stone);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
