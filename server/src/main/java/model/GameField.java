package model;

import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * GameField class manages game logic
 */
public class GameField {
    private static final Logger LOGGER = Logger.getLogger(GameField.class);
    private static final int TILE_FIELD = 400;
    private double tileSize;
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

    /**
     * sets length of one side of single cell
     * @param numberOfTiles number of game cells
     */
    public void initTileSize(int numberOfTiles) {
        tileSize = new BigDecimal(TILE_FIELD / numberOfTiles).setScale(2, RoundingMode.UP).doubleValue();
    }

    /**
     * tileSize getter
     * @return current cell size
     */
    public double getTileSize() {
        return tileSize;
    }

    /**
     * initiates 2d array which stores stones on the game grid
     * @param gridSize dimensions of game field
     */
    public void initGameField(int gridSize) { //points received in xml from player after game start
        gameGrid = new Point[gridSize + 1][gridSize + 1];
        for (int x = 0; x < gameGrid.length; x++) {
            for (int y = 0; y < gameGrid[x].length; y++) {
                gameGrid[x][y] = new Point(y * tileSize, x * tileSize);
                gameGrid[x][y].setPointState(PointState.BLANK);
            }
        }
        LOGGER.info("New game grid of size " + gridSize + " initialized");
    }

    /**
     * checks if current stone can be placed according to game rules
     * @param xCoordinate x
     * @param yCoordinate y
     * @param stoneColor current stone color
     * @return true if allowed, false if not
     */
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

    /**
     * @return points that are occupied by enemy
     */
    public Set<Point> getPointsToRemove() {
        return pointsToRemove;
    }

    /**
     * clears list of current points to be removed
     */
    public void setPointsToRemoveClear() {
        pointsToRemove.clear();
    }

    /**
     * @return number of occupied white stones
     */
    public int getCapturedWhiteStones() {
        return capturedWhiteStones;
    }

    /**
     * @return number of occupied black stones
     */
    public int getCapturedBlackStones() {
        return capturedBlackStones;
    }

    /**
     * counts score of players after game end
     */
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

    /**
     * current score of player who uses black stones
     * @return
     */
    public int getBlackCount() {
        return blackCount;
    }

    /**
     * current score of player who uses white stones
     * @return
     */
    public int getWhiteCount() {
        return whiteCount;
    }

    /**
     * resets current number of points for both players
     */
    private void increaseCapturedStonesNumber() {
        if (currentStoneColor.equals(PointState.STONE_BLACK)) {
            capturedWhiteStones += pointsToRemove.size();
        } else if (currentStoneColor.equals(PointState.STONE_WHITE)) {
            capturedBlackStones += pointsToRemove.size();
        }
    }

    /**
     * checks if one-color stones are occupied by enemy from both sides(game rule implementation)
     * @param point current point
     * @param stoneColor enemy stone color
     * @return true if stones are occupied from both sides
     */
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

    /**
     * service method to check if stones are surrounded
     */
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

    /**
     * sets certain point to be blocked from placing stone onto it
     */
    private void repeatingPosition() {
        for (Point point : pointsToRemove) {
            blockedPoint = point.clone();
        }
        int row = getPositionIndexFromCoordinate(blockedPoint.getY());
        int column = getPositionIndexFromCoordinate(blockedPoint.getX());
        gameGrid[row][column].setPointState(PointState.BLOCKED);
    }

    /**
     * checks if there is blocked point
     */
    private void checkPointToBlock() {
        block = pointsToRemove.size() == 1;
    }

    /**
     * checks if there are occupied enemy stones
     * @param stoneColor current stone color
     * @return true if occupied enemy stones exist
     */
    private boolean lookingForSurroundedEnemyStones(PointState stoneColor) {
        for (Point point : pointsToRemove) {
            if (point.getPointState().equals(getEnemyStoneColor(stoneColor))) {
                return true;
            }
        }
        return false;
    }

    /**
     * checks if target cell is occupied by other stone
     * @param target target coordinates
     * @return true if cell is vacant
     */
    private boolean isPositionOccupied(Point target) {
        int row = getPositionIndexFromCoordinate(target.getY());
        int column = getPositionIndexFromCoordinate(target.getX());
        return gameGrid[row][column].getPointState() != PointState.BLANK;
    }

    /**
     * checks if one of the sides is open or one stones in its group has way out
     * @param target
     * @return
     */
    private boolean hasAnyOpenWay(Point target) {
        initTempGrid();
        PointState enemyColor = getEnemyStoneColor(target.getPointState());
        int row = getPositionIndexFromCoordinate(target.getY());
        int column = getPositionIndexFromCoordinate(target.getX());
        tempGrid[row][column].setPointState(PointState.TARGET);
        return wayIsOpen(target.getPointState(), enemyColor, row, column);
    }

    /**
     * gets color of enemy based on its own
     * @param currentColor current stone color
     * @return enemy stone color
     */
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

    /**
     * goes to four sides looking for any open cell
     * @param current current stone color
     * @param opposite enemy stone color
     * @param rowToCheck row of stone in array
     * @param columnToCheck column of stone in array
     * @return true if has open way
     */
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

    /**
     * gets corresponding array cell from coordinate on user window
     * @param coordinate x or y of window
     * @return cell index
     */
    private int getPositionIndexFromCoordinate(double coordinate) {
        return (int) (coordinate / tileSize);
    }

    /**
     * adds new stone to array of other stones
     * @param stoneCoordinates coordinates of stone
     * @param pointState color of stone
     */
    private void addStone(Point stoneCoordinates, PointState pointState) {
        int row = getPositionIndexFromCoordinate(stoneCoordinates.getY());
        int column = getPositionIndexFromCoordinate(stoneCoordinates.getX());
        gameGrid[row][column].setPointState(pointState);
        LOGGER.info("Adding new stone to: [" + row + "][" + column + "] array cell");
    }

    /**
     * finds points that are surrounded by enemy
     */
    private void getSurroundedPoints() {
        for (Point[] aGameGrid : gameGrid) {
            for (Point anAGameGrid : aGameGrid) {
                if (!hasAnyOpenWay(anAGameGrid)) {
                    pointsToRemove.add(anAGameGrid.clone());
                    points.add(anAGameGrid);
                }
            }
        }
    }

    /**
     * reset removed points state to blank
     */
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

    /**
     * copies original array of cells for finding way out for other method
     */
    private void initTempGrid() {
        tempGrid = new Point[gameGrid.length][gameGrid[0].length];
        for (int x = 0; x < gameGrid.length; x++) {
            for (int y = 0; y < gameGrid[x].length; y++) {
                tempGrid[x][y] = gameGrid[x][y].clone();
            }
        }
    }

    /**
     * checks if territory unit is fully surrounded by one player so it can be counted as occupied
     * @param rowToCheck territory unit row in array
     * @param columnToCheck territory unit column in array
     * @param stoneColor color of stone to be checked
     * @return true if territory is surrounded by one player
     */
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