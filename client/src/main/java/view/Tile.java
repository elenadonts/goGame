package view;

import javafx.geometry.Bounds;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Tile extends Rectangle {
    private GoGame game;
    private LastStone lastStone;
    private Stone stone;
    double xCoordinate;
    double yCoordinate;

    public Tile(int x, int y, GoGame game) {
        this.game = game;
        setWidth(GoGame.TILE_SIZE);
        setHeight(GoGame.TILE_SIZE);
        relocate(x * GoGame.TILE_SIZE, y * GoGame.TILE_SIZE);
        setFill(Color.valueOf("#db9900"));
        setStroke(Color.BLACK);
        setStrokeWidth(2);

        this.setOnMousePressed(event -> {
            if(event.getButton() == MouseButton.PRIMARY) {
                System.out.println("Mouse coordinates : X = " + event.getSceneX() + " Y = " + event.getSceneY());
                this.getTileCorner(event.getSceneX(), event.getSceneY());
                System.out.println("Corner coordinates: X = " + xCoordinate + " Y = " + yCoordinate);
                    if(game.clickCount % 2 != 0){
                        if(game.getLastStone() != null){
                            game.removeLastStone();
                        }
                        stone = new Stone(StoneColor.BLACK, xCoordinate, yCoordinate);
                        this.game.drawStone(stone);
                        lastStone = new LastStone(StoneColor.BLACK, xCoordinate, yCoordinate);
                        this.game.drawLastStone(lastStone);
                    } else {
                        game.removeLastStone();
                        stone = new Stone(StoneColor.WHITE, xCoordinate, yCoordinate);
                        this.game.drawStone(stone);
                        lastStone = new LastStone(StoneColor.WHITE, xCoordinate, yCoordinate);
                        this.game.drawLastStone(lastStone);
                    }
            }
        });
    }

    //Method for deleting all stones from the list
    /*private void removeSurroundedStones(List<Point> stonesToRemove) {
        for (Point point : stonesToRemove){
            game.removeStone(point.getX(), point.getY());
        }
    }*/

    public void getTileCorner(double mouseClickX, double mouseClickY) {
        double layoutOffsetX = game.getTileGroup().getLayoutX();
        double layoutOffsetY = game.getTileGroup().getLayoutY();
        double strokeWidth = this.getStrokeWidth() / 2;
        Bounds bounds = this.getBoundsInParent();
        double middleX = ((layoutOffsetX + bounds.getMinX()) + (layoutOffsetX + bounds.getMaxX())) / 2;
        double middleY = ((layoutOffsetY + bounds.getMinY()) +(layoutOffsetY +  bounds.getMaxY())) / 2;
        if(mouseClickX <= middleX) {
            if(mouseClickY <= middleY) {
                xCoordinate = bounds.getMinX() + strokeWidth;
                yCoordinate = bounds.getMinY() + strokeWidth;
            } else {
                xCoordinate = bounds.getMinX() + strokeWidth;
                yCoordinate = bounds.getMaxY() - strokeWidth;
            }
        } else {
            if(mouseClickY <= middleY) {
                xCoordinate = bounds.getMaxX() - strokeWidth;
                yCoordinate = bounds.getMinY() + strokeWidth;
            } else {
                xCoordinate = bounds.getMaxX() - strokeWidth;
                yCoordinate = bounds.getMaxY() - strokeWidth;
            }
        }
    }
}