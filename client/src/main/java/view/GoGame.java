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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GoGame {
    public static final int GAME_FIELD = 480;
    public static final int TILE_FIELD = 400;
    private double tileSize;
    private int numberOfTiles;
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

    public void setNumberOfTiles(int numberOfTiles) {
        this.numberOfTiles = numberOfTiles;
        initTileSize();
    }

    public void initTileSize() {
        tileSize = new BigDecimal(TILE_FIELD / numberOfTiles).setScale(2, RoundingMode.UP).doubleValue();
    }

    public double getTileSize(){
        return tileSize;
    }

    public Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(GAME_FIELD, GAME_FIELD);
        root.getChildren().addAll(tileGroup, pieceGroup);
        root.setBackground(new Background(new BackgroundFill(Color.valueOf("#db9900"), CornerRadii.EMPTY, Insets.EMPTY)));
        for (int y = 0; y < numberOfTiles; y++) {
            for (int x = 0; x < numberOfTiles; x++) {
                Tile tile = new Tile(x, y, this, tileSize);
                tileGroup.getChildren().add(tile);
                tileGroup.setLayoutX(tileSize / 2);
                tileGroup.setLayoutY(tileSize / 2);
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


}