package view;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import static view.StoneColor.BLACK;

/**
 * Stone class creates visual representation of game stone(black, white)
 */
public class Stone extends StackPane {

    private double x;
    private double y;
    private StoneColor stoneColor;

    /**
     * constructor for creating stones
     *
     * @param stoneColor color of stone
     * @param x x coordinate of stone
     * @param y  y coordinate of stone
     * @param tileSize size of game tile
     */
    public Stone(StoneColor stoneColor, double x, double y, double tileSize) {
        this.x = x;
        this.y = y;
        this.stoneColor = stoneColor;
        relocate(x, y);

        double backCircleRadius = tileSize / 2 - tileSize * 0.06;
        Circle backCircle = new Circle(backCircleRadius);
        backCircle.setFill(Color.valueOf("#db9900"));
        backCircle.setTranslateX((tileSize - backCircleRadius * 2) / 2);
        backCircle.setTranslateY((tileSize - backCircleRadius * 2) / 2);

        double frontCircleRadius = tileSize / 2 - tileSize * 0.1;
        Circle frontCircle = new Circle(frontCircleRadius);
        frontCircle.setTranslateX((tileSize - backCircleRadius * 2) / 2);
        frontCircle.setTranslateY((tileSize - backCircleRadius * 2) / 2);
        if (stoneColor.equals(BLACK)) {
            RadialGradient blackGrad = new RadialGradient(0,
                    .0,
                    frontCircle.getCenterX(),
                    frontCircle.getCenterY(),
                    frontCircle.getRadius(),
                    false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, Color.WHITE),
                    new Stop(1, Color.BLACK));
            frontCircle.setFill(blackGrad);
            getChildren().addAll(backCircle, frontCircle);
        } else {
            RadialGradient whiteGrad = new RadialGradient(0,
                    .0,
                    frontCircle.getCenterX(),
                    frontCircle.getCenterY(),
                    frontCircle.getRadius(),
                    false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, Color.WHITE),
                    new Stop(0.35, Color.WHITE),
                    new Stop(1, Color.SILVER));
            frontCircle.setFill(whiteGrad);
            getChildren().addAll(backCircle, frontCircle);
        }
    }

    /**
     * method for getting stone x coordinate
     *
     * @return stone x coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * method for getting stone y coordinate
     *
     * @return stone y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * method for getting stone color
     *
     * @return color of stone
     */
    public StoneColor getStoneColor() {
        return stoneColor;
    }
}
