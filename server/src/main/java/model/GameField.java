package model;

import org.apache.log4j.Logger;
import java.util.*;

public class GameField {
    private static final Logger logger = Logger.getLogger(GameField.class);
    private static final int STEP_SIZE = 80;
    private static Point[][] gameGrid;
    private static Point[][] tempGrid;
    private static Set<Point> pointsToRemove = new HashSet<>();
    private static Set<Point> points = new HashSet<>();
    private static PointState currentStoneColor;
    private static Point blockedPoint;
    private static boolean block;
    private static int capturedWhiteStones;
    private static int capturedBlackStones;

    public static Set<Point> getPointsToRemove() {
        return pointsToRemove;
    }

    public static boolean isBlock() {
        return block;
    }

    public static void initGameField(Point[][] allPoints) { //points received in xml from player after game start
        gameGrid = allPoints;
        for (int x = 0; x < gameGrid.length; x++) {
            for (int y = 0; y < gameGrid[x].length; y++) {
                gameGrid[x][y].setPointState(PointState.BLANK);
            }
        }
    }

    public static void increaseCapturedStonesNumber() {
        if (currentStoneColor.equals(PointState.STONE_BLACK)) {
            capturedWhiteStones += getPointsToRemove().size();
        } else if (currentStoneColor.equals(PointState.STONE_WHITE)) {
            capturedBlackStones += getPointsToRemove().size();
        }
    }

    public static boolean isAllowedToPlace(double xCoordinate, double yCoordinate, PointState stoneColor) {
        currentStoneColor = stoneColor;
        Point pointOnGrid = new Point(xCoordinate, yCoordinate);
        pointOnGrid.setPointState(stoneColor);

        if ((blockedPoint != null) && (!blockedPoint.getPointState().equals(stoneColor))) {
            int row = getPositionIndexFromCoordinate(blockedPoint.getY());
            int column = getPositionIndexFromCoordinate(blockedPoint.getX());
            gameGrid[row][column].setPointState(PointState.BLANK);
            blockedPoint = null;
            block = false;
        }

        if (isPositionOccupied(pointOnGrid)) {
            return false;
        }

        if (hasAnyOpenWay(pointOnGrid)) {
            addStone(new Point(xCoordinate, yCoordinate), stoneColor);
            getSurroundedPoints();
            changeStateOfRemovedPoints();
            increaseCapturedStonesNumber();
            // add method, for transfering Set<Point> getSurroundedPoints to the client view
            // after that we can uncomment getSurroundedPoints.clear() method(see below)
            //getSurroundedPoints.clear();
            return true;
        }

        if (isMutuallySurrounded(pointOnGrid, stoneColor)) {
            changeStateOfRemovedPoints();
            if (GameField.isBlock()) {
                GameField.repeatingPosition();
            }
            increaseCapturedStonesNumber();
            // add method, for transfering Set<Point> getSurroundedPoints to the client view
            // after that we can uncomment getSurroundedPoints.clear() method(see below)
            //getSurroundedPoints.clear();
            return true;
        }
        return false;
    }

    private static boolean isMutuallySurrounded(Point point, PointState stoneColor) {
        int row = getPositionIndexFromCoordinate(point.getY());
        int column = getPositionIndexFromCoordinate(point.getX());
        gameGrid[row][column].setPointState(stoneColor);
        getSurroundedPoints();
        if (lookingForSurroundedEnemyStones(stoneColor)) {
            removePointsWithCurrentStoneColor();
            return true;
        }
        gameGrid[row][column].setPointState(PointState.BLANK);
        points.clear();
        pointsToRemove.clear();
        return false;
    }

    public static void removePointsWithCurrentStoneColor() {
        Iterator<Point> iterator = pointsToRemove.iterator();
        while (iterator.hasNext()) {
            Point point = iterator.next();
            if (point.getPointState().equals(currentStoneColor)) {
                iterator.remove();
            }
        }
        checkPointToBlock();
    }

    public static void repeatingPosition() {
        for (Point point : pointsToRemove) {
            blockedPoint = point.clone();
            break;
        }
        int row = getPositionIndexFromCoordinate(blockedPoint.getY());
        int column = getPositionIndexFromCoordinate(blockedPoint.getX());
        gameGrid[row][column].setPointState(PointState.BLOCKED);
    }

    public static void checkPointToBlock() {
        if (pointsToRemove.size() == 1) {
            block = true;
        } else {
            block = false;
        }
    }

    private static boolean lookingForSurroundedEnemyStones(PointState stoneColor) {
        Iterator<Point> iterator = pointsToRemove.iterator();
        while (iterator.hasNext()) {
            Point point = iterator.next();
            if (point.getPointState().equals(getEnemyStoneColor(stoneColor))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPositionOccupied(Point target) {
        int row = getPositionIndexFromCoordinate(target.getY());
        int column = getPositionIndexFromCoordinate(target.getX());
        if (gameGrid[row][column].getPointState() != PointState.BLANK) {
            return true;
        }
        return false;
    }

    private static boolean hasAnyOpenWay(Point target) {
        initTempGrid();
        PointState enemyColor = getEnemyStoneColor(target.getPointState());
        int row = getPositionIndexFromCoordinate(target.getY());
        int column = getPositionIndexFromCoordinate(target.getX());
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

    private static int getPositionIndexFromCoordinate(double coordinate) {
        return (int) coordinate / STEP_SIZE;
    }

    public static void addStone(Point stoneCoordinates, PointState pointState) {
        int row = getPositionIndexFromCoordinate(stoneCoordinates.getY());
        int column = getPositionIndexFromCoordinate(stoneCoordinates.getX());
        gameGrid[row][column].setPointState(pointState);
    }

    public static void getSurroundedPoints() {
        for (int x = 0; x < gameGrid.length; x++) {
            for (int y = 0; y < gameGrid[x].length; y++) {
                if (!hasAnyOpenWay(gameGrid[x][y])) {
                    pointsToRemove.add(gameGrid[x][y].clone());
                    points.add(gameGrid[x][y]);
                }
            }
        }
    }

    public static void changeStateOfRemovedPoints() {
        if (points.size() > 0) {
            for (Point point : points) {
                if (point.getPointState().equals(getEnemyStoneColor(currentStoneColor))) {
                    point.setPointState(PointState.BLANK);
                }
            }
            points.clear();
        }
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