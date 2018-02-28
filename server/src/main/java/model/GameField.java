package model;


import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class GameField {

    private static final int STEP_SIZE = 100;
    private static Point[][] gameGrid;
    private static final Logger logger = Logger.getLogger(GameField.class);
    private static Point[][] tempGrid;

    public static void initGameField(Point[][] allPoints){//points received in xml from player after game start
        gameGrid = allPoints;
        for (int i = 0; i < gameGrid.length; i++){
            for (int j = 0; j < gameGrid[i].length; j++){
                gameGrid[i][j].setPointState(PointState.BLANK);
            }
        }
    }

    public static boolean isAllowedToPlace(double x, double y, PointState stoneColor){
        Point pointOnGrid = new Point(x, y);
        pointOnGrid.setPointState(stoneColor);
        if (isCellOccupied(pointOnGrid)) return false;

        if (hasAnyOpenWay(pointOnGrid)){
            return true;
        }
        return false;
    }

    private static boolean isCellOccupied(Point target){
        int row = getArrayCellIndexFromCoordinate(target.getY());
        int column = getArrayCellIndexFromCoordinate(target.getX());
        if (gameGrid[row][column].getPointState() != PointState.BLANK){
            return true;
        }
        return false;
    }

    private static boolean hasAnyOpenWay(Point target){
        initTempGrid();
        PointState enemyColor = getEnemyStoneColor(target.getPointState());
        int row = getArrayCellIndexFromCoordinate(target.getY());
        int column = getArrayCellIndexFromCoordinate(target.getX());
        tempGrid[row][column].setPointState(PointState.TARGET);
        if (wayIsOpen(target.getPointState(), enemyColor, row, column)){
            return true;
        }
        return false;
    }

    private static PointState getEnemyStoneColor (PointState currentColor){
        PointState enemyStoneColor;
        switch (currentColor){
            case STONE_WHITE: enemyStoneColor = PointState.STONE_BLACK;
                break;
            case STONE_BLACK: enemyStoneColor = PointState.STONE_WHITE;
                break;
            default: enemyStoneColor = PointState.BLANK;
                break;
        }
        return enemyStoneColor;
    }

    private static boolean wayIsOpen(PointState current, PointState opposite, int rowToCheck, int columnToCheck){
        boolean isOpen;
        try {
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == PointState.BLANK){
                //logger.info("Blank cell " + rowToCheck + " " + columnToCheck);
                return true;
            }
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == opposite){
               //logger.info("Enemy stone");
                return false;
            }
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == current){
                tempGrid[rowToCheck][columnToCheck].setPointState(PointState.USED);
               // logger.info("Own stone");
                return wayIsOpen(current, opposite, rowToCheck, columnToCheck);
            }
            tempGrid[rowToCheck][columnToCheck].setPointState(opposite);
            isOpen = wayIsOpen(current, opposite, rowToCheck, columnToCheck-1) || //left
                    wayIsOpen(current, opposite, rowToCheck, columnToCheck+1) || //right
                    wayIsOpen(current, opposite, rowToCheck-1, columnToCheck) || //up
                    wayIsOpen(current, opposite, rowToCheck+1, columnToCheck); //down
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == PointState.USED) return false;
        }
        catch (ArrayIndexOutOfBoundsException e) {
           // logger.info("Out of game grid");
            return false;
        }
        return isOpen;
    }

    private static int getArrayCellIndexFromCoordinate(double coordinate){
        return (int)coordinate/STEP_SIZE;
    }

    public static void addStone(Point stoneCoordinates, PointState pointState){
        int row = getArrayCellIndexFromCoordinate(stoneCoordinates.getY());
        int column = getArrayCellIndexFromCoordinate(stoneCoordinates.getX());
        gameGrid[row][column].setPointState(pointState);

    }

    public static List<Point> getPointsToRemove(){
        List<Point> pointsToRemove = new ArrayList<>();
        for (int i = 0; i < tempGrid.length; i++){
            for (int j = 0; j < tempGrid[i].length; j++){
                if (!hasAnyOpenWay(tempGrid[i][j])){
                    pointsToRemove.add(gameGrid[i][j].clone());
                }
            }
        }
        for (Point point: pointsToRemove) {
            point.setPointState(PointState.BLANK);
        }
        return pointsToRemove;
    }

    private static void initTempGrid(){
        tempGrid  =  new Point[gameGrid.length][gameGrid[0].length];
        for (int i = 0; i < gameGrid.length; i++){
            for (int j = 0; j < gameGrid[i].length; j++){
                tempGrid[i][j] = gameGrid[i][j].clone();
            }
        }
    }
}
