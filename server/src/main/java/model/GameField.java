package model;


import org.apache.log4j.Logger;

import java.util.*;

public class GameField {

    private static final Logger logger = Logger.getLogger(GameField.class);
    private static final int STEP_SIZE = 100;
    private static Point[][] gameGrid;
    private static Point[][] tempGrid;
    private static Set<Point> pointsToRemove = new HashSet<>();
    private static Set<Point> points = new HashSet<>();
    private static PointState stoneColorGeneral;
    private static Point blockedPoint;
    private static boolean block;

    public static Set<Point> getPointsToRemove() {
        return pointsToRemove;
    }

    public static void initGameField(Point[][] allPoints) { //points received in xml from player after game start
        gameGrid = allPoints;
        for (int x = 0; x < gameGrid.length; x++) {
            for (int y = 0; y < gameGrid[x].length; y++) {
                gameGrid[x][y].setPointState(PointState.BLANK);
            }
        }
    }

    public static boolean isAllowedToPlace(double xCoordinate, double yCoordinate, PointState stoneColor) {
        stoneColorGeneral = stoneColor;
        Point pointOnGrid = new Point(xCoordinate, yCoordinate);
        pointOnGrid.setPointState(stoneColor);

        if ((blockedPoint != null) && (!blockedPoint.getPointState().equals(stoneColor))) {
            int row = getArrayCellIndexFromCoordinate(blockedPoint.getY());
            int column = getArrayCellIndexFromCoordinate(blockedPoint.getX());
            gameGrid[row][column].setPointState(PointState.BLANK);
            blockedPoint = null;
            block = false;
        }

        if (isCellOccupied(pointOnGrid)) {
            return false;
        }

        if (hasAnyOpenWay(pointOnGrid)) {
            addStone(new Point(xCoordinate, yCoordinate), stoneColor);
            pointsToRemove();
            removeSurroundedPoints();
            // add method, for transfering Set<Point> pointsToRemove to the client view
            // after that we can uncomment clearPointsToRemoveList() method(see below)
            //clearPointsToRemoveList();
            return true;
        }

        if (isDoubleSurrounded(pointOnGrid, stoneColor)) {
            removeSurroundedPoints();
            if (block) {
                GameField.repeatingPosition();
            }
            // add method, for transfering Set<Point> pointsToRemove to the client view
            // after that we can uncomment clearPointsToRemoveList() method(see below)
            //clearPointsToRemoveList();
            return true;
        }

        return false;
    }

    private static boolean isDoubleSurrounded(Point point, PointState stoneColor) {
        int row = getArrayCellIndexFromCoordinate(point.getY());
        int column = getArrayCellIndexFromCoordinate(point.getX());
        gameGrid[row][column].setPointState(stoneColor);
        pointsToRemove();
        if (lookForSurroundedEnemyStone(stoneColor)) {
            removeCurrentColorStones();
            return true;
        }
        gameGrid[row][column].setPointState(PointState.BLANK);
        return false;
    }

    public static void removeCurrentColorStones() {
        Iterator<Point> iterator = pointsToRemove.iterator();
        while (iterator.hasNext()) {
            Point point = iterator.next();
            if (point.getPointState().equals(stoneColorGeneral)) {
                iterator.remove();
            }
        }
        checkToBlockPoint();
    }

    public static void repeatingPosition() {
        for (Point point : pointsToRemove) {
            blockedPoint = point.clone();
            break;
        }
        int row = getArrayCellIndexFromCoordinate(blockedPoint.getY());
        int column = getArrayCellIndexFromCoordinate(blockedPoint.getX());
        gameGrid[row][column].setPointState(PointState.BLOCKED);
    }

    public static void checkToBlockPoint() {
        if (pointsToRemove.size() == 1) {
            block = true;
        } else {
            block = false;
        }
    }

    private static boolean lookForSurroundedEnemyStone(PointState stoneColor) {
        Iterator<Point> iterator = pointsToRemove.iterator();
        while (iterator.hasNext()) {
            Point point = iterator.next();
            if (point.getPointState().equals(getEnemyStoneColor(stoneColor))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCellOccupied(Point target) {
        int row = getArrayCellIndexFromCoordinate(target.getY());
        int column = getArrayCellIndexFromCoordinate(target.getX());
        if (gameGrid[row][column].getPointState() != PointState.BLANK) {
            return true;
        }
        return false;
    }

    private static boolean hasAnyOpenWay(Point target) {
        initTempGrid();
        PointState enemyColor = getEnemyStoneColor(target.getPointState());
        int row = getArrayCellIndexFromCoordinate(target.getY());
        int column = getArrayCellIndexFromCoordinate(target.getX());
        tempGrid[row][column].setPointState(PointState.TARGET);
        if (wayIsOpen(target.getPointState(), enemyColor, row, column)) {
            return true;
        }
        return false;
    }

    private static PointState getEnemyStoneColor(PointState currentColor) {
        PointState enemyStoneColor;
        switch (currentColor) {
            case STONE_WHITE:
                enemyStoneColor = PointState.STONE_BLACK;
                break;
            case STONE_BLACK:
                enemyStoneColor = PointState.STONE_WHITE;
                break;
            default:
                enemyStoneColor = PointState.BLANK;
                break;
        }
        return enemyStoneColor;
    }

    private static boolean wayIsOpen(PointState current, PointState opposite, int rowToCheck, int columnToCheck) {
        boolean isOpen;
        try {
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == PointState.BLANK) {
//                logger.info("Blank cell " + rowToCheck + " " + columnToCheck);
                return true;
            }
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == opposite) {
//               logger.info("Enemy stone");
                return false;
            }
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == current) {
                tempGrid[rowToCheck][columnToCheck].setPointState(PointState.USED);
//                logger.info("Own stone");
                return wayIsOpen(current, opposite, rowToCheck, columnToCheck);
            }
            tempGrid[rowToCheck][columnToCheck].setPointState(opposite);
            isOpen = wayIsOpen(current, opposite, rowToCheck, columnToCheck - 1) || //left
                    wayIsOpen(current, opposite, rowToCheck, columnToCheck + 1) || //right
                    wayIsOpen(current, opposite, rowToCheck - 1, columnToCheck) || //up
                    wayIsOpen(current, opposite, rowToCheck + 1, columnToCheck); //down
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == PointState.USED) return false;
        } catch (ArrayIndexOutOfBoundsException e) {
//            logger.info("Out of game grid");
            return false;
        }
        return isOpen;
    }

    private static int getArrayCellIndexFromCoordinate(double coordinate) {
        return (int) coordinate / STEP_SIZE;
    }

    public static void addStone(Point stoneCoordinates, PointState pointState) {
        int row = getArrayCellIndexFromCoordinate(stoneCoordinates.getY());
        int column = getArrayCellIndexFromCoordinate(stoneCoordinates.getX());
        gameGrid[row][column].setPointState(pointState);
    }

    public static void pointsToRemove() {
        for (int x = 0; x < gameGrid.length; x++) {
            for (int y = 0; y < gameGrid[x].length; y++) {
                if (!hasAnyOpenWay(gameGrid[x][y])) {
                    pointsToRemove.add(gameGrid[x][y].clone());
                    points.add(gameGrid[x][y]);
                }
            }
        }
    }

    public static void removeSurroundedPoints() {
        if (points.size() > 0) {
            for (Point point : points) {
                if (point.getPointState().equals(getEnemyStoneColor(stoneColorGeneral))) {
                    point.setPointState(PointState.BLANK);
                }
            }
            points.clear();
        }
    }

    public static void clearPointsToRemoveList() {
        pointsToRemove.clear();
    }

    private static void initTempGrid() {
        tempGrid = new Point[gameGrid.length][gameGrid[0].length];
        for (int x = 0; x < gameGrid.length; x++) {
            for (int y = 0; y < gameGrid[x].length; y++) {
                tempGrid[x][y] = gameGrid[x][y].clone();
            }
        }
    }
}