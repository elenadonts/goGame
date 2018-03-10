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

/**
 * GoGame class creates visual representation of game field
 */
public class GoGame {
    private static final int GAME_FIELD = 480;
    private double tileSize;
    private int numberOfTiles;
    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();

    private Set<Stone> stones = new HashSet<>();
    private LastStone lastStone;
    private static final Logger LOGGER = Logger.getLogger(GoGame.class);

    /**
     * method for getting tileGroup
     *
     * @return reference to the group of tiles
     */
    public Group getTileGroup() {
        return tileGroup;
    }

    /**
     * method for getting lastStone
     *
     * @return last stone
     */
    public LastStone getLastStone() {
        return lastStone;
    }

    /**
     * method for setting number of tiles in game field
     *
     * @param numberOfTiles number of tiles
     */
    public void setNumberOfTiles(int numberOfTiles) {
        this.numberOfTiles = numberOfTiles;
    }

    /**
     * method for setting size of tile
     *
     * @param tileSize size of tile
     */
    public void setTileSize(double tileSize) {
        this.tileSize = tileSize;
    }


    /**
     * method for getting size of tile
     *
     * @return size of tile
     */
    public double getTileSize(){
        return tileSize;
    }

    /**
     * method for creating pane with 2d array from every single tile
     *
     */
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

    /**
     * method for drawing stone at the game field
     *
     * @param stone stone
     */
    public void drawStone(Stone stone) {
        LOGGER.info("Drawing " + stone.getStoneColor() + " x: " + stone.getX() + " y: " + stone.getY());
        stones.add(stone);
        pieceGroup.getChildren().add(stone);
    }

    /**
     * method for drawing last set stone indication
     *
     * @param lastStone last set stone indication
     */
    public void drawLastStone(LastStone lastStone) {
        pieceGroup.getChildren().add(lastStone);
        this.lastStone = lastStone;
    }

    /**
     * method for removing stone from the game field
     *
     * @param xCoordinate x coordinate of stone
     * @param yCoordinate y coordinate of stone
     */
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

    /**
     * method for removing last set stone indication
     *
     */
    public void removeLastStone() {
        pieceGroup.getChildren().remove(lastStone);
    }
}