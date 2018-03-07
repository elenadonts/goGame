package model;

import org.apache.log4j.Logger;

import java.util.*;

public class GameField {
    private static final Logger LOGGER = Logger.getLogger(GameField.class);
    private static final int STEP_SIZE = 80;
    private Point[][] gameGrid;
    private Point[][] tempGrid;
    private Set<Point> pointsToRemove = new HashSet<>();
    private Set<Point> points = new HashSet<>();
    private PointState currentStoneColor;
    private Point blockedPoint;
    private boolean block;
    private int capturedWhiteStones;
    private int capturedBlackStones;
    private int whiteCount;
    private int blackCount;


    public void initGameField(int gridSize) { //points received in xml from player after game start
        gameGrid = new Point[gridSize + 1][gridSize + 1];
        for (int x = 0; x < gameGrid.length; x++) {
            for (int y = 0; y < gameGrid[x].length; y++) {
                gameGrid[x][y] = new Point(y * STEP_SIZE, x * STEP_SIZE);
                gameGrid[x][y].setPointState(PointState.BLANK);
            }
        }
        LOGGER.info("New game grid of size " + gridSize + " initialized");
    }

    public boolean isAllowedToPlace(double xCoordinate, double yCoordinate, PointState stoneColor) {
        LOGGER.info("Target coordinates x: " + xCoordinate + "y: " + yCoordinate);
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
            LOGGER.info("Position occupied");
            return false;
        }

        if (hasAnyOpenWay(pointOnGrid)) {
            addStone(new Point(xCoordinate, yCoordinate), stoneColor);
            getSurroundedPoints();
            changeStateOfRemovedPoints();
            increaseCapturedStonesNumber();
            LOGGER.info("Stone placed");
            return true;
        }

        if (isMutuallySurrounded(pointOnGrid, stoneColor)) {
            changeStateOfRemovedPoints();
            if (block) {
                repeatingPosition();
            }
            increaseCapturedStonesNumber();
            return true;
        }
        return false;
    }

    public Set<Point> getPointsToRemove() {
        return pointsToRemove;
    }

    public void setPointsToRemoveClear() {
        pointsToRemove.clear();
    }

    public int getCapturedWhiteStones() {
        return capturedWhiteStones;
    }

    public int getCapturedBlackStones() {
        return capturedBlackStones;
    }

    public void countPlayersScore() {
        for (int i = 0; i < gameGrid.length; i++) {
            for (int j = 0; j < gameGrid[i].length; j++) {
                if (gameGrid[i][j].getPointState() == PointState.BLANK) {

                    initTempGrid();
                    tempGrid[i][j].setPointState(PointState.TARGET);
                    if (isSurroundedByOnePlayer(i, j, PointState.STONE_WHITE))
                        whiteCount++;

                    initTempGrid();
                    tempGrid[i][j].setPointState(PointState.TARGET);
                    if (isSurroundedByOnePlayer(i, j, PointState.STONE_BLACK))
                        blackCount++;
                }
            }
        }
        whiteCount -= capturedWhiteStones;
        blackCount -= capturedBlackStones;
        LOGGER.info("Game result: white - " + whiteCount + " black - " + blackCount);
    }

    public int getBlackCount() {
        return blackCount;
    }

    public int getWhiteCount() {
        return whiteCount;
    }

    private void increaseCapturedStonesNumber() {
        if (currentStoneColor.equals(PointState.STONE_BLACK)) {
            capturedWhiteStones += pointsToRemove.size();
        } else if (currentStoneColor.equals(PointState.STONE_WHITE)) {
            capturedBlackStones += pointsToRemove.size();
        }
    }

    private boolean isMutuallySurrounded(Point point, PointState stoneColor) {
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

    private void removePointsWithCurrentStoneColor() {
        Iterator<Point> iterator = pointsToRemove.iterator();
        while (iterator.hasNext()) {
            Point point = iterator.next();
            if (point.getPointState().equals(currentStoneColor)) {
                iterator.remove();
            }
        }
        checkPointToBlock();
    }

    private void repeatingPosition() {
        for (Point point : pointsToRemove) {
            blockedPoint = point.clone();
        }
        int row = getPositionIndexFromCoordinate(blockedPoint.getY());
        int column = getPositionIndexFromCoordinate(blockedPoint.getX());
        gameGrid[row][column].setPointState(PointState.BLOCKED);
    }

    private void checkPointToBlock() {
        block = pointsToRemove.size() == 1;
    }

    private boolean lookingForSurroundedEnemyStones(PointState stoneColor) {
        for (Point point : pointsToRemove) {
            if (point.getPointState().equals(getEnemyStoneColor(stoneColor))) {
                return true;
            }
        }
        return false;
    }

    private boolean isPositionOccupied(Point target) {
        int row = getPositionIndexFromCoordinate(target.getY());
        int column = getPositionIndexFromCoordinate(target.getX());
        if (gameGrid[row][column].getPointState() != PointState.BLANK) {
            return true;
        }
        return false;
    }

    private boolean hasAnyOpenWay(Point target) {
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

    private PointState getEnemyStoneColor(PointState currentColor) {
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

    private boolean wayIsOpen(PointState current, PointState opposite, int rowToCheck, int columnToCheck) {
        boolean isOpen;
        try {
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == PointState.BLANK) {
                return true;
            }
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == opposite) {
                return false;
            }
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == current) {
                tempGrid[rowToCheck][columnToCheck].setPointState(PointState.USED);
                return wayIsOpen(current, opposite, rowToCheck, columnToCheck);
            }
            tempGrid[rowToCheck][columnToCheck].setPointState(opposite);
            isOpen = wayIsOpen(current, opposite, rowToCheck, columnToCheck - 1) || //left
                    wayIsOpen(current, opposite, rowToCheck, columnToCheck + 1) || //right
                    wayIsOpen(current, opposite, rowToCheck - 1, columnToCheck) || //up
                    wayIsOpen(current, opposite, rowToCheck + 1, columnToCheck); //down
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == PointState.USED) return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
        return isOpen;
    }

    private int getPositionIndexFromCoordinate(double coordinate) {
        return (int) coordinate / STEP_SIZE;
    }

    private void addStone(Point stoneCoordinates, PointState pointState) {
        int row = getPositionIndexFromCoordinate(stoneCoordinates.getY());
        int column = getPositionIndexFromCoordinate(stoneCoordinates.getX());
        gameGrid[row][column].setPointState(pointState);
        LOGGER.info("Adding new stone to: [" + row + "][" + column + "] array cell");
    }

    private void getSurroundedPoints() {
        for (int x = 0; x < gameGrid.length; x++) {
            for (int y = 0; y < gameGrid[x].length; y++) {
                if (!hasAnyOpenWay(gameGrid[x][y])) {
                    pointsToRemove.add(gameGrid[x][y].clone());
                    points.add(gameGrid[x][y]);
                }
            }
        }
    }

    private void changeStateOfRemovedPoints() {
        if (points.size() > 0) {
            for (Point point : points) {
                if (point.getPointState().equals(getEnemyStoneColor(currentStoneColor))) {
                    point.setPointState(PointState.BLANK);
                }
            }
            points.clear();
        }
    }

    private void initTempGrid() {
        tempGrid = new Point[gameGrid.length][gameGrid[0].length];
        for (int x = 0; x < gameGrid.length; x++) {
            for (int y = 0; y < gameGrid[x].length; y++) {
                tempGrid[x][y] = gameGrid[x][y].clone();
            }
        }
    }

    private boolean isSurroundedByOnePlayer(int rowToCheck, int columnToCheck, PointState stoneColor) {
        boolean isSurrounded;
        try {
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == stoneColor) {
                return true;
            }
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == getEnemyStoneColor(stoneColor)) {
                return false;
            }
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == PointState.BLANK) {
                tempGrid[rowToCheck][columnToCheck].setPointState(PointState.USED);
                return isSurroundedByOnePlayer(rowToCheck, columnToCheck, stoneColor);
            }
            tempGrid[rowToCheck][columnToCheck].setPointState(stoneColor);
            isSurrounded = isSurroundedByOnePlayer(rowToCheck, columnToCheck - 1, stoneColor) &&
                    isSurroundedByOnePlayer(rowToCheck, columnToCheck + 1, stoneColor) &&
                    isSurroundedByOnePlayer(rowToCheck - 1, columnToCheck, stoneColor) &&
                    isSurroundedByOnePlayer(rowToCheck + 1, columnToCheck, stoneColor);
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == PointState.USED) return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return true;
        }
        return isSurrounded;
    }
}