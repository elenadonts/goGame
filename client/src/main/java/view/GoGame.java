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
import model.GameField;
import model.Point;


public class GoGame extends Application {
    public static final int TILE_SIZE = 100;
    public static final int WIDTH = 5;
    public static final int HEIGHT = 5;
    public static int count = 1;

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

        Point[][] gameGrid = new Point[WIDTH+1][HEIGHT+1];

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile(x, y, this);
                gameGrid[x][y] = new Point(y * TILE_SIZE, x * TILE_SIZE);
                tileGroup.getChildren().add(tile);
                tileGroup.setLayoutX(50);
                tileGroup.setLayoutY(50);
            }
            gameGrid[WIDTH][y] = new Point(y * TILE_SIZE, WIDTH * TILE_SIZE);
            System.out.println();
        }
        for (int i = 0; i < HEIGHT+1; i++){
            gameGrid[i][HEIGHT] = new Point(HEIGHT * TILE_SIZE, i * TILE_SIZE);
        }

        //later - send in xml
        GameField.initGameField(gameGrid);
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
        count++;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
