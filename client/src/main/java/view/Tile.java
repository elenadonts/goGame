package view;

import controller.PlayerWindowController;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.ClientHandler;
import org.apache.log4j.Logger;

/**
 * Tile class creates visual representation of every single square of game field
 */
public class Tile extends Rectangle {
    private GoGame game;
    private double xCoordinate;
    private double yCoordinate;
    private static ClientHandler clientHandler;
    private static PlayerWindowController playerWindowController;
    private static final Logger LOGGER = Logger.getLogger(Tile.class);


    /**
     * constructor for creating tiles(squares)
     *
     * @param x x coordinate of tile
     * @param y  y coordinate of tile
     * @param game  reference to GoGame object
     * @param tileSize size of game tile
     */
    public Tile(int x, int y, GoGame game, double tileSize) {
        this.game = game;
        setWidth(tileSize);
        setHeight(tileSize);
        relocate(x * tileSize, y * tileSize);
        setFill(Color.valueOf("#db9900"));
        setStroke(Color.BLACK);
        setStrokeWidth(2);

        this.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                this.getTileCorner(event.getSceneX() - 180, event.getSceneY() - 70);
                clientHandler.send(playerWindowController.sendCoordinatesToServer(xCoordinate,yCoordinate,
                        playerWindowController.getColorCurrentPlayer()));
            }
        });
    }

    /**
     * method for defining coordinates of closest corner of tile
     *
     * @param mouseClickX    x coordinate of mouse click
     * @param mouseClickY    y coordinate of mouse click
     */
    private void getTileCorner(double mouseClickX, double mouseClickY) {
        LOGGER.info("Mouse coordinates: x - " + mouseClickX + " y - " + mouseClickY);
        double layoutOffsetX = game.getTileGroup().getLayoutX();
        double layoutOffsetY = game.getTileGroup().getLayoutY();
        double strokeWidth = this.getStrokeWidth() / 2;
        Bounds bounds = this.getBoundsInParent();
        double middleX = ((layoutOffsetX + bounds.getMinX()) + (layoutOffsetX + bounds.getMaxX())) / 2;
        double middleY = ((layoutOffsetY + bounds.getMinY()) + (layoutOffsetY + bounds.getMaxY())) / 2;
        if (mouseClickX <= middleX) {
            if (mouseClickY <= middleY) {
                xCoordinate = bounds.getMinX() + strokeWidth;
                yCoordinate = bounds.getMinY() + strokeWidth;
            } else {
                xCoordinate = bounds.getMinX() + strokeWidth;
                yCoordinate = bounds.getMaxY() - strokeWidth;
            }
        } else {
            if (mouseClickY <= middleY) {
                xCoordinate = bounds.getMaxX() - strokeWidth;
                yCoordinate = bounds.getMinY() + strokeWidth;
            } else {
                xCoordinate = bounds.getMaxX() - strokeWidth;
                yCoordinate = bounds.getMaxY() - strokeWidth;
            }
        }
    }

    /**
     * method for setting reference to ClientHandler object
     *
     * @param clientHandler reference to ClientHandler object
     */
    public static void setClientHandler(ClientHandler clientHandler) {
        Tile.clientHandler = clientHandler;
    }

    /**
     * method for setting reference to PlayerWindowController object
     *
     * @param playerWindowController reference to PlayerWindowController object
     */
    public static void setPlayerWindowController(PlayerWindowController playerWindowController) {
        Tile.playerWindowController = playerWindowController;
    }
}