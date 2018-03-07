package view;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class GoGame {
    public static int TILE_SIZE = 80;
    private int side = 5;
    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();

    private Set<Stone> stones = new HashSet<>();
    private LastStone lastStone;
    private static final Logger LOGGER = Logger.getLogger(GoGame.class);

    public Group getTileGroup() {
        return tileGroup;
    }

    public LastStone getLastStone() {
        return lastStone;
    }

    public Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(side * TILE_SIZE + TILE_SIZE, side * TILE_SIZE + TILE_SIZE);
        root.getChildren().addAll(tileGroup, pieceGroup);
        root.setBackground(new Background(new BackgroundFill(Color.valueOf("#db9900"), CornerRadii.EMPTY, Insets.EMPTY)));
        for (int y = 0; y < side; y++) {
            for (int x = 0; x < side; x++) {
                Tile tile = new Tile(x, y, this);
                tileGroup.getChildren().add(tile);
                tileGroup.setLayoutX(TILE_SIZE / 2);
                tileGroup.setLayoutY(TILE_SIZE / 2);
            }
        }
        return root;
    }

    public void drawStone(Stone stone) {
        LOGGER.info("Drawing " + stone.getStoneColor() + " x: " + stone.getX() + " y: " + stone.getY());
        stones.add(stone);
        pieceGroup.getChildren().add(stone);
    }

    public void drawLastStone(LastStone lastStone) {
        pieceGroup.getChildren().add(lastStone);
        this.lastStone = lastStone;
    }

    public void removeStone(double xCoordinate, double yCoordinate) {
        LOGGER.info("Removing x: " + xCoordinate + " y: " + yCoordinate);
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

    public void setSide(int side) {
        this.side = side;
        TILE_SIZE = 400 / side;
    }
}