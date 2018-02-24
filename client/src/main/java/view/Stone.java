package view;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import static view.StoneColor.BLACK;

public class Stone extends StackPane {
    public static final int TILE_SIZE = 100;

    public Stone(StoneColor stoneColor, double x, double y) {
        if (stoneColor.equals(BLACK)){
            relocate(x + 6, y + 6);
            Circle backCircle = new Circle(TILE_SIZE / 2 - TILE_SIZE * 0.06);
            backCircle.setFill(Color.valueOf("#db9900"));
            Circle frontCircle = new Circle(TILE_SIZE / 2 - TILE_SIZE * 0.1);
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
            relocate(x + 6, y + 6);
            Circle backCircle = new Circle(TILE_SIZE / 2 - TILE_SIZE * 0.06);
            backCircle.setFill(Color.valueOf("#db9900"));
            Circle frontCircle = new Circle(TILE_SIZE / 2 - TILE_SIZE * 0.1);
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
}
