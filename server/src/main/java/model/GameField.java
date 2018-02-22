package model;

import java.util.HashMap;
import java.util.HashSet;

public class GameField {

    public static HashMap<Point, PointState> allPoints;

    public static void initGameField(HashSet<Point> points){//points received in xml from player after game start
        for (Point point : points){
            allPoints.put(point, PointState.BLANK);
        }
    }
}
