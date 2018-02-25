package model;


import org.apache.log4j.Logger;

public class GameField {

    private static final int STEP_SIZE = 100;
    private static Point[][] gameGrid;
    private static final Logger logger = Logger.getLogger(GameField.class);
    static Point[][] tempGrid;

    public static void initGameField(Point[][] allPoints){//points received in xml from player after game start
        gameGrid = allPoints;
        for (int i = 0; i < gameGrid.length; i++){
            for (int j = 0; j < gameGrid[i].length; j++){
                System.out.print(gameGrid[i][j].getX() + " -- " + gameGrid[i][j].getY() + "      ");
            }
            System.out.println();
        }
    }

    public static boolean isAllowedToPlace(double x, double y, PointState stoneColor){
         tempGrid = gameGrid.clone();
        if (hasAnyOpenWay(stoneColor, new Point(x, y))){
            return true;
        }
        return false;
    }

    private static boolean hasAnyOpenWay(PointState current, Point target){
        PointState enemyColor = getEnemyStoneColor(current);
        int row = getArrayCellIndexFromCoordinate(target.getY());
        int column = getArrayCellIndexFromCoordinate(target.getX());
        tempGrid[row][column].setPointState(PointState.TARGET);//temporary
        if (wayIsOpen(current, enemyColor, row, column)){
            System.out.println("yess");
            return true;
        }
        System.out.println("noooo");
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

        try {
            System.out.println(tempGrid[rowToCheck][columnToCheck].getPointState()+ " " + rowToCheck + " " + columnToCheck);
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == PointState.BLANK){
                logger.info("Blank cell " + rowToCheck + " " + columnToCheck);
                return true;
            }
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == opposite){
                logger.info("Enemy stone ");
                return false;
            }
            if (tempGrid[rowToCheck][columnToCheck].getPointState() == current){
                tempGrid[rowToCheck][columnToCheck].setPointState(PointState.USED);
                logger.info("Own stone " );
                return wayIsOpen(current, opposite, rowToCheck, columnToCheck);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            logger.info("Out of game grid ");
            return false;
        }
        return wayIsOpen(current, opposite, rowToCheck, columnToCheck-1) ||
                wayIsOpen(current, opposite, rowToCheck, columnToCheck+1) || //right
                wayIsOpen(current, opposite, rowToCheck-1, columnToCheck) || //up
                wayIsOpen(current, opposite, rowToCheck+1, columnToCheck); //down

    }

    private static int getArrayCellIndexFromCoordinate(double coordinate){
        return (int)coordinate/STEP_SIZE;
    }

    public static void addStone(Point stoneCoordinates, PointState pointState){
        int row = getArrayCellIndexFromCoordinate(stoneCoordinates.getY());
        int column = getArrayCellIndexFromCoordinate(stoneCoordinates.getX());
        gameGrid[row][column].setPointState(pointState);
        //remove stones if necessary
    }

}
