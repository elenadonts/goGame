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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class GoGame {
    public static final int TILE_SIZE = 80;
    public static final int SIDE = 5;
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

    public Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(SIDE * TILE_SIZE + TILE_SIZE, SIDE * TILE_SIZE + TILE_SIZE);
        root.getChildren().addAll(tileGroup, pieceGroup);
        root.setBackground(new Background(new BackgroundFill(Color.valueOf("#db9900"), CornerRadii.EMPTY, Insets.EMPTY)));

        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                Tile tile = new Tile(x, y, this);
                tileGroup.getChildren().add(tile);
                tileGroup.setLayoutX(TILE_SIZE / 2);
                tileGroup.setLayoutY(TILE_SIZE / 2);
            }
        }
        return root;
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
        Iterator<Stone> iterator = stones.iterator();
        while (iterator.hasNext()) {
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
}