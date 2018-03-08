package view;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import static view.StoneColor.BLACK;


public class LastStone extends StackPane {


    public LastStone(StoneColor stoneColor, double x, double y, double tileSize) {
        relocate(x, y);

        double middleCircleRadius = 0.1 * tileSize / 2;
        Circle middleCircle = new Circle(middleCircleRadius);
        middleCircle.setFill(Color.RED);
        middleCircle.setTranslateX((tileSize - middleCircleRadius * 2) / 2);
        middleCircle.setTranslateY((tileSize - middleCircleRadius * 2) / 2);

        if (stoneColor.equals(BLACK)) {
            getChildren().addAll(middleCircle);
        } else {
            getChildren().addAll(middleCircle);
        }
    }
}
