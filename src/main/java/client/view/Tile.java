package client.view;

import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Tile extends Rectangle {
    private GoGame game;
    private Stone stone;
    private double[] corner;

    public Tile(int x, int y, GoGame game) {
        this.game = game;
        setWidth(GoGame.TILE_SIZE);
        setHeight(GoGame.TILE_SIZE);
        relocate(x * GoGame.TILE_SIZE, y * GoGame.TILE_SIZE);
        setFill(Color.valueOf("#db9900"));
        setStroke(Color.BLACK);
        setStrokeWidth(2);

        setOnMousePressed(event -> {
                System.out.println("Mouse coordinates : X = " + event.getSceneX() + " Y = " + event.getSceneY());
                this.getTileCorner(event.getSceneX(), event.getSceneY());
                System.out.println("Corner coordinates: = " + corner[0] + " Y = " + corner[1]);
                System.out.println();
            stone = new Stone(corner[0], corner[1]);
            this.game.drawStone(stone);
        });
    }

    public void getTileCorner(double mouseClickX, double mouseClickY) {
        double layoutOffsetX = game.getTileGroup().getLayoutX();
        double layoutOffsetY = game.getTileGroup().getLayoutY();
        double strokeWidth = this.getStrokeWidth() / 2;
        Bounds bounds = this.getBoundsInParent();
        corner = new double[2];
        double middleX = ((layoutOffsetX + bounds.getMinX()) + (layoutOffsetX + bounds.getMaxX())) / 2;
        double middleY = ((layoutOffsetY + bounds.getMinY()) +(layoutOffsetY +  bounds.getMaxY())) / 2;
        if(mouseClickX <= middleX) {
            if(mouseClickY <= middleY) {
                corner[0] = bounds.getMinX() + strokeWidth;
                corner[1] = bounds.getMinY() + strokeWidth;
            } else {
                corner[0] = bounds.getMinX() + strokeWidth;
                corner[1] = bounds.getMaxY() - strokeWidth;
            }
        } else {
            if(mouseClickY <= middleY) {
                corner[0] = bounds.getMaxX() - strokeWidth;
                corner[1] = bounds.getMinY() + strokeWidth;
            } else {
                corner[0] = bounds.getMaxX() - strokeWidth;
                corner[1] = bounds.getMaxY() - strokeWidth;
            }
        }
    }
}