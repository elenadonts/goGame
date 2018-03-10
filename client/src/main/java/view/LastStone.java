package view;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import static view.StoneColor.BLACK;

/**
 * LastStone class creates visual indication of the last set stone(red circle)
 */
public class LastStone extends StackPane {

    /**
     * constructor for creating last set stone indication
     *
     * @param x x coordinate of stone
     * @param y  y coordinate of stone
     * @param tileSize size of game tile
     */
    public LastStone(double x, double y, double tileSize) {
        relocate(x, y);

        double radius = 0.1 * tileSize / 2;
        Circle redCircle = new Circle(radius);
        redCircle.setFill(Color.RED);
        redCircle.setTranslateX((tileSize - radius * 2) / 2);
        redCircle.setTranslateY((tileSize - radius * 2) / 2);

        getChildren().addAll(redCircle);
    }
}
