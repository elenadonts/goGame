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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class GoGame extends Application {
    public static final int TILE_SIZE = 100;
    public static final int WIDTH = 5;
    public static final int HEIGHT = 5;
    public static int clickCount = 1;

    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();

    private Set<Stone> stones = new HashSet<>();
    private LastStone lastStone;

    public Group getTileGroup() {
        return tileGroup;
    }
    public LastStone getLastStone() {
        return lastStone;
    }

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE + TILE_SIZE, HEIGHT * TILE_SIZE + TILE_SIZE);
        root.getChildren().addAll(tileGroup, pieceGroup);
        root.setBackground(new Background(new BackgroundFill(Color.valueOf("#db9900"), CornerRadii.EMPTY, Insets.EMPTY)));

        Point[][] gameGrid = new Point[WIDTH + 1][HEIGHT + 1];

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile(x, y, this);
                gameGrid[x][y] = new Point(y * TILE_SIZE, x * TILE_SIZE);
                tileGroup.getChildren().add(tile);
                tileGroup.setLayoutX(TILE_SIZE / 2);
                tileGroup.setLayoutY(TILE_SIZE / 2);
            }
            gameGrid[WIDTH][y] = new Point(y * TILE_SIZE, WIDTH * TILE_SIZE);
            System.out.println();
        }
        for (int i = 0; i < HEIGHT + 1; i++){
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
        stones.add(stone);
        pieceGroup.getChildren().add(stone);
        clickCount++;
    }

    public void drawLastStone(LastStone lastStone) {
        pieceGroup.getChildren().add(lastStone);
        this.lastStone = lastStone;
    }

    public void removeStone(double xCoordinate, double yCoordinate) {
        System.out.println(stones);
        Iterator<Stone> iterator = stones.iterator();
        while (iterator.hasNext()){
            Stone stone = iterator.next();
            if ((stone.getLayoutX() == xCoordinate) && (stone.getLayoutY() == yCoordinate)) {
                pieceGroup.getChildren().remove(stone);
                iterator.remove();
            }
        }
    }

    public void removeLastStone() {
        pieceGroup.getChildren().remove(lastStone);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
