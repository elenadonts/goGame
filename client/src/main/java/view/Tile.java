package view;

import controller.PlayerWindowController;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.ClientHandler;
import org.apache.log4j.Logger;

public class Tile extends Rectangle {
    private GoGame game;
    private double xCoordinate;
    private double yCoordinate;
    private static ClientHandler clientHandler;
    private static PlayerWindowController playerWindowController;
    private static final Logger LOGGER = Logger.getLogger(Tile.class);


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

    public static void setClientHandler(ClientHandler clientHandler) {
        Tile.clientHandler = clientHandler;
    }

    public static void setPlayerWindowController(PlayerWindowController playerWindowController) {
        Tile.playerWindowController = playerWindowController;
    }
}